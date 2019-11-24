package br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates

import br.com.afadc.folderstoalbumsconvertergooglephotos.extensions.kv
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.values.IAlbumsDirectoryScreenResBundleValues
import java.util.*

abstract class AlbumsDirectoryScreenResBundleTemplate:
    ListResourceBundle(),
    IAlbumsDirectoryScreenResBundleValues {

    final override fun getContents() = arrayOf(
        kv(SELECT_THE_ALBUMS_DIRECTORY_BUTTON_KEY, selectTheAlbumsDirectoryButton),
        kv(SELECT_THE_ALBUMS_DIRECTORY_LABEL_KEY, selectTheAlbumsDirectoryLabel),
        kv(SELECT_THE_ALBUMS_DIRECTORY_TITLE_KEY, selectTheAlbumsDirectoryTitle),
        kv(SELECT_THE_ALBUMS_DIRECTORY_FILE_CHOOSER_TITLE_KEY, selectTheAlbumsDirectoryFileChooserTitle)
    )

    companion object {
        const val SELECT_THE_ALBUMS_DIRECTORY_BUTTON_KEY = "select_the_albums_directory_button"
        const val SELECT_THE_ALBUMS_DIRECTORY_LABEL_KEY = "select_the_albums_directory_label"
        const val SELECT_THE_ALBUMS_DIRECTORY_TITLE_KEY = "select_the_albums_directory_title"
        const val SELECT_THE_ALBUMS_DIRECTORY_FILE_CHOOSER_TITLE_KEY = "select_the_albums_directory_file_chooser_title"
    }
}