package br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates

import br.com.afadc.folderstoalbumsconvertergooglephotos.extensions.kv
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.templates.values.IUploadScreenResBundleValues
import java.util.*

abstract class UploadScreenResBundleTemplate: ListResourceBundle(),
    IUploadScreenResBundleValues {

    final override fun getContents() = arrayOf(
        kv(BACK_BUTTON_KEY, backButton),
        kv(CANCEL_MEDIA_UPLOAD_BUTTON_KEY, cancelMediaUploadButton),
        kv(FETCHING_ALBUMS_KEY, fetchingAlbums),
        kv(EMAIL_LABEL_FORMAT_KEY, emailLabelFormat),
        kv(LOG_ON_MEDIA_ITEM_CREATION_FAILED_FORMAT_KEY, logOnMediaItemCreationFailedFormat),
        kv(LOG_ON_MEDIA_ITEM_CREATION_SUCCEEDED_FORMAT_KEY, logOnMediaItemCreationSucceededFormat),
        kv(LOG_ON_MEDIA_UPLOAD_FAILED_KEY, logOnMediaUploadFailed),
        kv(LOG_ON_MEDIA_UPLOAD_SUCCEEDED_KEY, logOnMediaUploadedSucceeded),
        kv(LOG_ON_WILL_CREATE_MEDIA_ITEMS_FORMAT_KEY, logOnWillCreateMediaItemsFormat),
        kv(LOG_ON_WILL_UPLOAD_MEDIA_FORMAT_KEY, logOnWillUploadMediaFormat),
        kv(LOG_STARTING_TO_UPLOAD_MEDIAS_KEY, logStartingToUploadMedias),
        kv(NO_ALBUMS_DIRECTORIES_POP_UP_MESSAGE_KEY, noAlbumsDirectoriesPopUpMessage),
        kv(NO_ALBUMS_DIRECTORIES_POP_UP_TITLE_KEY, noAlbumsDirectoriesPopUpTitle),
        kv(NO_ALBUMS_SELECTED_TO_UPLOAD_POP_UP_MESSAGE_KEY, noAlbumsSelectedToUploadPopUpMessage),
        kv(NO_ALBUMS_SELECTED_TO_UPLOAD_POP_UP_TITLE_KEY, noAlbumsSelectedToUploadPopUpTitle),
        kv(UPLOAD_MEDIA_BUTTON_ZERO_MEDIA_COUNT_KEY, uploadMediaButtonZeroMediaCount),
        kv(UPLOAD_MEDIA_BUTTON_NON_ZERO_MEDIA_COUNT_FORMAT_KEY, uploadMediaButtonNonZeroMediaCountFormat),
        kv(UPLOAD_MEDIA_ERROR_POP_UP_ALBUM_CREATION_FAILED_MESSAGE_KEY, uploadMediaErrorPopUpAlbumCreationFailedMessage),
        kv(UPLOAD_MEDIA_ERROR_POP_UP_GENERAL_ERROR_MESSAGE_KEY, uploadMediaErrorPopUpGeneralErrorMessage),
        kv(UPLOAD_MEDIA_ERROR_POP_UP_NO_GOOGLE_PHOTOS_ACCESS_MESSAGE_KEY, uploadMediaErrorPopUpNoGooglePhotosAccessMessage),
        kv(UPLOAD_MEDIA_ERROR_POP_UP_TITLE_KEY, uploadMediaErrorPopUpTitle)
    )

    companion object {
        const val BACK_BUTTON_KEY = "back_button"
        const val CANCEL_MEDIA_UPLOAD_BUTTON_KEY = "cancel_media_upload_button"
        const val EMAIL_LABEL_FORMAT_KEY = "email_label_format"
        const val FETCHING_ALBUMS_KEY = "fetching_albums"
        const val LOG_ON_MEDIA_UPLOAD_FAILED_KEY = "log_on_media_upload_failed"
        const val LOG_ON_MEDIA_UPLOAD_SUCCEEDED_KEY = "log_on_media_upload_succeeded"
        const val LOG_ON_MEDIA_ITEM_CREATION_FAILED_FORMAT_KEY = "log_on_media_item_creation_failed_format"
        const val LOG_ON_MEDIA_ITEM_CREATION_SUCCEEDED_FORMAT_KEY = "log_on_media_item_creation_succeeded_format"
        const val LOG_ON_WILL_CREATE_MEDIA_ITEMS_FORMAT_KEY = "log_on_will_create_media_items_format"
        const val LOG_ON_WILL_UPLOAD_MEDIA_FORMAT_KEY = "log_on_will_upload_media_format"
        const val NO_ALBUMS_DIRECTORIES_POP_UP_MESSAGE_KEY = "no_albums_directories_pop_up_message"
        const val NO_ALBUMS_DIRECTORIES_POP_UP_TITLE_KEY = "no_albums_directories_pop_up_title"
        const val NO_ALBUMS_SELECTED_TO_UPLOAD_POP_UP_MESSAGE_KEY = "no_albums_selected_to_upload_pop_up_message"
        const val NO_ALBUMS_SELECTED_TO_UPLOAD_POP_UP_TITLE_KEY = "no_albums_selected_to_upload_pop_up_title"
        const val LOG_STARTING_TO_UPLOAD_MEDIAS_KEY = "log_starting_to_upload_medias"
        const val UPLOAD_MEDIA_BUTTON_NON_ZERO_MEDIA_COUNT_FORMAT_KEY = "upload_media_button_non_zero_media_count_format"
        const val UPLOAD_MEDIA_BUTTON_ZERO_MEDIA_COUNT_KEY = "upload_media_button_zero_media_count"
        const val UPLOAD_MEDIA_ERROR_POP_UP_ALBUM_CREATION_FAILED_MESSAGE_KEY = "upload_media_error_pop_up_album_creation_failed_message"
        const val UPLOAD_MEDIA_ERROR_POP_UP_GENERAL_ERROR_MESSAGE_KEY = "upload_media_error_pop_up_general_error_message"
        const val UPLOAD_MEDIA_ERROR_POP_UP_NO_GOOGLE_PHOTOS_ACCESS_MESSAGE_KEY = "upload_media_error_pop_up_no_google_photos_access_message"
        const val UPLOAD_MEDIA_ERROR_POP_UP_TITLE_KEY = "upload_media_error_pop_up_title"
    }
}