package br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def.ConnectScreenResBundle
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def.AlbumsDirectoryScreenResBundle
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def.UploadScreenResBundle
import java.util.*

object AppResBundles {
    fun getConnectScreenBundle() = ConnectScreenResBundle.Bundle(
        ResourceBundle.getBundle(
            ConnectScreenResBundle::class.java.name,
            Locale.getDefault()
        )
    )

    fun getAlbumsDirectoryScreenBundle() = AlbumsDirectoryScreenResBundle.Bundle(
        ResourceBundle.getBundle(
            AlbumsDirectoryScreenResBundle::class.java.name,
            Locale.getDefault()
        )
    )

    fun getUploadScreenBundle() = UploadScreenResBundle.Bundle(
        ResourceBundle.getBundle(
            UploadScreenResBundle::class.java.name,
            Locale.getDefault()
        )
    )
}