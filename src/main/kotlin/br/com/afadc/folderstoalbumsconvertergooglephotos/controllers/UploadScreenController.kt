package br.com.afadc.folderstoalbumsconvertergooglephotos.controllers

import br.com.afadc.folderstoalbumsconvertergooglephotos.PhotosUtils.MediaUploader
import com.google.photos.library.v1.PhotosLibraryClient
import br.com.afadc.folderstoalbumsconvertergooglephotos.db.AppDatabase
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.AppResBundles
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.FileNodesCreator
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.Log
import br.com.afadc.folderstoalbumsconvertergooglephotos.views.AppFrame
import br.com.afadc.folderstoalbumsconvertergooglephotos.views.UploadScreen
import java.io.File
import java.lang.StringBuilder
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class UploadScreenController(
    private val appFrame: AppFrame,
    private val photosLibraryClient: PhotosLibraryClient,
    private val appDatabase: AppDatabase,
    private val userEmail: String,
    private val albumsDirectoryFile: File
) : IScreenController {

    private enum class RenderingState {
        NONE,
        LOADING_TREE_VIEW,
        SELECTING_MEDIA,
        UPLOADING_MEDIA
    }

    interface Listener {
        fun onDismissRequested()
    }

    private var renderingState = RenderingState.NONE

    private val resBundle = AppResBundles.getUploadScreenBundle()

    private val uploadScreen = UploadScreen(resBundle)

    private var currentAlbumsUploadTask: MediaUploader.AlbumsUploadTask? = null

    private val mediaUploadLogList = ArrayList<String>(MAX_UPLOAD_LOG_LIST_SIZE)

    private val mediaUploader by lazy { MediaUploader() }

    private val albumsUploadTaskListener: MediaUploader.AlbumsUploadTaskListener

    private val fileNodesCreator = FileNodesCreator()
    private var currentFileNodesCreatorTask: FileNodesCreator.FileNodesCreatorTask? = null
    private val currentFileNodesCreatorTaskListener: FileNodesCreator.FileNodesCreatorTaskListener

    var listener: Listener? = null

    init {
        uploadScreen.setUserEmail(userEmail)

        uploadScreen.listener = object : UploadScreen.Listener {
            override fun onBackButtonClicked() {
                dismiss()
            }

            override fun onAlbumsTreeViewSelectedTreePathsChanged(selectedPaths: Array<TreePath>?) {
                updateUploadButtonText()
            }

            override fun onUploadButtonClicked() {
                uploadSelectedMedia()
            }

            override fun onCancelUploadButtonClicked() {
                cancelMediaUpload()
            }
        }

        currentFileNodesCreatorTaskListener = object : FileNodesCreator.FileNodesCreatorTaskListener {
            override fun onCompleted(rootNode: DefaultMutableTreeNode) {
                SwingUtilities.invokeLater {
                    val albumsTreeViewModel = DefaultTreeModel(rootNode)
                    uploadScreen.setAlbumsTreeViewModel(albumsTreeViewModel)

                    setRenderingState(RenderingState.SELECTING_MEDIA)
                }
            }

            override fun onCanceled() {
                // do nothing
            }
        }

        albumsUploadTaskListener = object : MediaUploader.AlbumsUploadTaskListener {
            override fun onWillUploadAlbum(albumDir: File) {
                SwingUtilities.invokeLater {
                    // TODO:
                }
            }

            override fun onWWillUploadAlbumMedia(albumDir: File, mediaFile: File) {
                SwingUtilities.invokeLater {
                    appendNewMediaUploadLogEntry(
                        resBundle.logOnWillUploadMediaFormat.format(
                            "file:${mediaFile.absolutePath}",
                            mediaFile.absolutePath
                        )
                    )
                }
            }

            override fun onAlbumMediaUploadResult(success: Boolean, albumDir: File, mediaFile: File) {
                SwingUtilities.invokeLater {
                    if (success) {
                        appendNewMediaUploadLogEntry(resBundle.logOnMediaUploadedSucceeded)
                    } else {
                        appendNewMediaUploadLogEntry(resBundle.logOnMediaUploadFailed)
                    }
                }
            }

            override fun onWillCreateAlbumMediaItems(albumDir: File, mediasFiles: List<File>) {
                SwingUtilities.invokeLater {
                    appendNewMediaUploadLogEntry(
                        resBundle.logOnWillCreateMediaItemsFormat.format(
                            mediasFiles.count()
                        )
                    )
                }
            }

            override fun onAlbumMediaItemCreationResult(success: Boolean, albumDir: File, mediaFile: File) {
                SwingUtilities.invokeLater {
                    if (success) {
                        appendNewMediaUploadLogEntry(
                            resBundle.logOnMediaItemCreationSucceededFormat.format(
                                mediaFile.absolutePath
                            )
                        )
                    } else {
                        appendNewMediaUploadLogEntry(
                            resBundle.logOnMediaItemCreationFailedFormat.format(
                                mediaFile.absolutePath
                            )
                        )
                    }
                }
            }

            override fun onAlbumUploadCompleted(albumDir: File) {
                SwingUtilities.invokeLater {
                    // TODO:
                }
            }

            override fun onError(error: MediaUploader.Error) {
                currentAlbumsUploadTask?.cancel()

                SwingUtilities.invokeLater {
                    val errorMessage = when (error) {
                        MediaUploader.Error.CANT_ACCESS_GOOGLE_PHOTOS -> {
                            resBundle.uploadMediaErrorPopUpNoGooglePhotosAccessMessage
                        }
                        MediaUploader.Error.GENERAL_ERROR -> {
                            resBundle.uploadMediaErrorPopUpGeneralErrorMessage
                        }
                        MediaUploader.Error.ALBUM_CREATION_ERROR -> {
                            resBundle.uploadMediaErrorPopUpAlbumCreationFailedMessage
                        }
                    }

                    JOptionPane.showMessageDialog(
                        appFrame,
                        errorMessage,
                        resBundle.uploadMediaErrorPopUpTitle,
                        JOptionPane.WARNING_MESSAGE
                    )
                }
            }

            override fun onCompleted() {
                SwingUtilities.invokeLater {
                    setRenderingState(RenderingState.SELECTING_MEDIA)
                }
            }

            override fun onCanceled() {
                SwingUtilities.invokeLater {
                    setRenderingState(RenderingState.SELECTING_MEDIA)
                }
            }
        }

        setupAlbumsTreeViewContent()
    }

    override fun show() {
        appFrame.contentPane.removeAll()
        appFrame.add(uploadScreen)

        appFrame.validate()
        appFrame.repaint()
    }

    private fun setupAlbumsTreeViewContent() {
        setRenderingState(RenderingState.LOADING_TREE_VIEW)

        cancelCurrentFileNodesCreatorTask()

        Thread {
            val newFilesNodeCreatorTask = fileNodesCreator.createFileNodesCreatorTask(
                albumsDirectoryFile,
                MediaUploader.ALL_VALID_EXTENSIONS
            )
            currentFileNodesCreatorTask = newFilesNodeCreatorTask
            fileNodesCreator.executeFileNodesCreatorTask(
                newFilesNodeCreatorTask,
                currentFileNodesCreatorTaskListener
            )
        }.start()
    }

    private fun cancelCurrentFileNodesCreatorTask() {
        currentFileNodesCreatorTask?.cancel()
        currentFileNodesCreatorTask = null
    }

    private fun onBeforeSettingRenderingState() {
        uploadScreen.setFetchingAlbumsLabelVisibility(false)
        uploadScreen.setAlbumsTreeViewScrollablePaneVisibility(false)
        uploadScreen.setUploadLogTextAreaScrollablePaneVisibility(false)
        uploadScreen.setUploadButtonVisibility(false)
        uploadScreen.setCancelUploadButtonVisibility(false)
    }

    private fun setRenderingState(renderingState: RenderingState) {
        onBeforeSettingRenderingState()

        when (renderingState) {
            RenderingState.NONE -> Unit // do nothing
            RenderingState.LOADING_TREE_VIEW -> applyLoadingTreeViewRenderingState()
            RenderingState.SELECTING_MEDIA -> applySelectingMediaRenderingState()
            RenderingState.UPLOADING_MEDIA -> applyUploadingMediaRenderingState()
        }

        this.renderingState = renderingState
    }

    private fun applyLoadingTreeViewRenderingState() {
        uploadScreen.setFetchingAlbumsLabelVisibility(true)
    }

    private fun applySelectingMediaRenderingState() {
        uploadScreen.setAlbumsTreeViewScrollablePaneVisibility(true)
        uploadScreen.setUploadButtonVisibility(true)
    }

    private fun applyUploadingMediaRenderingState() {
        uploadScreen.setUploadLogTextAreaScrollablePaneVisibility(true)
        uploadScreen.setCancelUploadButtonVisibility(true)
    }

    private fun dismiss() {
        cancelCurrentFileNodesCreatorTask()
        cancelMediaUpload()

        listener?.onDismissRequested()
    }

    private fun updateUploadButtonText() {
        val selectedTreePaths = uploadScreen.albumsTreeViewSelectedTreePaths
        if (selectedTreePaths == null || selectedTreePaths.isEmpty()) {
            uploadScreen.setUploadButtonText(resBundle.uploadMediaButtonZeroMediaCount)

            return
        }

        val selectedNodes = selectedTreePaths.map {
            (it.lastPathComponent as DefaultMutableTreeNode)
        }

        val albumCount = selectedNodes.count()
        val photoCount = selectedNodes.fold(0, { acc, node ->
            return@fold acc + (node.userObject as FileNodesCreator.FileNode).photoCount
        })

        uploadScreen.setUploadButtonText(
            resBundle.uploadMediaButtonNonZeroMediaCountFormat.format(
                albumCount,
                photoCount
            )
        )
    }

    private fun uploadSelectedMedia() {
        val selectedTreePaths = uploadScreen.albumsTreeViewSelectedTreePaths
        if (selectedTreePaths == null || selectedTreePaths.isEmpty()) {
            Log.i(TAG, "No media to upload")

            JOptionPane.showMessageDialog(
                appFrame,
                resBundle.noMediaSelectedPopUpMessage,
                resBundle.noMediaSelectedPopUpTitle,
                JOptionPane.WARNING_MESSAGE
            )

            return
        }

        val selectedDirectories = selectedTreePaths.map {
            ((it.lastPathComponent as DefaultMutableTreeNode).userObject as FileNodesCreator.FileNode).file
        }

        clearMediaUploadLogList()
        appendNewMediaUploadLogEntry(resBundle.logStartingToUploadMedias)

        cancelMediaUpload()

        setRenderingState(RenderingState.UPLOADING_MEDIA)

        val newAlbumsUploadTask = mediaUploader.createAlbumsUploadTask(
            selectedDirectories
        )

        currentAlbumsUploadTask = newAlbumsUploadTask

        Thread {
            if (currentAlbumsUploadTask != newAlbumsUploadTask) {
                return@Thread
            }

            mediaUploader.executeAlbumsUploadTask(
                photosLibraryClient,
                appDatabase,
                newAlbumsUploadTask,
                albumsUploadTaskListener
            )
        }.start()
    }

    private fun appendNewMediaUploadLogEntry(logText: String) {
        synchronized(mediaUploadLogList) {
            mediaUploadLogList.add(logText)
            if (mediaUploadLogList.count() > MAX_UPLOAD_LOG_LIST_SIZE) {
                mediaUploadLogList.removeAt(0)
            }

            updateMediaUploadScreenUploadLogText()
        }
    }

    private fun updateMediaUploadScreenUploadLogText() {
        synchronized(mediaUploadLogList) {
            val stringBuilder = StringBuilder()
            mediaUploadLogList.forEach {
                stringBuilder.append(it).append("<br>")
            }

            uploadScreen.setUploadLogTextAreaText(stringBuilder.toString())
            uploadScreen.scrollLogsViewToTheBottom()
        }
    }

    private fun clearMediaUploadLogList() {
        synchronized(mediaUploadLogList) {
            mediaUploadLogList.clear()

            updateMediaUploadScreenUploadLogText()
        }
    }

    private fun cancelMediaUpload() {
        currentAlbumsUploadTask?.cancel()
        currentAlbumsUploadTask = null
    }

    companion object {
        private val TAG = UploadScreenController::class.java.simpleName

        private const val MAX_UPLOAD_LOG_LIST_SIZE = 25
    }
}