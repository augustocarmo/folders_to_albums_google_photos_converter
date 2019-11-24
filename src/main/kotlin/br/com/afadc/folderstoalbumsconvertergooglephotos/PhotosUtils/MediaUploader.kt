package br.com.afadc.folderstoalbumsconvertergooglephotos.PhotosUtils

import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.library.v1.upload.UploadMediaItemRequest
import com.google.photos.library.v1.util.NewMediaItemFactory
import com.google.rpc.Code
import br.com.afadc.folderstoalbumsconvertergooglephotos.db.AppDatabase
import br.com.afadc.folderstoalbumsconvertergooglephotos.db.entities.DbAlbum
import br.com.afadc.folderstoalbumsconvertergooglephotos.db.entities.DbMedia
import br.com.afadc.folderstoalbumsconvertergooglephotos.entities.Album
import br.com.afadc.folderstoalbumsconvertergooglephotos.entities.Media
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.Log
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
        fun onWillUploadMedia(media: Media)
        fun onMediaUploadResult(success: Boolean, media: Media)
        fun onWillCreateMediaItems(medias: List<Media>)
        fun onMediaItemCreationResult(success: Boolean, media: Media)
        fun onError(error: Error)
        fun onCompleted()
        fun onCanceled()
    }

    interface AlbumsUploadTaskListener {
        fun onWillUploadAlbum(album: Album)
        fun onWWillUploadAlbumMedia(album: Album, media: Media)
        fun onAlbumMediaUploadResult(success: Boolean, album: Album, media: Media)
        fun onWillCreateAlbumMediaItems(album: Album, medias: List<Media>)
        fun onAlbumMediaItemCreationResult(success: Boolean, album: Album, media: Media)
        fun onAlbumUploadCompleted(album: Album)
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

    private class AlbumUploadTask(val album: Album) : MediaUploadTask()

    class AlbumsUploadTask(val albums: List<Album>) : MediaUploadTask()

    private fun createAlbumUploadTask(album: Album) = AlbumUploadTask(album)

    fun createAlbumsUploadTask(albums: List<Album>) = AlbumsUploadTask(albums)

    private fun getTotalFilesLengthInBytes(medias: List<Media>): Long {
        if (medias.isEmpty()) {
            return 0L
        }

        return medias.fold(0L) { acc, media ->
            acc + media.file.length()
        }
    }

    private fun canPhotosLibraryClientBeUsed(photosLibraryClient: PhotosLibraryClient): Boolean {
        return !photosLibraryClient.isTerminated && !photosLibraryClient.isShutdown
    }

    private fun batchCreateAlbumMediaItems(
        photosLibraryClient: PhotosLibraryClient,
        db: AppDatabase,
        photosAlbumId: String,
        albumUploadTask: AlbumUploadTask,
        album: Album,
        uploadMediasAndTokens: List<Pair<Media, String>>,
        albumUploadListener: AlbumUploadListener
    ) {
        if (albumUploadTask.isCanceled) {
            return
        }

        if (uploadMediasAndTokens.count() > MAX_ITEMS_UPLOADS_PER_BATCH) {
            throw IllegalArgumentException("mediaUploadTokensList has a size [${uploadMediasAndTokens.count()}] greater than the limit [$MAX_ITEMS_UPLOADS_PER_BATCH]")
        }


        albumUploadListener.onWillCreateMediaItems(
            uploadMediasAndTokens.map { it.first }
        )

        val mediaItemsUploadTokens = uploadMediasAndTokens.map {
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
            val media = uploadMediasAndTokens.first { it.second == itemResponse.uploadToken }.first
            if (itemResponse.status.code == Code.OK_VALUE) {
                db.insertOrUpdateMedia(
                    DbMedia(
                        path = db.getRelativeFilePathToDbDir(media.file),
                        albumName = album.name,
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
            val mediaFile = uploadMediasAndTokens.first { it.second == itemResponse.uploadToken }.first

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

                val album = task.album
                val albumMedias = album.medias
                if (albumMedias.isEmpty()) {
                    listener.onCompleted()
                }

                val photosAlbumId: String

                val dbAlbum = db.getDbAlbumByDir(album.directory)

                if (task.isCanceled) {
                    return
                }

                val albumName = album.name

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

                val uploadedMedias = ArrayList<Pair<Media, String>>()
                for (albumMedia in albumMedias) {
                    if (task.isCanceled) {
                        return
                    }

                    val currentUploadedMediasFilesLength = getTotalFilesLengthInBytes(
                        uploadedMedias.map { it.first }
                    )
                    if (uploadedMedias.count() == MAX_ITEMS_UPLOADS_PER_BATCH
                        || currentUploadedMediasFilesLength > MAX_SIZE_BYTES_TO_TRIGGER_MEDIA_CREATION_BATCH
                    ) {
                        batchCreateAlbumMediaItems(
                            photosLibraryClient,
                            db,
                            photosAlbumId,
                            task,
                            album,
                            uploadedMedias,
                            listener
                        )

                        uploadedMedias.clear()
                    }

                    if (task.isCanceled) {
                        return
                    }

                    println("Uploading photo: ${albumMedia.file.absolutePath}")

                    listener.onWillUploadMedia(albumMedia)

                    val dbMedia = db.getDbMediaByFile(albumMedia.file)

                    if (task.isCanceled) {
                        return
                    }

                    if (dbMedia != null) {
                        if (dbMedia.isUploadedAndCreated) {
                            println("${albumMedia.file.absolutePath} is already uploaded and created")

                            listener.onMediaUploadResult(true, albumMedia)

                            if (task.isCanceled) {
                                return
                            }

                            listener.onMediaItemCreationResult(true, albumMedia)

                            continue
                        }

                        val currentTimeMs = System.currentTimeMillis()
                        val mediaFileUploadElapsedTimeMs = currentTimeMs - dbMedia.uploadTokenGenerationTime!!
                        if (dbMedia.uploadToken != null
                            && dbMedia.uploadTokenGenerationTime != null
                            && mediaFileUploadElapsedTimeMs < MAX_TIME_MS_TO_CONSIDER_UPLOAD_TOKE_FROM_DB
                        ) {
                            uploadedMedias.add(albumMedia to dbMedia.uploadToken!!)

                            listener.onMediaUploadResult(true, albumMedia)

                            continue
                        }
                    }

                    if (task.isCanceled) {
                        return
                    }

                    if (!albumMedia.file.exists()) {
                        Log.w(
                            TAG, "The file [${albumMedia.file.absolutePath}] no longer exists")

                        listener.onMediaUploadResult(false, albumMedia)

                        continue
                    }

                    val uploadRequest = UploadMediaItemRequest
                        .newBuilder()
                        .setFileName(albumMedia.name)
                        .setDataFile(RandomAccessFile(albumMedia.file, "r"))
                        .build()

                    if (task.isCanceled) {
                        return
                    }

                    val uploadResponse = photosLibraryClient.uploadMediaItem(uploadRequest)
                    if (!uploadResponse.error.isPresent) {
                        println("Photo uploaded: ${albumMedia.file.absolutePath}")

                        val uploadToken = uploadResponse.uploadToken.get()
                        val uploadTokenGenerationTimeMs = System.currentTimeMillis()

                        uploadedMedias.add(albumMedia to uploadToken)

                        db.insertOrUpdateMedia(
                            DbMedia(
                                path = db.getRelativeFilePathToDbDir(albumMedia.file),
                                albumName = albumName,
                                isUploadedAndCreated = false,
                                uploadToken = uploadToken,
                                uploadTokenGenerationTime = uploadTokenGenerationTimeMs
                            )
                        )

                        if (task.isCanceled) {
                            return
                        }

                        listener.onMediaUploadResult(true, albumMedia)
                    } else {
                        println("Failed to upload the photo: ${albumMedia.file.absolutePath}")

                        if (task.isCanceled) {
                            return
                        }

                        listener.onMediaUploadResult(false, albumMedia)
                    }
                }

                if (task.isCanceled) {
                    return
                }

                if (uploadedMedias.isNotEmpty()) {
                    batchCreateAlbumMediaItems(
                        photosLibraryClient,
                        db,
                        photosAlbumId,
                        task,
                        album,
                        uploadedMedias,
                        listener
                    )

                    uploadedMedias.clear()
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

            val albums = task.albums.toList()
            for (album in albums) {
                if (task.isCanceled) {
                    return
                }

                listener.onWillUploadAlbum(album)

                if (task.isCanceled) {
                    return
                }

                val albumUploadTask = createAlbumUploadTask(album)

                currentAlbumUploadTask = albumUploadTask

                executeAlbumUploadTask(
                    photosLibraryClient,
                    db,
                    albumUploadTask,
                    object :
                        AlbumUploadListener {
                        override fun onWillUploadMedia(media: Media) {
                            if (task.isCanceled) {
                                return
                            }

                            listener.onWWillUploadAlbumMedia(album, media)
                        }

                        override fun onMediaUploadResult(success: Boolean, media: Media) {
                            if (task.isCanceled) {
                                return
                            }

                            listener.onAlbumMediaUploadResult(success, album, media)
                        }

                        override fun onWillCreateMediaItems(medias: List<Media>) {
                            if (task.isCanceled) {
                                return
                            }

                            listener.onWillCreateAlbumMediaItems(album, medias)
                        }

                        override fun onMediaItemCreationResult(success: Boolean, media: Media) {
                            if (task.isCanceled) {
                                return
                            }

                            listener.onAlbumMediaItemCreationResult(success, album, media)
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

                            listener.onAlbumUploadCompleted(album)
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