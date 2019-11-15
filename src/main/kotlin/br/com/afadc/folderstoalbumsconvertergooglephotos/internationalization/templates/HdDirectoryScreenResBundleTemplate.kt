package br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates

import br.com.afadc.folderstoalbumsconvertergooglephotos.extensions.kv
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.values.IHdDirectoryScreenResBundleValues
import java.util.*

abstract class HdDirectoryScreenResBundleTemplate: ListResourceBundle(),
    IHdDirectoryScreenResBundleValues {

    final override fun getContents() = arrayOf(
        kv(SELECT_THE_HD_DIRECTORY_BUTTON_KEY, selectTheHdDirectoryButton),
        kv(SELECT_THE_HD_DIRECTORY_LABEL_KEY, selectTheHdDirectoryLabel),
        kv(SELECT_THE_HD_DIRECTORY_TITLE_KEY, selectTheHdDirectoryTitle),
        kv(SELECT_THE_HD_DIRECTORY_FILE_CHOOSER_TITLE_KEY, selectTheHdDirectoryFileChooserTitle)
    )

    companion object {
        const val SELECT_THE_HD_DIRECTORY_BUTTON_KEY = "select_the_hd_directory_button"
        const val SELECT_THE_HD_DIRECTORY_LABEL_KEY = "select_the_hd_directory_label"
        const val SELECT_THE_HD_DIRECTORY_TITLE_KEY = "select_the_hd_directory_title"
        const val SELECT_THE_HD_DIRECTORY_FILE_CHOOSER_TITLE_KEY = "select_the_hd_directory_file_chooser_title"
    }
}