package br.com.afadc.folderstoalbumsconvertergooglephotos.db

import br.com.afadc.folderstoalbumsconvertergooglephotos.db.entities.DbAlbum
import br.com.afadc.folderstoalbumsconvertergooglephotos.db.entities.DbMedia
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Types

class AppDatabase(private val dbDir: File) {

    private val dbDirAsUri = dbDir.toURI()

    private val conn: Connection

    init {
        if (!dbDir.isDirectory) {
            throw IllegalArgumentException("dbDir [$dbDir] is not a directory")
        }

        val dbFile = File("${dbDir.absolutePath}/$DB_FILE_NAME")

        val wasDbJustCreated = !dbFile.exists()

        val dbUrl = "jdbc:sqlite:${dbFile.absolutePath}"
        conn = DriverManager.getConnection(dbUrl)

        if (conn.isClosed) {
            throw RuntimeException("The connection is closed")
        }

        if (wasDbJustCreated) {
            createDb()
        }
    }

    private fun createDb() {
        val statement = conn.createStatement()

        statement.execute(CREATE_TABLE_ALBUMS_SQL)
        statement.execute(CREATE_TABLE_MEDIAS_SQL)
    }

    fun close() {
        if (conn.isClosed) {
            return
        }

        conn.close()
    }

    fun getRelativeFilePathToDbDir(file: File): String {
        return dbDirAsUri.relativize(file.toURI()).path
    }

    fun getDbAlbumByDir(dir: File): DbAlbum? {
        if (!dir.isDirectory) {
            throw IllegalArgumentException("dir [$dir] is not a directory")
        }

        val query = """
            SELECT
            *
            FROM
            albums
            WHERE name = ?
        """

        val preparedStatement = conn.prepareStatement(query)
        preparedStatement.setString(1, dir.name)

        val rs = preparedStatement.executeQuery()

        return if (rs.next()) {
            DbAlbum(
                rs.getString("name"),
                rs.getString("photos_album_id")
            )
        } else {
            null
        }
    }

    fun insertOrUpdateDbAlbum(dbAlbum: DbAlbum) {
        if (getDbAlbumByDir(File(dbDir, dbAlbum.name)) == null) {
            insertDbAlbum(dbAlbum)
        } else {
            updateDbAlbum(dbAlbum)
        }
    }

    private fun insertDbAlbum(dbAlbum: DbAlbum) {
        val sql = """
            INSERT INTO albums (
                name,
                photos_album_id
            ) VALUES (
                ?,
                ?
            )
        """

        val preparedStatement = conn.prepareStatement(sql)
        preparedStatement.setString(1, dbAlbum.name)
        preparedStatement.setString(2, dbAlbum.photos_album_id)

        preparedStatement.executeUpdate()
    }

    private fun updateDbAlbum(dbAlbum: DbAlbum) {
        val sql = """
            UPDATE albums
            SET photos_album_id = ?
            WHERE name = ?
        """

        val preparedStatement = conn.prepareStatement(sql)
        preparedStatement.setString(1, dbAlbum.photos_album_id)
        preparedStatement.setString(2, dbAlbum.name)

        preparedStatement.executeUpdate()
    }

    fun getDbMediaByFile(file: File): DbMedia? {
        val relativePath = getRelativeFilePathToDbDir(file)

        val query = """
           SELECT
            *
            FROM
            medias
            WHERE path = ?
        """

        val preparedStatement = conn.prepareStatement(query)
        preparedStatement.setString(1, relativePath)

        val rs = preparedStatement.executeQuery()

        return if (rs.next()) {
            DbMedia(
                rs.getString("path"),
                rs.getString("album_name"),
                rs.getBoolean("is_uploaded_and_created"),
                rs.getString("upload_token"),
                rs.getLong("upload_token_generation_time")
            )
        } else {
            null
        }
    }


    fun insertOrUpdateMedia(dbMedia: DbMedia) {
        if (getDbMediaByFile(File(dbDir, dbMedia.path)) == null) {
            insertDbMedia(dbMedia)
        } else {
            updateDbMedia(dbMedia)
        }
    }

    private fun insertDbMedia(dbMedia: DbMedia) {
        val sql = """
           INSERT INTO medias (
                path,
                album_name,
                is_uploaded_and_created,
                upload_token,
                upload_token_generation_time
           ) VALUES (
                ?,
                ?,
                ?,
                ?,
                ?
           )
        """

        val preparedStatement = conn.prepareStatement(sql)
        preparedStatement.setString(1, dbMedia.path)
        preparedStatement.setString(2, dbMedia.albumName)
        preparedStatement.setBoolean(3, dbMedia.isUploadedAndCreated)
        preparedStatement.setString(4, dbMedia.uploadToken)
        if (dbMedia.uploadTokenGenerationTime != null) {
            preparedStatement.setLong(5, dbMedia.uploadTokenGenerationTime!!)
        } else {
            preparedStatement.setNull(5, Types.INTEGER)
        }

        preparedStatement.executeUpdate()
    }

    private fun updateDbMedia(dbMedia: DbMedia) {
        val sql = """
           UPDATE medias
           SET album_name = ?,
           is_uploaded_and_created = ?,
           upload_token = ?,
           upload_token_generation_time = ?
           WHERE path = ?
        """

        val preparedStatement = conn.prepareStatement(sql)
        preparedStatement.setString(1, dbMedia.albumName)
        preparedStatement.setBoolean(2, dbMedia.isUploadedAndCreated)
        preparedStatement.setString(3, dbMedia.uploadToken)
        if (dbMedia.uploadTokenGenerationTime != null) {
            preparedStatement.setLong(4, dbMedia.uploadTokenGenerationTime!!)
        } else {
            preparedStatement.setNull(4, Types.INTEGER)
        }
        preparedStatement.setString(5, dbMedia.path)

        preparedStatement.executeUpdate()
    }

    companion object {
        private const val DB_FILE_NAME = "foldersToAlbums.sqlite"

        private const val CREATE_TABLE_ALBUMS_SQL =
            """
                CREATE TABLE albums (
                    name TEXT NOT NULL PRIMARY KEY,
                    photos_album_id TEXT NOT NULL
                );
            """

        private const val CREATE_TABLE_MEDIAS_SQL =
            """
                CREATE TABLE medias (
                    path TEXT NOT NULL PRIMARY KEY,
                    album_name TEXT NOT NULL,
                    is_uploaded_and_created BOOLEAN NOT NULL,
                    upload_token TEXT,
                    upload_token_generation_time INTEGER,
                    FOREIGN KEY (album_name)
                        REFERENCES albums (name)
                            ON DELETE CASCADE
                            ON UPDATE NO ACTION
                );
            """
    }
}