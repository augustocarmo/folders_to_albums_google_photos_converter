package br.com.afadc.folderstoalbumsconvertergooglephotos.views

import javax.swing.JFrame
import javax.swing.WindowConstants

class AppFrame : JFrame("Folders to Albums Google Photos Converter") {

    init {
        this.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        this.setSize(800, 600)
        this.minimumSize = this.size
        this.setLocationRelativeTo(null)
    }
}