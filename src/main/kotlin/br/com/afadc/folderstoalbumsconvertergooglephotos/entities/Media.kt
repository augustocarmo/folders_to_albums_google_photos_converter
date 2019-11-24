package br.com.afadc.folderstoalbumsconvertergooglephotos.entities

import java.io.File

class Media(val file: File) {
    val name = file.nameWithoutExtension
    val extension = file.extension
}