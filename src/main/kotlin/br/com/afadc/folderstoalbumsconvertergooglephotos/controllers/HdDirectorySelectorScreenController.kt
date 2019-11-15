package br.com.afadc.folderstoalbumsconvertergooglephotos.controllers

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.AppResBundles
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.Log
import br.com.afadc.folderstoalbumsconvertergooglephotos.views.AppFrame
import br.com.afadc.folderstoalbumsconvertergooglephotos.views.HdDirectorySelectorScreen
import java.io.File
import java.lang.RuntimeException
import javax.swing.JFileChooser

class HdDirectorySelectorScreenController(private val appFrame: AppFrame):
    IScreenController {

    interface Listener {
        fun onHdDirectorySelected(hdDir: File)
    }

    private val resBundle = AppResBundles.getHdDirectoryScreenBundle()

    private val hdDirectorySelectorScreen =
        HdDirectorySelectorScreen(resBundle)

    var listener: Listener? = null

    init {
        hdDirectorySelectorScreen.listener = object : HdDirectorySelectorScreen.Listener {
            override fun onSelectButtonClicked() {
                selectHdDirectory()
            }
        }
    }

    override fun show() {
        appFrame.contentPane.removeAll()
        appFrame.contentPane.add(hdDirectorySelectorScreen)

        appFrame.validate()
        appFrame.repaint()
    }

    fun selectHdDirectory() {
        val chosenFile = chooseHdDirectoryFile()
        if (chosenFile == null) {
            Log.i(TAG, "no hd directory file was chosen")

            return
        }

        if (!chosenFile.isDirectory) {
            throw RuntimeException("The chosen file [$chosenFile] is not a directory")
        }

        Log.i(TAG, "The Hd directory was chosen: [$chosenFile]")

        listener?.onHdDirectorySelected(chosenFile)
    }

    private fun chooseHdDirectoryFile(): File? {
        val jfc = JFileChooser()
        jfc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        jfc.dialogTitle = resBundle.selectTheHdDirectoryFileChooserTitle
        val returnValue = jfc.showOpenDialog(null)
        // int returnValue = jfc.showSaveDialog(null);

        return if (returnValue == JFileChooser.APPROVE_OPTION) {
            jfc.selectedFile
        } else {
            null
        }
    }

    companion object {
        private val TAG = HdDirectorySelectorScreenController::class.java.simpleName
    }
}