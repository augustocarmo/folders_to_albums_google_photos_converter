package br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates

import br.com.afadc.folderstoalbumsconvertergooglephotos.extensions.kv
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.values.IConnectScreenResBundleValues
import java.util.*

abstract class ConnectScreenResBundleTemplate : ListResourceBundle(),
    IConnectScreenResBundleValues {

    final override fun getContents() = arrayOf(
        kv(EMAIL_LABEL_KEY, emailLabel),
        kv(INVALID_EMAIL_POP_UP_MESSAGE_KEY, invalidEmailPopUpMessage),
        kv(INVALID_EMAIL_POP_UP_TITLE_KEY, invalidEmailPopUpTitle),
        kv(SELECT_CREDENTIALS_FILE_TITLE_KEY, selectCredentialsFileTitle),
        kv(SELECT_CREDENTIALS_FILE_BUTTON_KEY, selectCredentialsFileButton),
        kv(SELECT_CREDENTIALS_FILE_FILE_CHOOSER_TITLE_KEY, selectCredentialsFileFileChooserTitle),
        kv(WRONG_CREDENTIALS_FILE_POP_UP_MESSAGE_KEY, wrongCredentialsFilePopUpMessage),
        kv(WRONG_CREDENTIALS_FILE_POP_UP_TITLE_KEY, wrongCredentialsFilePopUpTitle)
    )

    companion object {
        const val EMAIL_LABEL_KEY = "email_label"
        const val INVALID_EMAIL_POP_UP_MESSAGE_KEY = "invalid_email_pop_up_message"
        const val INVALID_EMAIL_POP_UP_TITLE_KEY = "invalid_email_pop_up_title"
        const val SELECT_CREDENTIALS_FILE_TITLE_KEY = "select_credentials_file_title"
        const val SELECT_CREDENTIALS_FILE_BUTTON_KEY = "select_credentials_file_button"
        const val SELECT_CREDENTIALS_FILE_FILE_CHOOSER_TITLE_KEY = "select_credentials_file_file_chooser_title"
        const val WRONG_CREDENTIALS_FILE_POP_UP_MESSAGE_KEY = "wrong_credentials_file_pop_up_message"
        const val WRONG_CREDENTIALS_FILE_POP_UP_TITLE_KEY = "wrong_credentials_file_pop_up_title"
    }
}