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

    private var hdDirectoryFile: File? = null
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
        synchronized(screenControllersStack){
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
        val connectScreenController =
            ConnectScreenController(
                appFrame
            )
        connectScreenController.listener = object :
            ConnectScreenController.Listener {
            override fun onUserConnected(photosLibraryClient: PhotosLibraryClient, email: String) {
                this@AppFrameController.photosLibraryClient?.shutdown()
                this@AppFrameController.photosLibraryClient = photosLibraryClient
                this@AppFrameController.userEmail = email

                addScreenControllerToStack(createHdDirectorySelectorScreenController())
            }
        }

        return connectScreenController
    }

    private fun createHdDirectorySelectorScreenController(): HdDirectorySelectorScreenController {
        val hdDirectorySelectorScreenController =
            HdDirectorySelectorScreenController(
                appFrame
            )
        hdDirectorySelectorScreenController.listener = object :
            HdDirectorySelectorScreenController.Listener {
            override fun onHdDirectorySelected(hdDir: File) {
                this@AppFrameController.appDb?.close()
                this@AppFrameController.hdDirectoryFile = hdDir
                this@AppFrameController.appDb =
                    AppDatabase(hdDir)

                addScreenControllerToStack(createUploadScreenController())
            }
        }

        return hdDirectorySelectorScreenController
    }

    private fun createUploadScreenController(): UploadScreenController {
        val photosLibraryClient = photosLibraryClient
        val appDb = appDb
        val hdDirectoryFile = hdDirectoryFile
        val userEmail = userEmail

        if (photosLibraryClient == null) {
            throw IllegalStateException("photosLibraryClient is null")
        }

        if (appDb == null) {
            throw IllegalStateException("appDb is null")
        }

        if (hdDirectoryFile == null) {
            throw IllegalStateException("hdDirectoryFile is null")
        }

        if (userEmail?.isEmpty() != false) {
            throw IllegalStateException("user email is null or empty")
        }

        val uploadScreenController =
            UploadScreenController(
                appFrame = appFrame,
                photosLibraryClient = photosLibraryClient,
                appDatabase = appDb,
                userEmail = userEmail,
                hdDirectoryFile = hdDirectoryFile
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