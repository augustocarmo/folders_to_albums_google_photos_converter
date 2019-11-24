package br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.UploadScreenResBundleTemplate
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.values.IUploadScreenResBundleValues
import java.util.*

class UploadScreenResBundle : UploadScreenResBundleTemplate(),
    IUploadScreenResBundleValues {

    class Bundle(private val rb: ResourceBundle) :
        IUploadScreenResBundleValues {
        override val backButton: String = rb.getString(BACK_BUTTON_KEY)
        override val cancelMediaUploadButton: String = rb.getString(CANCEL_MEDIA_UPLOAD_BUTTON_KEY)
        override val emailLabelFormat: String = rb.getString(EMAIL_LABEL_FORMAT_KEY)
        override val fetchingAlbums: String get() = rb.getString(FETCHING_ALBUMS_KEY)
        override val logOnMediaItemCreationFailedFormat: String = rb.getString(LOG_ON_MEDIA_ITEM_CREATION_FAILED_FORMAT_KEY)
        override val logOnMediaItemCreationSucceededFormat: String =
            rb.getString(LOG_ON_MEDIA_ITEM_CREATION_SUCCEEDED_FORMAT_KEY)
        override val logOnMediaUploadedSucceeded: String = rb.getString(LOG_ON_MEDIA_UPLOAD_SUCCEEDED_KEY)
        override val logOnMediaUploadFailed: String = rb.getString(LOG_ON_MEDIA_UPLOAD_FAILED_KEY)
        override val logOnWillCreateMediaItemsFormat: String = rb.getString(LOG_ON_WILL_CREATE_MEDIA_ITEMS_FORMAT_KEY)
        override val logOnWillUploadMediaFormat: String = rb.getString(LOG_ON_WILL_UPLOAD_MEDIA_FORMAT_KEY)
        override val logStartingToUploadMedias: String = rb.getString(LOG_STARTING_TO_UPLOAD_MEDIAS_KEY)
        override val noAlbumsDirectoriesPopUpMessage: String
            get() = rb.getString(NO_ALBUMS_DIRECTORIES_POP_UP_MESSAGE_KEY)
        override val noAlbumsDirectoriesPopUpTitle: String
            get() = rb.getString(NO_ALBUMS_DIRECTORIES_POP_UP_TITLE_KEY)
        override val noMediaSelectedPopUpMessage: String = rb.getString(NO_MEDIA_SELECTED_POP_UP_MESSAGE_KEY)
        override val noMediaSelectedPopUpTitle: String = rb.getString(NO_MEDIA_SELECTED_POP_UP_TITLE_KEY)
        override val uploadMediaButtonZeroMediaCount: String = rb.getString(UPLOAD_MEDIA_BUTTON_ZERO_MEDIA_COUNT_KEY)
        override val uploadMediaButtonNonZeroMediaCountFormat: String =
            rb.getString(UPLOAD_MEDIA_BUTTON_NON_ZERO_MEDIA_COUNT_FORMAT_KEY)
        override val uploadMediaErrorPopUpAlbumCreationFailedMessage: String
            get() = rb.getString(UPLOAD_MEDIA_ERROR_POP_UP_ALBUM_CREATION_FAILED_MESSAGE_KEY)
        override val uploadMediaErrorPopUpGeneralErrorMessage: String
            get() = rb.getString(UPLOAD_MEDIA_ERROR_POP_UP_GENERAL_ERROR_MESSAGE_KEY)
        override val uploadMediaErrorPopUpNoGooglePhotosAccessMessage: String
            get() = rb.getString(UPLOAD_MEDIA_ERROR_POP_UP_NO_GOOGLE_PHOTOS_ACCESS_MESSAGE_KEY)
        override val uploadMediaErrorPopUpTitle: String
            get() = rb.getString(UPLOAD_MEDIA_ERROR_POP_UP_TITLE_KEY)
    }

    override val backButton: String
        get() = "Return"
    override val cancelMediaUploadButton: String
        get() = "Cancel Upload"
    override val emailLabelFormat: String
        get() = "E-mail: %s"
    override val fetchingAlbums: String
        get() = "Fetching albums..."
    override val logOnMediaItemCreationFailedFormat: String
        get() = "Fail on media creation [%s] on Google Photos!<br><br><br>"
    override val logOnMediaItemCreationSucceededFormat: String
        get() = "Success on media creation [%s] on Google Photos!<br><br><br>"
    override val logOnMediaUploadedSucceeded: String
        get() = "Succeeded!<br><br><br>"
    override val logOnMediaUploadFailed: String
        get() = "Failed!<br><br><br>"
    override val logOnWillCreateMediaItemsFormat: String
        get() = "Creating %s medias on Google Photos..."
    override val logOnWillUploadMediaFormat: String
        get() = "Uploading...<br><img src=\"%s\" width=\"150px\"/><br>%s..."
    override val logStartingToUploadMedias: String
        get() = "Starting to upload medias...<br>"
    override val noAlbumsDirectoriesPopUpMessage: String
        get() = "There are no albums directories in the selected directory (or they have no medias)"
    override val noAlbumsDirectoriesPopUpTitle: String
        get() = "Missing Albums Directories"
    override val noMediaSelectedPopUpMessage: String
        get() = "No media has been selected"
    override val noMediaSelectedPopUpTitle: String
        get() = "Failed to Upload"
    override val uploadMediaButtonZeroMediaCount: String
        get() = "Upload Medias"
    override val uploadMediaButtonNonZeroMediaCountFormat: String
        get() = "Upload Medias ([%d] Albums and [%d] Medias)"
    override val uploadMediaErrorPopUpAlbumCreationFailedMessage: String
        get() = "There was an error on one of the albums creation"
    override val uploadMediaErrorPopUpGeneralErrorMessage: String
        get() = "An error occurred"
    override val uploadMediaErrorPopUpNoGooglePhotosAccessMessage: String
        get() = "There was an error on communicating with Google Photos"
    override val uploadMediaErrorPopUpTitle: String
        get() = "Media Upload Failed"
}