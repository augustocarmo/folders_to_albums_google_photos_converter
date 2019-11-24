package br.com.afadc.folderstoalbumsconvertergooglephotos.controllers

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.AppResBundles
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.Log
import br.com.afadc.folderstoalbumsconvertergooglephotos.views.AppFrame
import br.com.afadc.folderstoalbumsconvertergooglephotos.views.AlbumsDirectorySelectorScreen
import java.io.File
import java.lang.RuntimeException
import javax.swing.JFileChooser

class AlbumsDirectorySelectorScreenController(private val appFrame: AppFrame) : IScreenController {

    interface Listener {
        fun onAlbumsDirectorySelected(albumsDir: File)
    }

    private val resBundle = AppResBundles.getAlbumsDirectoryScreenBundle()

    private val albumsDirectorySelectorScreen = AlbumsDirectorySelectorScreen(resBundle)

    var listener: Listener? = null

    init {
        albumsDirectorySelectorScreen.listener = object : AlbumsDirectorySelectorScreen.Listener {
            override fun onSelectButtonClicked() {
                selectAlbumsDirectory()
            }
        }
    }

    override fun show() {
        appFrame.contentPane.removeAll()
        appFrame.contentPane.add(albumsDirectorySelectorScreen)

        appFrame.validate()
        appFrame.repaint()
    }

    fun selectAlbumsDirectory() {
        val chosenFile = chooseAlbumsDirectoryFile()
        if (chosenFile == null) {
            Log.i(TAG, "no albums directory file was chosen")

            return
        }

        if (!chosenFile.isDirectory) {
            throw RuntimeException("The chosen file [$chosenFile] is not a directory")
        }

        Log.i(TAG, "The albums directory was chosen: [$chosenFile]")

        listener?.onAlbumsDirectorySelected(chosenFile)
    }

    private fun chooseAlbumsDirectoryFile(): File? {
        val jfc = JFileChooser()
        jfc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        jfc.dialogTitle = resBundle.selectTheAlbumsDirectoryFileChooserTitle
        val returnValue = jfc.showOpenDialog(null)
        // int returnValue = jfc.showSaveDialog(null);

        return if (returnValue == JFileChooser.APPROVE_OPTION) {
            jfc.selectedFile
        } else {
            null
        }
    }

    companion object {
        private val TAG = AlbumsDirectorySelectorScreenController::class.java.simpleName
    }
}