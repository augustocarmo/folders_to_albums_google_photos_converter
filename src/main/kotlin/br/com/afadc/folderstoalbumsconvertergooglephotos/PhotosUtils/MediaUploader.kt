package br.com.afadc.folderstoalbumsconvertergooglephotos.PhotosUtils

import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.library.v1.upload.UploadMediaItemRequest
import com.google.photos.library.v1.util.NewMediaItemFactory
import com.google.rpc.Code
import br.com.afadc.folderstoalbumsconvertergooglephotos.db.AppDatabase
import br.com.afadc.folderstoalbumsconvertergooglephotos.db.entities.DbAlbum
import br.com.afadc.folderstoalbumsconvertergooglephotos.db.entities.DbMedia
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.Log
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.min

class MediaUploader {

    enum class Error {
        CANT_ACCESS_GOOGLE_PHOTOS,
        ALBUM_CREATION_ERROR,
        GENERAL_ERROR
    }

    private interface AlbumUploadListener {
        fun onWillUploadMedia(mediaFile: File)
        fun onMediaUploadResult(success: Boolean, mediaFile: File)
        fun onWillCreateMediaItems(mediasFiles: List<File>)
        fun onMediaItemCreationResult(success: Boolean, mediaFile: File)
        fun onError(error: Error)
        fun onCompleted()
        fun onCanceled()
    }

    interface AlbumsUploadTaskListener {
        fun onWillUploadAlbum(albumDir: File)
        fun onWWillUploadAlbumMedia(albumDir: File, mediaFile: File)
        fun onAlbumMediaUploadResult(success: Boolean, albumDir: File, mediaFile: File)
        fun onWillCreateAlbumMediaItems(albumDir: File, mediasFiles: List<File>)
        fun onAlbumMediaItemCreationResult(success: Boolean, albumDir: File, mediaFile: File)
        fun onAlbumUploadCompleted(albumDir: File)
        fun onError(error: Error)
        fun onCompleted()
        fun onCanceled()
    }

    abstract class MediaUploadTask {

        interface CancelListener {
            fun onCanceled()
        }

        private var cancelListeners = ArrayList<CancelListener>()

        var isCanceled = false
            private set

        @Synchronized
        fun cancel() {
            if (isCanceled) {
                return
            }

            isCanceled = true

            synchronized(cancelListeners) {
                for (listener in cancelListeners.toList()) {
                    listener.onCanceled()
                }
            }
        }

        fun addCancelListener(listener: CancelListener) {
            synchronized(cancelListeners) {
                if (cancelListeners.contains(listener)) {
                    return
                }

                cancelListeners.add(listener)
            }
        }

        fun removeCancelListener(listener: CancelListener) {
            synchronized(cancelListeners) {
                cancelListeners.remove(listener)
            }
        }
    }

    private class AlbumUploadTask(val albumDir: File) : MediaUploadTask()

    class AlbumsUploadTask(val albumsDirs: List<File>) : MediaUploadTask()

    private fun createAlbumUploadTask(albumDir: File) =
        AlbumUploadTask(
            albumDir
        )

    fun createAlbumsUploadTask(albumsDirs: List<File>) =
        AlbumsUploadTask(
            albumsDirs
        )

    private fun getTotalFilesLengthInBytes(files: List<File>): Long {
        if (files.isEmpty()) {
            return 0L
        }

        return files.fold(0L) { acc, file ->
            acc + file.length()
        }
    }

    private fun extractMediasFromAlbumDir(albumDir: File): List<File> {
        if (!albumDir.isDirectory) {
            throw IllegalArgumentException("album dir [$albumDir] is not a directory")
        }

        val validExtensions = ALL_VALID_EXTENSIONS.map { it.toLowerCase() }
        val mediasFiles = ArrayList<File>()

        val filesToCheck = arrayListOf(albumDir)

        while (filesToCheck.isNotEmpty()) {
            val currentFile = filesToCheck.removeAt(0)
            if (currentFile.isDirectory) {
                val childrenFiles = currentFile.listFiles()
                if (childrenFiles != null) {
                    filesToCheck.addAll(childrenFiles)
                }
            } else if (validExtensions.contains(currentFile.extension.toLowerCase())) {
                mediasFiles.add(currentFile)
            }
        }

        return mediasFiles
    }

    private fun canPhotosLibraryClientBeUsed(photosLibraryClient: PhotosLibraryClient): Boolean {
        return !photosLibraryClient.isTerminated && !photosLibraryClient.isShutdown
    }

    private fun batchCreateAlbumMediaItems(
        photosLibraryClient: PhotosLibraryClient,
        db: AppDatabase,
        photosAlbumId: String,
        albumUploadTask: AlbumUploadTask,
        albumName: String,
        uploadFilesAndTokens: List<Pair<File, String>>,
        albumUploadListener: AlbumUploadListener
    ) {
        if (albumUploadTask.isCanceled) {
            return
        }

        if (uploadFilesAndTokens.count() > MAX_ITEMS_UPLOADS_PER_BATCH) {
            throw IllegalArgumentException("mediaUploadTokensList has a size [${uploadFilesAndTokens.count()}] greater than the limit [$MAX_ITEMS_UPLOADS_PER_BATCH]")
        }


        albumUploadListener.onWillCreateMediaItems(
            uploadFilesAndTokens.map { it.first }
        )

        val mediaItemsUploadTokens = uploadFilesAndTokens.map {
            NewMediaItemFactory.createNewMediaItem(it.second)
        }

        val mediaItemsToCreateBatch = mediaItemsUploadTokens.subList(
            0,
            min(
                MAX_ITEMS_UPLOADS_PER_BATCH,
                mediaItemsUploadTokens.count()
            )
        )

        val mediaItemsCreationResponse = photosLibraryClient.batchCreateMediaItems(
            photosAlbumId,
            mediaItemsToCreateBatch
        )

        // this operation should not be canceled, once the items creations has already occurred.
        // that is why the task isCanceled flag is not checked and the loop is duplicated afterwards
        for (itemResponse in mediaItemsCreationResponse.newMediaItemResultsList) {
            val mediaFile = uploadFilesAndTokens.first { it.second == itemResponse.uploadToken }.first
            if (itemResponse.status.code == Code.OK_VALUE) {
                db.insertOrUpdateMedia(
                    DbMedia(
                        path = db.getRelativeFilePathToDbDir(mediaFile),
                        albumName = albumName,
                        isUploadedAndCreated = true,
                        uploadToken = null,
                        uploadTokenGenerationTime = null
                    )
                )
            }
        }

        if (albumUploadTask.isCanceled) {
            return
        }

        for (itemResponse in mediaItemsCreationResponse.newMediaItemResultsList) {
            if (albumUploadTask.isCanceled) {
                return
            }

            val success = itemResponse.status.code == Code.OK_VALUE
            val mediaFile = uploadFilesAndTokens.first { it.second == itemResponse.uploadToken }.first

            albumUploadListener.onMediaItemCreationResult(success, mediaFile)
        }
    }

    private fun executeAlbumUploadTask(
        photosLibraryClient: PhotosLibraryClient,
        db: AppDatabase,
        task: AlbumUploadTask,
        listener: AlbumUploadListener
    ) {
        /**
         * Only one task can run at any time, otherwise multiple albums/photos and etc can be created wrongly
         * To reproduce that behavior, on an album that is not yet created on Photos, start/cancel the
         * upload of it multiple times, very fast
         */
        synchronized(UPLOAD_TASK_LOCK) {
            val taskCancelListener = object :
                MediaUploadTask.CancelListener {
                override fun onCanceled() {
                    listener.onCanceled()
                }
            }

            task.addCancelListener(taskCancelListener)

            try {
                if (task.isCanceled) {
                    return
                }

                if (!canPhotosLibraryClientBeUsed(photosLibraryClient)) {
                    println("the Photos Library Client can no longer be used.")

                    if (task.isCanceled) {
                        return
                    }

                    listener.onError(Error.CANT_ACCESS_GOOGLE_PHOTOS)

                    return
                }

                val albumDir = task.albumDir
                val albumMediasFiles = extractMediasFromAlbumDir(albumDir)
                if (albumMediasFiles.isEmpty()) {
                    listener.onCompleted()
                }

                val photosAlbumId: String

                val dbAlbum = db.getDbAlbumByDir(albumDir)

                if (task.isCanceled) {
                    return
                }

                val albumName = albumDir.name

                if (dbAlbum != null) {
                    photosAlbumId = dbAlbum.photos_album_id
                } else {
                    val newPhotosAlbumId = photosLibraryClient.createAlbum(albumName)?.id
                    if (newPhotosAlbumId == null) {
                        println("The photos album id is null")

                        if (task.isCanceled) {
                            return
                        }

                        listener.onError(Error.ALBUM_CREATION_ERROR)

                        return
                    }

                    photosAlbumId = newPhotosAlbumId

                    db.insertOrUpdateDbAlbum(
                        DbAlbum(
                            name = albumName,
                            photos_album_id = photosAlbumId
                        )
                    )
                }

                if (task.isCanceled) {
                    return
                }

                val uploadedMediasFiles = ArrayList<Pair<File, String>>()
                for (albumMediaFile in albumMediasFiles) {
                    if (task.isCanceled) {
                        return
                    }

                    val currentUploadedMediasFilesLength = getTotalFilesLengthInBytes(
                        uploadedMediasFiles.map { it.first }
                    )
                    if (uploadedMediasFiles.count() == MAX_ITEMS_UPLOADS_PER_BATCH
                        || currentUploadedMediasFilesLength > MAX_SIZE_BYTES_TO_TRIGGER_MEDIA_CREATION_BATCH
                    ) {
                        batchCreateAlbumMediaItems(
                            photosLibraryClient,
                            db,
                            photosAlbumId,
                            task,
                            albumName,
                            uploadedMediasFiles,
                            listener
                        )

                        uploadedMediasFiles.clear()
                    }

                    if (task.isCanceled) {
                        return
                    }

                    println("Uploading photo: ${albumMediaFile.absolutePath}")

                    listener.onWillUploadMedia(albumMediaFile)

                    val dbMedia = db.getDbMediaByFile(albumMediaFile)

                    if (task.isCanceled) {
                        return
                    }

                    if (dbMedia != null) {
                        if (dbMedia.isUploadedAndCreated) {
                            println("${albumMediaFile.absolutePath} is already uploaded and created")

                            listener.onMediaUploadResult(true, albumMediaFile)

                            if (task.isCanceled) {
                                return
                            }

                            listener.onMediaItemCreationResult(true, albumMediaFile)

                            continue
                        }

                        val currentTimeMs = System.currentTimeMillis()
                        val mediaFileUploadElapsedTimeMs = currentTimeMs - dbMedia.uploadTokenGenerationTime!!
                        if (dbMedia.uploadToken != null
                            && dbMedia.uploadTokenGenerationTime != null
                            && mediaFileUploadElapsedTimeMs < MAX_TIME_MS_TO_CONSIDER_UPLOAD_TOKE_FROM_DB
                        ) {
                            uploadedMediasFiles.add(albumMediaFile to dbMedia.uploadToken!!)

                            listener.onMediaUploadResult(true, albumMediaFile)

                            continue
                        }
                    }

                    if (task.isCanceled) {
                        return
                    }

                    if (!albumMediaFile.exists()) {
                        Log.w(
                            TAG, "The file [${albumMediaFile.absolutePath}] no longer exists")

                        listener.onMediaUploadResult(false, albumMediaFile)

                        continue
                    }

                    val uploadRequest = UploadMediaItemRequest
                        .newBuilder()
                        .setFileName(albumMediaFile.name)
                        .setDataFile(RandomAccessFile(albumMediaFile, "r"))
                        .build()

                    if (task.isCanceled) {
                        return
                    }

                    val uploadResponse = photosLibraryClient.uploadMediaItem(uploadRequest)
                    if (!uploadResponse.error.isPresent) {
                        println("Photo uploaded: ${albumMediaFile.absolutePath}")

                        val uploadToken = uploadResponse.uploadToken.get()
                        val uploadTokenGenerationTimeMs = System.currentTimeMillis()

                        uploadedMediasFiles.add(albumMediaFile to uploadToken)

                        db.insertOrUpdateMedia(
                            DbMedia(
                                path = db.getRelativeFilePathToDbDir(albumMediaFile),
                                albumName = albumName,
                                isUploadedAndCreated = false,
                                uploadToken = uploadToken,
                                uploadTokenGenerationTime = uploadTokenGenerationTimeMs
                            )
                        )

                        if (task.isCanceled) {
                            return
                        }

                        listener.onMediaUploadResult(true, albumMediaFile)
                    } else {
                        println("Failed to upload the photo: ${albumMediaFile.absolutePath}")

                        if (task.isCanceled) {
                            return
                        }

                        listener.onMediaUploadResult(false, albumMediaFile)
                    }
                }

                if (task.isCanceled) {
                    return
                }

                if (uploadedMediasFiles.isNotEmpty()) {
                    batchCreateAlbumMediaItems(
                        photosLibraryClient,
                        db,
                        photosAlbumId,
                        task,
                        albumName,
                        uploadedMediasFiles,
                        listener
                    )

                    uploadedMediasFiles.clear()
                }

                if (task.isCanceled) {
                    return
                }

                listener.onCompleted()
            } catch (e: Exception) {
                Log.e(TAG, "Error on executing album upload task: \n${e.message}")
            } finally {
                task.removeCancelListener(taskCancelListener)
            }
        }
    }

    fun executeAlbumsUploadTask(
        photosLibraryClient: PhotosLibraryClient,
        db: AppDatabase,
        task: AlbumsUploadTask,
        listener: AlbumsUploadTaskListener
    ) {
        var currentAlbumUploadTask: AlbumUploadTask? = null

        val taskCancelListener = object :
            MediaUploadTask.CancelListener {
            override fun onCanceled() {
                currentAlbumUploadTask?.cancel()

                listener.onCanceled()
            }
        }

        task.addCancelListener(taskCancelListener)

        try {
            if (task.isCanceled) {
                return
            }

            val albumsDirs = task.albumsDirs.toList()
            for (albumDir in albumsDirs) {
                if (task.isCanceled) {
                    return
                }

                listener.onWillUploadAlbum(albumDir)

                if (task.isCanceled) {
                    return
                }

                val albumUploadTask = createAlbumUploadTask(albumDir)

                currentAlbumUploadTask = albumUploadTask

                executeAlbumUploadTask(
                    photosLibraryClient,
                    db,
                    albumUploadTask,
                    object :
                        AlbumUploadListener {
                        override fun onWillUploadMedia(mediaFile: File) {
                            if (task.isCanceled) {
                                return
                            }

                            listener.onWWillUploadAlbumMedia(albumDir, mediaFile)
                        }

                        override fun onMediaUploadResult(success: Boolean, mediaFile: File) {
                            if (task.isCanceled) {
                                return
                            }

                            listener.onAlbumMediaUploadResult(success, albumDir, mediaFile)
                        }

                        override fun onWillCreateMediaItems(mediasFiles: List<File>) {
                            if (task.isCanceled) {
                                return
                            }

                            listener.onWillCreateAlbumMediaItems(albumDir, mediasFiles)
                        }

                        override fun onMediaItemCreationResult(success: Boolean, mediaFile: File) {
                            if (task.isCanceled) {
                                return
                            }

                            listener.onAlbumMediaItemCreationResult(success, albumDir, mediaFile)
                        }

                        override fun onError(error: Error) {
                            if (task.isCanceled) {
                                return
                            }

                            listener.onError(error)
                        }

                        override fun onCompleted() {
                            if (task.isCanceled) {
                                return
                            }

                            listener.onAlbumUploadCompleted(albumDir)
                        }

                        override fun onCanceled() {
                            // do nothing
                        }
                    }
                )
            }

            currentAlbumUploadTask = null

            if (task.isCanceled) {
                return
            }

            listener.onCompleted()
        } finally {
            task.removeCancelListener(taskCancelListener)
        }
    }

    companion object {
        private val TAG = MediaUploader::class.java.simpleName

        private const val MAX_ITEMS_UPLOADS_PER_BATCH = 50
        private const val MAX_SIZE_BYTES_TO_TRIGGER_MEDIA_CREATION_BATCH = 100 * 1024 * 1024L // 100 mb
        private const val MAX_TIME_MS_TO_CONSIDER_UPLOAD_TOKE_FROM_DB = 40 * 60 * 1000 // 40 minutes

        private val UPLOAD_TASK_LOCK = ReentrantLock()

        val VALID_IMAGE_EXTENSIONS = arrayOf(
            "MP",
            "GIF",
            "HEIC",
            "ICO",
            "JPG",
            "PNG",
            "TIFF",
            "WEBP"
        )

        val VALID_VIDEO_EXTENSIONS = arrayOf(
            "3GP",
            "3G2",
            "ASF",
            "AVI",
            "DIVX",
            "M2T",
            "M2TS",
            "M4V",
            "MKV",
            "MMV",
            "MOD",
            "MOV",
            "MP4",
            "MPG",
            "MTS",
            "TOD",
            "WMV"
        )

        val ALL_VALID_EXTENSIONS = VALID_IMAGE_EXTENSIONS + VALID_VIDEO_EXTENSIONS
    }
}