package br.com.afadc.folderstoalbumsconvertergooglephotos.views

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def.ConnectScreenResBundle
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.GridBagConstraintsBuilder
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class ConnectScreen(resBundle: ConnectScreenResBundle.Bundle) : JPanel(GridBagLayout()) {

    interface Listener {
        fun onConnectButtonClicked()
    }

    private val titleLabel: JLabel
    private val emailLabel: JLabel
    private val emailTextField: JTextField
    private val connectButton: JButton

    val emailTxt: String get() = emailTextField.text

    var listener: Listener? = null

    init {
        titleLabel = JLabel(
            resBundle.selectCredentialsFileTitle,
            SwingConstants.CENTER
        )
        titleLabel.font = Font(titleLabel.font.name, Font.PLAIN, 35)
        this.add(
            titleLabel,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(0)
                .setGridWidth(3)
                .setInsets(Insets(0, 0, 40, 0))
                .build()
        )

        emailLabel = JLabel(resBundle.emailLabel)
        this.add(
            emailLabel,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(1)
                .build()
        )

        emailTextField = JTextField(20)
        this.add(
            emailTextField,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(1)
                .setGridY(1)
                .setInsets(Insets(0, 8, 0, 8))
                .build()
        )

        connectButton = JButton(resBundle.selectCredentialsFileButton)
        this.add(
            connectButton,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(2)
                .setGridY(1)
                .build()
        )

        emailTextField.addActionListener {
            connectButton.doClick()
        }

        connectButton.addActionListener {
            listener?.onConnectButtonClicked()
        }
    }
}