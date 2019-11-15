package br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization

import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def.ConnectScreenResBundle
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def.HdDirectoryScreenResBundle
import br.com.afadc.folderstoalbumsconvertergooglephotos.internationalization.def.UploadScreenResBundle
import java.util.*

object AppResBundles {
    fun getConnectScreenBundle() = ConnectScreenResBundle.Bundle(
        ResourceBundle.getBundle(
            ConnectScreenResBundle::class.java.name,
            Locale.getDefault()
        )
    )

    fun getHdDirectoryScreenBundle() = HdDirectoryScreenResBundle.Bundle(
        ResourceBundle.getBundle(
            HdDirectoryScreenResBundle::class.java.name,
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