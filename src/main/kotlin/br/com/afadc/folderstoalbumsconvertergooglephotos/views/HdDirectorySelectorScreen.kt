package br.com.afadc.folderstoalbumsconvertergooglephotos.views

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def.HdDirectoryScreenResBundle
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

class HdDirectorySelectorScreen(resBundle: HdDirectoryScreenResBundle.Bundle) : JPanel(GridBagLayout()) {

    interface Listener {
        fun onSelectButtonClicked()
    }

    private val titleLabel: JLabel
    private val selectHdDirectoryLabel: JLabel
    private val selectHdDirectoryTextField: JTextField
    private val selectButton: JButton

    var listener: Listener? = null

    init {
        titleLabel = JLabel(resBundle.selectTheHdDirectoryTitle)
        titleLabel.font = Font(titleLabel.font.name, Font.PLAIN, 30)
        this.add(
            titleLabel,
            GridBagConstraintsBuilder()
                .setGridX(0)
                .setGridY(0)
                .setGridWidth(3)
                .setInsets(Insets(0, 0, 40, 0))
                .build()
        )

        selectHdDirectoryLabel = JLabel(resBundle.selectTheHdDirectoryLabel)
        this.add(
            selectHdDirectoryLabel,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(1)
                .build()
        )

        selectHdDirectoryTextField = JTextField(20)
        selectHdDirectoryTextField.isFocusable = false
        this.add(
            selectHdDirectoryTextField,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(1)
                .setGridY(1)
                .setInsets(Insets(0, 8, 0, 8))
                .build()
        )

        selectButton = JButton(resBundle.selectTheHdDirectoryButton)
        this.add(
            selectButton,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(2)
                .setGridY(1)
                .build()
        )

        selectHdDirectoryTextField.addMouseListener(
            object : MouseListenerAdapter() {
                override fun mouseClicked(event: MouseEvent?) {
                    listener?.onSelectButtonClicked()
                }
            }
        )

        selectButton.addActionListener {
            listener?.onSelectButtonClicked()
        }
    }
}