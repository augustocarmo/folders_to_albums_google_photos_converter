package br.com.afadc.folderstoalbumsconvertergooglephotos.views

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def.AlbumsDirectoryScreenResBundle
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.GridBagConstraintsBuilder
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.MouseListenerAdapter
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class AlbumsDirectorySelectorScreen(resBundle: AlbumsDirectoryScreenResBundle.Bundle) : JPanel(GridBagLayout()) {

    interface Listener {
        fun onSelectButtonClicked()
    }

    private val titleLabel: JLabel
    private val selectAlbumsDirectoryLabel: JLabel
    private val selectAlbumsDirectoryTextField: JTextField
    private val selectAlbumsDirectoryButton: JButton

    var listener: Listener? = null

    init {
        titleLabel = JLabel(resBundle.selectTheAlbumsDirectoryTitle)
        titleLabel.font = Font(titleLabel.font.name, Font.PLAIN, 26)
        this.add(
            titleLabel,
            GridBagConstraintsBuilder()
                .setGridX(0)
                .setGridY(0)
                .setGridWidth(3)
                .setInsets(Insets(0, 0, 40, 0))
                .build()
        )

        selectAlbumsDirectoryLabel = JLabel(resBundle.selectTheAlbumsDirectoryLabel)
        this.add(
            selectAlbumsDirectoryLabel,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(1)
                .build()
        )

        selectAlbumsDirectoryTextField = JTextField(28)
        selectAlbumsDirectoryTextField.isFocusable = false
        this.add(
            selectAlbumsDirectoryTextField,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(1)
                .setGridY(1)
                .setInsets(Insets(0, 8, 0, 8))
                .build()
        )

        selectAlbumsDirectoryButton = JButton(resBundle.selectTheAlbumsDirectoryButton)
        this.add(
            selectAlbumsDirectoryButton,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(2)
                .setGridY(1)
                .build()
        )

        selectAlbumsDirectoryTextField.addMouseListener(
            object : MouseListenerAdapter() {
                override fun mouseClicked(event: MouseEvent?) {
                    listener?.onSelectButtonClicked()
                }
            }
        )

        selectAlbumsDirectoryButton.addActionListener {
            listener?.onSelectButtonClicked()
        }
    }
}