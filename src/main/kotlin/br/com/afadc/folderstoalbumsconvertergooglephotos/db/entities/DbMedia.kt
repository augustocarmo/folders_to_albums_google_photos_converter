package br.com.afadc.folderstoalbumsconvertergooglephotos.db.entities

class DbMedia(
    var path: String,
    var albumName: String,
    var isUploadedAndCreated: Boolean,
    var uploadToken: String?,
    var uploadTokenGenerationTime: Long?
)