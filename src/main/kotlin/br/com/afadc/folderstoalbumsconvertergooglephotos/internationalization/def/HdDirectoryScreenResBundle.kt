package br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.HdDirectoryScreenResBundleTemplate
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.values.IHdDirectoryScreenResBundleValues
import java.util.*

class HdDirectoryScreenResBundle : HdDirectoryScreenResBundleTemplate() {

    class Bundle(private val rb: ResourceBundle) :
        IHdDirectoryScreenResBundleValues {
        override val selectTheHdDirectoryButton: String = rb.getString(SELECT_THE_HD_DIRECTORY_BUTTON_KEY)
        override val selectTheHdDirectoryLabel: String = rb.getString(SELECT_THE_HD_DIRECTORY_LABEL_KEY)
        override val selectTheHdDirectoryTitle: String = rb.getString(SELECT_THE_HD_DIRECTORY_TITLE_KEY)
        override val selectTheHdDirectoryFileChooserTitle: String = rb.getString(
            SELECT_THE_HD_DIRECTORY_FILE_CHOOSER_TITLE_KEY
        )
    }

    override val selectTheHdDirectoryButton: String
        get() = "Select"
    override val selectTheHdDirectoryLabel: String
        get() = "Directory"
    override val selectTheHdDirectoryTitle: String
        get() = "Select the HD directory"
    override val selectTheHdDirectoryFileChooserTitle: String
        get() = "Select the HD directory"
}