package br.com.afadc.folderstoalbumsconvertergooglephotos.views

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def.ConnectScreenResBundle
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.GridBagConstraintsBuilder
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.Log
import java.awt.*
import javax.swing.*
import javax.swing.event.HyperlinkEvent


class ConnectScreen(resBundle: ConnectScreenResBundle.Bundle) : JPanel(GridBagLayout()) {

    interface Listener {
        fun onConnectButtonClicked()
    }

    private val topPanel: JPanel
    private val bottomPanel: JPanel
    private val titleLabel: JLabel
    private val emailLabel: JLabel
    private val emailTextField: JTextField
    private val connectButton: JButton
    private val credentialsFileInfoTextPane: JTextPane

    val emailTxt: String get() = emailTextField.text

    var listener: Listener? = null

    init {
        topPanel = JPanel(GridBagLayout())
        this.add(
            topPanel,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(0)
                .setWeightX(1.0)
                .setWeightY(1.0)
                .build()
        )

        bottomPanel = JPanel(GridBagLayout())
        this.add(
            bottomPanel,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.HORIZONTAL)
                .setGridX(0)
                .setGridY(1)
                .setWeightX(1.0)
                .setIPadY(60)
                .build()
        )

        titleLabel = JLabel(
            resBundle.selectCredentialsFileTitle,
            SwingConstants.CENTER
        )
        titleLabel.font = Font(titleLabel.font.name, Font.PLAIN, 35)
        topPanel.add(
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
        topPanel.add(
            emailLabel,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(1)
                .build()
        )

        emailTextField = JTextField(20)
        topPanel.add(
            emailTextField,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(1)
                .setGridY(1)
                .setInsets(Insets(0, 8, 0, 8))
                .build()
        )

        connectButton = JButton(resBundle.selectCredentialsFileButton)
        topPanel.add(
            connectButton,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(2)
                .setGridY(1)
                .build()
        )

        credentialsFileInfoTextPane = JTextPane()
        credentialsFileInfoTextPane.contentType = "text/html"
        credentialsFileInfoTextPane.isEnabled = true
        credentialsFileInfoTextPane.isEditable = false
        credentialsFileInfoTextPane.background = null
        credentialsFileInfoTextPane.border = null
        credentialsFileInfoTextPane.text = "<html><div style=\"width:400px;\"><p style=\"text-align: justify;\">${resBundle.credentialsInstructions}</p></div></html>"
        bottomPanel.add(credentialsFileInfoTextPane)

        emailTextField.addActionListener {
            connectButton.doClick()
        }

        connectButton.addActionListener {
            listener?.onConnectButtonClicked()
        }

        credentialsFileInfoTextPane.addHyperlinkListener { hle ->
            if (HyperlinkEvent.EventType.ACTIVATED == hle.eventType) {
                Log.i(TAG, "opening ${hle.url} on browser")

                val desktop = Desktop.getDesktop()
                try {
                    desktop.browse(hle.url.toURI())
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    companion object {
        private val TAG = ConnectScreen::class.java.simpleName
    }
}