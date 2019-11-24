package br.com.afadc.folderstoalbumsconvertergooglephotos.controllers

import com.google.photos.library.v1.PhotosLibraryClient
import br.com.afadc.folderstoalbumsconvertergooglephotos.db.AppDatabase
import br.com.afadc.folderstoalbumsconvertergooglephotos.views.AppFrame
import java.io.File
import java.lang.IllegalStateException
import kotlin.system.exitProcess

class AppFrameController {

    private val appFrame = AppFrame()

    private val screenControllersStack = ArrayList<IScreenController>()

    private var photosLibraryClient: PhotosLibraryClient? = null
    private var userEmail: String? = null

    private var albumsDirectoryFile: File? = null
    private var appDb: AppDatabase? = null

    init {
        addScreenControllerToStack(createConnectScreenController())
    }

    private fun showTopScreenController() {
        synchronized(screenControllersStack) {
            if (screenControllersStack.isEmpty()) {
                throw IllegalStateException("There are no screens to be shown")
            }

            screenControllersStack.last().show()
        }
    }

    fun show() {
        synchronized(screenControllersStack) {
            showTopScreenController()

            appFrame.isVisible = true
        }
    }

    fun addScreenControllerToStack(screenController: IScreenController) {
        synchronized(screenControllersStack) {
            screenControllersStack.add(screenController)

            showTopScreenController()
        }
    }

    fun removeScreenControllerFromStack(screenController: IScreenController) {
        synchronized(screenControllersStack) {
            val isTheCurrentShowingScreenController = (screenController == screenControllersStack.lastOrNull())

            screenControllersStack.remove(screenController)

            if (isTheCurrentShowingScreenController) {
                if (screenControllersStack.isEmpty()) {
                    exitProgram()
                }

                showTopScreenController()
            }
        }
    }

    private fun exitProgram() {
        appFrame.isVisible = false
        appFrame.dispose()

        exitProcess(0)
    }

    private fun createConnectScreenController(): ConnectScreenController {
        val connectScreenController = ConnectScreenController(appFrame)
        connectScreenController.listener = object :
            ConnectScreenController.Listener {
            override fun onUserConnected(photosLibraryClient: PhotosLibraryClient, email: String) {
                this@AppFrameController.photosLibraryClient?.shutdown()
                this@AppFrameController.photosLibraryClient = photosLibraryClient
                this@AppFrameController.userEmail = email

                addScreenControllerToStack(createAlbumsDirectorySelectorScreenController())
            }
        }

        return connectScreenController
    }

    private fun createAlbumsDirectorySelectorScreenController(): AlbumsDirectorySelectorScreenController {
        val albumsDirectorySelectorScreenController = AlbumsDirectorySelectorScreenController(appFrame)
        albumsDirectorySelectorScreenController.listener = object :
            AlbumsDirectorySelectorScreenController.Listener {
            override fun onAlbumsDirectorySelected(albumsDir: File) {
                this@AppFrameController.appDb?.close()
                this@AppFrameController.albumsDirectoryFile = albumsDir
                this@AppFrameController.appDb = AppDatabase(albumsDir)

                addScreenControllerToStack(createUploadScreenController())
            }
        }

        return albumsDirectorySelectorScreenController
    }

    private fun createUploadScreenController(): UploadScreenController {
        val photosLibraryClient = photosLibraryClient
        val appDb = appDb
        val albumsDirectoryFile = albumsDirectoryFile
        val userEmail = userEmail

        if (photosLibraryClient == null) {
            throw IllegalStateException("photosLibraryClient is null")
        }

        if (appDb == null) {
            throw IllegalStateException("appDb is null")
        }

        if (albumsDirectoryFile == null) {
            throw IllegalStateException("albumsDirectoryFile is null")
        }

        if (userEmail?.isEmpty() != false) {
            throw IllegalStateException("user email is null or empty")
        }

        val uploadScreenController = UploadScreenController(
                appFrame = appFrame,
                photosLibraryClient = photosLibraryClient,
                appDatabase = appDb,
                userEmail = userEmail,
                albumsDirectoryFile = albumsDirectoryFile
            )

        uploadScreenController.listener = object :
            UploadScreenController.Listener {
            override fun onDismissRequested() {
                removeScreenControllerFromStack(uploadScreenController)
            }
        }

        return uploadScreenController
    }

    companion object {
        private val TAG = AppFrameController::class.java.simpleName
    }
}