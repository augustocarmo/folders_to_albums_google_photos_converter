package br.com.afadc.folderstoalbumsconvertergooglephotos.entities

import java.io.File

class Album(
    val directory: File,
    val medias: List<Media>
) {

    val name = directory.name
}