package br.com.afadc.folderstoalbumsconvertergooglephotos.views

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def.UploadScreenResBundle
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.GridBagConstraintsBuilder
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.HdTreeSelectionModel
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

class UploadScreen(private val resBundle: UploadScreenResBundle.Bundle) : JPanel(GridBagLayout()) {

    interface Listener {
        fun onBackButtonClicked()
        fun onHdTreeViewSelectedTreePathsChanged(selectedPaths: Array<TreePath>?)
        fun onUploadButtonClicked()
        fun onCancelUploadButtonClicked()
    }

    private val topPanel: JPanel
    private val middlePanel: JPanel
    private val bottomPanel: JPanel


    private val backButton: JButton
    private val userEmailLabel: JLabel
    private val hdTreeView: JTree
    private val hdTreeViewScrollablePane: JScrollPane
    private val uploadLogTextArea: JEditorPane
    private val uploadLogTextAreaScrollablePane: JScrollPane
    private val uploadButton: JButton
    private val cancelUploadButton: JButton

    val hdTreeViewSelectedTreePaths: Array<TreePath>? get() = hdTreeView.selectionPaths

    var listener: Listener? = null

    init {
        topPanel = JPanel(GridBagLayout())
        this.add(
            topPanel,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(0)
                .setIPadY(30)
                .build()
        )

        middlePanel = JPanel(GridBagLayout())
        this.add(
            middlePanel,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(1)
                .setWeightX(1.0)
                .setWeightY(1.0)
                .build()
        )

        bottomPanel = JPanel(GridBagLayout())
        this.add(
            bottomPanel,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(2)
                .setIPadY(30)
                .build()
        )

        backButton = JButton(resBundle.backButton)
        topPanel.add(
            backButton,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(0)
                .setWeightX(0.2)
                .setWeightY(1.0)
                .build()
        )

        userEmailLabel = JLabel(resBundle.emailLabelFormat.format(""))
        topPanel.add(
            userEmailLabel,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(1)
                .setGridY(0)
                .setWeightX(0.8)
                .setWeightY(1.0)
                .setInsets(Insets(0, 16, 0, 0))
                .build()
        )

        hdTreeView = JTree()
        hdTreeView.selectionModel =
            HdTreeSelectionModel()
        hdTreeView.autoscrolls = true
        hdTreeView.model = null

        hdTreeViewScrollablePane = JScrollPane(hdTreeView)
        middlePanel.add(
            hdTreeViewScrollablePane,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(0)
                .setWeightX(1.0)
                .setWeightY(1.0)
                .build()
        )

        uploadLogTextArea = JEditorPane("text/html", "")
        uploadLogTextArea.autoscrolls = true
        uploadLogTextArea.isFocusable = false

        uploadLogTextAreaScrollablePane = JScrollPane(uploadLogTextArea)
        middlePanel.add(
            uploadLogTextAreaScrollablePane,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(0)
                .setWeightX(1.0)
                .setWeightY(1.0)
                .build()
        )

        uploadButton = JButton(resBundle.uploadMediaButtonZeroMediaCount)
        bottomPanel.add(
            uploadButton,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(0)
                .setWeightX(1.0)
                .setWeightY(1.0)
                .build()
        )

        cancelUploadButton = JButton(resBundle.cancelMediaUploadButton)
        bottomPanel.add(
            cancelUploadButton,
            GridBagConstraintsBuilder()
                .setFill(GridBagConstraints.BOTH)
                .setGridX(0)
                .setGridY(0)
                .setWeightX(1.0)
                .setWeightY(1.0)
                .build()
        )

        backButton.addActionListener {
            listener?.onBackButtonClicked()
        }

        hdTreeView.addTreeSelectionListener {
            listener?.onHdTreeViewSelectedTreePathsChanged(hdTreeViewSelectedTreePaths)
        }

        uploadButton.addActionListener {
            listener?.onUploadButtonClicked()
        }

        cancelUploadButton.addActionListener {
            listener?.onCancelUploadButtonClicked()
        }
    }

    fun setHdTreeViewScrollablePaneVisibility(isVisible: Boolean) {
        hdTreeViewScrollablePane.isVisible = isVisible
    }

    fun setUploadLogTextAreaScrollablePaneVisibility(isVisible: Boolean) {
        uploadLogTextAreaScrollablePane.isVisible = isVisible
    }

    fun setUploadButtonVisibility(isVisible: Boolean) {
        uploadButton.isVisible = isVisible
    }

    fun setCancelUploadButtonVisibility(isVisible: Boolean) {
        cancelUploadButton.isVisible = isVisible
    }

    fun setHdTreeViewModel(model: TreeModel) {
        hdTreeView.model = model
    }

    fun setUserEmail(email: String) {
        userEmailLabel.text = resBundle.emailLabelFormat.format(
            email
        )
    }

    fun setUploadLogTextAreaText(text: String) {
        uploadLogTextArea.text = text
    }

    fun setUploadButtonText(text: String) {
        uploadButton.text = text
    }

    fun scrollLogsViewToTheBottom() {
        val verticalScrollBar = hdTreeViewScrollablePane.verticalScrollBar
        verticalScrollBar.value = verticalScrollBar.maximum
    }
}