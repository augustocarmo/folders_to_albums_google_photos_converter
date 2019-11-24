package br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.ConnectScreenResBundleTemplate
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.values.IConnectScreenResBundleValues
import java.util.*

class ConnectScreenResBundle : ConnectScreenResBundleTemplate() {

    class Bundle(private val rb: ResourceBundle) :
        IConnectScreenResBundleValues {
        override val credentialsInstructions: String get() = rb.getString(CREDENTIALS_INSTRUCTIONS_KEY)
        override val emailLabel: String = rb.getString(EMAIL_LABEL_KEY)
        override val invalidEmailPopUpMessage: String = rb. getString(INVALID_EMAIL_POP_UP_MESSAGE_KEY)
        override val invalidEmailPopUpTitle: String = rb.getString(INVALID_EMAIL_POP_UP_TITLE_KEY)
        override val selectCredentialsFileTitle: String = rb.getString(SELECT_CREDENTIALS_FILE_TITLE_KEY)
        override val selectCredentialsFileButton: String = rb.getString(SELECT_CREDENTIALS_FILE_BUTTON_KEY)
        override val selectCredentialsFileFileChooserTitle: String = rb.getString(SELECT_CREDENTIALS_FILE_FILE_CHOOSER_TITLE_KEY)
        override val wrongCredentialsFilePopUpMessage: String = rb.getString(WRONG_CREDENTIALS_FILE_POP_UP_MESSAGE_KEY)
        override val wrongCredentialsFilePopUpTitle: String = rb.getString(WRONG_CREDENTIALS_FILE_POP_UP_TITLE_KEY)
    }

    override val credentialsInstructions: String
        get() = "You need to enter your email from Gmail and then select the Google credentials json file to be able to connect. For further info, please access <a href=\"https://github.com/augustocarmo/folders_to_albums_google_photos_converter\">this link.</a>"
    override val emailLabel: String
        get() = "Email: "
    override val invalidEmailPopUpMessage: String
        get() = "Enter a valid email."
    override val invalidEmailPopUpTitle: String
        get() = "Invalid Email"
    override val selectCredentialsFileTitle: String
        get() = "Select the Credentials File"
    override val selectCredentialsFileButton: String
        get() = "Connect"
    override val selectCredentialsFileFileChooserTitle: String
        get() = "Select the Google Photo's credential file"
    override val wrongCredentialsFilePopUpMessage: String
        get() = "Select the correct Google Photo's credentials file"
    override val wrongCredentialsFilePopUpTitle: String
        get() = "Fail to Connect"
}