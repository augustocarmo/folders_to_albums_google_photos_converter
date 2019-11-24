package br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.AlbumsDirectoryScreenResBundleTemplate
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.values.IAlbumsDirectoryScreenResBundleValues
import java.util.*

class AlbumsDirectoryScreenResBundle : AlbumsDirectoryScreenResBundleTemplate() {

    class Bundle(private val rb: ResourceBundle) :
        IAlbumsDirectoryScreenResBundleValues {
        override val selectTheAlbumsDirectoryButton: String = rb.getString(SELECT_THE_ALBUMS_DIRECTORY_BUTTON_KEY)
        override val selectTheAlbumsDirectoryLabel: String = rb.getString(SELECT_THE_ALBUMS_DIRECTORY_LABEL_KEY)
        override val selectTheAlbumsDirectoryTitle: String = rb.getString(SELECT_THE_ALBUMS_DIRECTORY_TITLE_KEY)
        override val selectTheAlbumsDirectoryFileChooserTitle: String = rb.getString(
            SELECT_THE_ALBUMS_DIRECTORY_FILE_CHOOSER_TITLE_KEY
        )
    }

    override val selectTheAlbumsDirectoryButton: String
        get() = "Select"
    override val selectTheAlbumsDirectoryLabel: String
        get() = "Directory"
    override val selectTheAlbumsDirectoryTitle: String
        get() = "Select the Directory With the Albums Folders"
    override val selectTheAlbumsDirectoryFileChooserTitle: String
        get() = "Select the directory with the albums folders"
}