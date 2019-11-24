package br.com.afadc.folderstoalbumsconvertergooglephotos.controllers

import br.com.afadc.folderstoalbumsconvertergooglephotos.PhotosUtils.PhotosLibraryClientFactory
import com.google.photos.library.v1.PhotosLibraryClient
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.AppResBundles
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.EmailValidator
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.Log
import br.com.afadc.folderstoalbumsconvertergooglephotos.views.AppFrame
import br.com.afadc.folderstoalbumsconvertergooglephotos.views.ConnectScreen
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane

class ConnectScreenController(private val appFrame: AppFrame) :
    IScreenController {

    interface Listener {
        fun onUserConnected(photosLibraryClient: PhotosLibraryClient, email: String)
    }

    private val resBundle = AppResBundles.getConnectScreenBundle()

    private val connectScreen = ConnectScreen(resBundle)

    private val emailValidator by lazy { EmailValidator() }

    var listener: Listener? = null

    init {
        connectScreen.listener = object : ConnectScreen.Listener {
            override fun onConnectButtonClicked() {
                connect(connectScreen.emailTxt)
            }
        }
    }

    override fun show() {
        appFrame.contentPane.removeAll()
        appFrame.contentPane.add(connectScreen)

        appFrame.validate()
        appFrame.repaint()
    }

    private fun connect(email: String) {
        if (!emailValidator.isValid(email)) {
            Log.i(TAG, "the email [$email] is invalid")

            JOptionPane.showMessageDialog(
                appFrame,
                resBundle.invalidEmailPopUpMessage,
                resBundle.invalidEmailPopUpTitle,
                JOptionPane.WARNING_MESSAGE
            )

            return
        }

        val chosenFile = chooseCredentialFile()
        if (chosenFile == null) {
            Log.i(TAG, "no credential file was chosen to connect the user")

            return
        }

        try {
            val photosLibraryClient = PhotosLibraryClientFactory()
                .createClient(chosenFile.absolutePath, email)

            Log.i(TAG, "the user under email [$email] was connected")

            listener?.onUserConnected(photosLibraryClient, email)
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                appFrame,
                resBundle.wrongCredentialsFilePopUpMessage,
                resBundle.wrongCredentialsFilePopUpTitle,
                JOptionPane.WARNING_MESSAGE
            )
        }
    }

    private fun chooseCredentialFile(): File? {
        val jfc = JFileChooser()
        jfc.fileSelectionMode = JFileChooser.FILES_ONLY
        jfc.dialogTitle = resBundle.selectCredentialsFileFileChooserTitle
        val resultValue = jfc.showOpenDialog(null)

        return if (resultValue == JFileChooser.APPROVE_OPTION) {
            jfc.selectedFile
        } else {
            null
        }
    }

    companion object {
        private val TAG = ConnectScreenController::class.java.simpleName
    }
}