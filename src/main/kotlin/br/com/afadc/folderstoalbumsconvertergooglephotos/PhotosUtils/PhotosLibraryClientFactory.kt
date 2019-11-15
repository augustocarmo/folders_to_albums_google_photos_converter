package br.com.afadc.folderstoalbumsconvertergooglephotos.PhotosUtils

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.Credentials
import com.google.auth.oauth2.UserCredentials
import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.library.v1.PhotosLibrarySettings
import br.com.afadc.folderstoalbumsconvertergooglephotos.utils.EmailValidator
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.security.GeneralSecurityException

class PhotosLibraryClientFactory {

    @Throws(IOException::class, GeneralSecurityException::class)
    fun createClient(credentialsPath: String, email: String): PhotosLibraryClient {
        if (!EmailValidator().isValid(email)) {
            throw IllegalArgumentException("The email is not valid")
        }

        val settings = PhotosLibrarySettings.newBuilder()
            .setCredentialsProvider(
                FixedCredentialsProvider.create(
                    getUserCredentials(credentialsPath, SCOPES, email)
                )
            )
            .build()
        return PhotosLibraryClient.initialize(settings)
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    private fun getUserCredentials(
        credentialsPath: String,
        selectedScopes: List<String>,
        email: String
    ): Credentials {
        val clientSecrets = GoogleClientSecrets.load(
            JSON_FACTORY, InputStreamReader(FileInputStream(credentialsPath))
        )
        val clientId = clientSecrets.details.clientId
        val clientSecret = clientSecrets.details.clientSecret

        val flow = GoogleAuthorizationCodeFlow.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            clientSecrets,
            selectedScopes
        )
            .setDataStoreFactory(FileDataStoreFactory(DATA_STORE_DIR))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(LOCAL_RECEIVER_PORT).build()
        val credential = AuthorizationCodeInstalledApp(flow, receiver).authorize(email)
        return UserCredentials.newBuilder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRefreshToken(credential.refreshToken)
            .build()
    }

    companion object {
        private val DATA_STORE_DIR =
            java.io.File(System.getProperty("user.home"), "afadc_google_credentials")
        private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
        private const val LOCAL_RECEIVER_PORT = 61984

        private val SCOPES = listOf(
            "https://www.googleapis.com/auth/photoslibrary"
        )
    }
}