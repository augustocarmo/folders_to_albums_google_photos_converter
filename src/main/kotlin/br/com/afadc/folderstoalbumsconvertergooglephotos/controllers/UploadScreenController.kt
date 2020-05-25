package br.com.afadc.folderstoalbumsconvertergooglephotos.controllers

import br.com.afadc.folderstoalbumsconvertergooglephotos.PhotosUtils.MediaUploader
import com.google.photos.library.v1.PhotosLibraryClient
import br.com.afadc.folderstoalbumsconvertergooglephotos.db.AppDatabase
import br.com.afadc.folderstoalbumsconvertergooglephotos.entities.Album
import br.com.afadc.folderstoalbumsconvertergooglephotos.entities.Media
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
                uploadSelectedAlbums()
            }

            override fun onCancelUploadButtonClicked() {
                cancelMediaUpload()
            }
        }

        currentFileNodesCreatorTaskListener = object : FileNodesCreator.FileNodesCreatorTaskListener {
            override fun onCompleted(rootNode: DefaultMutableTreeNode) {
                SwingUtilities.invokeLater {
                    if (rootNode.childCount == 0) {
                        JOptionPane.showMessageDialog(
                            appFrame,
                            resBundle.noAlbumsDirectoriesPopUpMessage,
                            resBundle.noAlbumsDirectoriesPopUpTitle,
                            JOptionPane.WARNING_MESSAGE
                        )

                        dismiss()

                        return@invokeLater
                    }

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
            override fun onWillUploadAlbum(album: Album) {
                SwingUtilities.invokeLater {
                    // TODO:
                }
            }

            override fun onWWillUploadAlbumMedia(album: Album, media: Media) {
                SwingUtilities.invokeLater {
                    appendNewMediaUploadLogEntry(
                        resBundle.logOnWillUploadMediaFormat.format(
                            "file:${media.file.absolutePath}",
                            media.file.absolutePath
                        )
                    )
                }
            }

            override fun onAlbumMediaUploadResult(success: Boolean, album: Album, media: Media) {
                SwingUtilities.invokeLater {
                    if (success) {
                        appendNewMediaUploadLogEntry(resBundle.logOnMediaUploadedSucceeded)
                    } else {
                        appendNewMediaUploadLogEntry(resBundle.logOnMediaUploadFailed)
                    }
                }
            }

            override fun onWillCreateAlbumMediaItems(album: Album, medias: List<Media>) {
                SwingUtilities.invokeLater {
                    appendNewMediaUploadLogEntry(
                        resBundle.logOnWillCreateMediaItemsFormat.format(
                            medias.count()
                        )
                    )
                }
            }

            override fun onAlbumMediaItemCreationResult(success: Boolean, album: Album, media: Media) {
                SwingUtilities.invokeLater {
                    if (success) {
                        appendNewMediaUploadLogEntry(
                            resBundle.logOnMediaItemCreationSucceededFormat.format(
                                media.file.absolutePath
                            )
                        )
                    } else {
                        appendNewMediaUploadLogEntry(
                            resBundle.logOnMediaItemCreationFailedFormat.format(
                                media.file.absolutePath
                            )
                        )
                    }
                }
            }

            override fun onAlbumUploadCompleted(album: Album) {
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
            return@fold acc + (node.userObject as FileNodesCreator.FileNode).mediaCount
        })

        uploadScreen.setUploadButtonText(
            resBundle.uploadMediaButtonNonZeroMediaCountFormat.format(
                albumCount,
                photoCount
            )
        )
    }

    private fun uploadSelectedAlbums() {
        val selectedTreePaths = uploadScreen.albumsTreeViewSelectedTreePaths
        if (selectedTreePaths == null || selectedTreePaths.isEmpty()) {
            Log.i(TAG, "No media to upload")

            JOptionPane.showMessageDialog(
                appFrame,
                resBundle.noAlbumsSelectedToUploadPopUpMessage,
                resBundle.noAlbumsSelectedToUploadPopUpTitle,
                JOptionPane.WARNING_MESSAGE
            )

            return
        }

        val alreadyUploadedAndCreatedMediaPaths = appDatabase.getUploadedAndCreatedDbMediasPaths()
        val albums = ArrayList<Album>()

        for (selectedTreePath in selectedTreePaths) {
            val mediaFiles = ArrayList<Media>()

            val albumNode = (selectedTreePath.lastPathComponent as DefaultMutableTreeNode)
            val albumFile = (albumNode.userObject as FileNodesCreator.FileNode).file
            val albumEnumeration = albumNode.preorderEnumeration()
            while (albumEnumeration.hasMoreElements()) {
                val nextNode = (albumEnumeration.nextElement() as DefaultMutableTreeNode)
                val file = (nextNode.userObject as FileNodesCreator.FileNode).file

                if (file.isFile && !alreadyUploadedAndCreatedMediaPaths.contains(file.absolutePath)) {
                    mediaFiles.add(Media(file))
                }
            }

            if (mediaFiles.isNotEmpty()) {
                albums.add(Album(albumFile, mediaFiles))
            }
        }

        clearMediaUploadLogList()
        appendNewMediaUploadLogEntry(resBundle.logStartingToUploadMedias)

        cancelMediaUpload()

        setRenderingState(RenderingState.UPLOADING_MEDIA)

        val newAlbumsUploadTask = mediaUploader.createAlbumsUploadTask(albums)

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
            stringBuilder.append("<html>")
            mediaUploadLogList.forEach {
                stringBuilder.append(it).append("<br>")
            }
            stringBuilder.append("</html>")

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