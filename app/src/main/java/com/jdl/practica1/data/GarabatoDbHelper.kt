package com.jdl.practica1.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.IOException
import com.jdl.practica1.domain.model.DibujoModel
import com.jdl.practica1.domain.model.UsuarioModel

data class DibujoDeletionResult(
    val deleted: Boolean,
    val imageDeleted: Boolean
)

class GarabatoDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val appContext = context.applicationContext

    companion object {
        private const val DATABASE_NAME = "garabato.db"
        private const val DATABASE_VERSION = 4

        @Volatile
        private var INSTANCE: GarabatoDbHelper? = null

        fun getInstance(context: Context): GarabatoDbHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = GarabatoDbHelper(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE usuario (
                idUsuario INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                correo TEXT NOT NULL,
                avatarUrl TEXT,
                descripcion TEXT NOT NULL DEFAULT ''
            )
        """)

        db.execSQL("""
            CREATE TABLE dibujo (
                idDibujo INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT NOT NULL,
                fechaCreacion INTEGER NOT NULL,
                idUsuarioFK INTEGER NOT NULL,
                rutaImagen TEXT,
                FOREIGN KEY(idUsuarioFK) REFERENCES usuario(idUsuario) ON DELETE CASCADE
            )
        """)

        db.execSQL("""
            CREATE TABLE comentario (
                idComentario INTEGER PRIMARY KEY AUTOINCREMENT,
                contenido TEXT NOT NULL,
                fecha INTEGER NOT NULL,
                idUsuarioFK INTEGER NOT NULL,
                idDibujoFK INTEGER NOT NULL,
                FOREIGN KEY(idUsuarioFK) REFERENCES usuario(idUsuario) ON DELETE CASCADE,
                FOREIGN KEY(idDibujoFK) REFERENCES dibujo(idDibujo) ON DELETE CASCADE
            )
        """)

        db.execSQL("""
            CREATE TABLE me_gusta (
                idUsuarioFK INTEGER NOT NULL,
                idDibujoFK INTEGER NOT NULL,
                fecha INTEGER NOT NULL,
                PRIMARY KEY(idUsuarioFK, idDibujoFK),
                FOREIGN KEY(idUsuarioFK) REFERENCES usuario(idUsuario) ON DELETE CASCADE,
                FOREIGN KEY(idDibujoFK) REFERENCES dibujo(idDibujo) ON DELETE CASCADE
            )
        """)

        db.execSQL("""
            CREATE TABLE amistad (
                idEmisor INTEGER NOT NULL,
                idReceptor INTEGER NOT NULL,
                estado TEXT NOT NULL,
                PRIMARY KEY(idEmisor, idReceptor),
                FOREIGN KEY(idEmisor) REFERENCES usuario(idUsuario) ON DELETE CASCADE,
                FOREIGN KEY(idReceptor) REFERENCES usuario(idUsuario) ON DELETE CASCADE
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS amistad")
            db.execSQL("DROP TABLE IF EXISTS me_gusta")
            db.execSQL("DROP TABLE IF EXISTS comentario")
            db.execSQL("DROP TABLE IF EXISTS dibujo")
            db.execSQL("DROP TABLE IF EXISTS usuario")
            onCreate(db)
        } else if (oldVersion < 4) {
            db.execSQL("ALTER TABLE usuario ADD COLUMN descripcion TEXT NOT NULL DEFAULT ''")
        }
    }

    fun obtenerDibujos(): List<DibujoModel> {
        val lista = mutableListOf<DibujoModel>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT idDibujo, titulo, fechaCreacion, idUsuarioFK, rutaImagen FROM dibujo ORDER BY fechaCreacion DESC",
            null
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("idDibujo"))
                val titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"))
                val fecha = cursor.getLong(cursor.getColumnIndexOrThrow("fechaCreacion"))
                val usuarioFk = cursor.getLong(cursor.getColumnIndexOrThrow("idUsuarioFK"))
                val rutaImagen = cursor.getString(cursor.getColumnIndexOrThrow("rutaImagen"))
                lista.add(
                    DibujoModel(
                        idDibujo = id,
                        titulo = titulo,
                        fechaCreacion = fecha,
                        idUsuarioFK = usuarioFk,
                        rutaImagen = rutaImagen
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun obtenerDibujosPorUsuario(usuarioId: Long): List<DibujoModel> {
        val lista = mutableListOf<DibujoModel>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT idDibujo, titulo, fechaCreacion, idUsuarioFK, rutaImagen FROM dibujo WHERE idUsuarioFK = ? ORDER BY fechaCreacion DESC",
            arrayOf(usuarioId.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("idDibujo"))
                val titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"))
                val fecha = cursor.getLong(cursor.getColumnIndexOrThrow("fechaCreacion"))
                val usuarioFk = cursor.getLong(cursor.getColumnIndexOrThrow("idUsuarioFK"))
                val rutaImagen = cursor.getString(cursor.getColumnIndexOrThrow("rutaImagen"))
                lista.add(
                    DibujoModel(
                        idDibujo = id,
                        titulo = titulo,
                        fechaCreacion = fecha,
                        idUsuarioFK = usuarioFk,
                        rutaImagen = rutaImagen
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun contarDibujosPorUsuario(usuarioId: Long): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM dibujo WHERE idUsuarioFK = ?", arrayOf(usuarioId.toString()))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun eliminarDibujoPropio(dibujoId: Long, usuarioId: Long): DibujoDeletionResult {
        val db = writableDatabase
        var imagePath: String? = null
        db.beginTransaction()
        try {
            val cursor = db.rawQuery(
                "SELECT rutaImagen FROM dibujo WHERE idDibujo = ? AND idUsuarioFK = ?",
                arrayOf(dibujoId.toString(), usuarioId.toString())
            )
            val owned = cursor.moveToFirst()
            if (owned && !cursor.isNull(0)) {
                imagePath = cursor.getString(0)
            }
            cursor.close()
            if (!owned) {
                return DibujoDeletionResult(deleted = false, imageDeleted = true)
            }

            db.delete("me_gusta", "idDibujoFK = ?", arrayOf(dibujoId.toString()))
            db.delete("comentario", "idDibujoFK = ?", arrayOf(dibujoId.toString()))
            val deleted = db.delete(
                "dibujo",
                "idDibujo = ? AND idUsuarioFK = ?",
                arrayOf(dibujoId.toString(), usuarioId.toString())
            )
            if (deleted != 1) {
                return DibujoDeletionResult(deleted = false, imageDeleted = true)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        val imageDeleted = imagePath?.let(::deleteDrawingImage) ?: true
        return DibujoDeletionResult(deleted = true, imageDeleted = imageDeleted)
    }

    private fun deleteDrawingImage(path: String): Boolean {
        return try {
            val filesRoot = appContext.filesDir.canonicalFile
            val imageFile = File(path).canonicalFile
            if (!imageFile.path.startsWith(filesRoot.path + File.separator)) {
                return false
            }
            !imageFile.exists() || imageFile.delete()
        } catch (_: IOException) {
            return false
        }
    }

    fun obtenerUsuarios(): List<UsuarioModel> {
        val lista = mutableListOf<UsuarioModel>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM usuario", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("idUsuario"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val correo = cursor.getString(cursor.getColumnIndexOrThrow("correo"))
                val avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatarUrl"))
                lista.add(UsuarioModel(idUsuario = id, nombre = nombre, correo = correo, avatarUrl = avatar))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun buscarUsuarios(query: String, currentUserId: Long): List<UsuarioModel> {
        val lista = mutableListOf<UsuarioModel>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM usuario WHERE idUsuario != ? AND nombre LIKE ? ORDER BY nombre ASC",
            arrayOf(currentUserId.toString(), "%$query%")
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("idUsuario"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val correo = cursor.getString(cursor.getColumnIndexOrThrow("correo"))
                val avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatarUrl"))
                lista.add(UsuarioModel(idUsuario = id, nombre = nombre, correo = correo, avatarUrl = avatar))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun obtenerUsuarioPorId(id: Long): UsuarioModel? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM usuario WHERE idUsuario = ?", arrayOf(id.toString()))
        var usuario: UsuarioModel? = null
        if (cursor.moveToFirst()) {
            val uid = cursor.getLong(cursor.getColumnIndexOrThrow("idUsuario"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val correo = cursor.getString(cursor.getColumnIndexOrThrow("correo"))
            val avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatarUrl"))
            val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
            usuario = UsuarioModel(idUsuario = uid, nombre = nombre, correo = correo, avatarUrl = avatar, descripcion = descripcion)
        }
        cursor.close()
        return usuario
    }

    fun enviarSolicitudAmistad(emisorId: Long, receptorId: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("idEmisor", emisorId)
            put("idReceptor", receptorId)
            put("estado", "pendiente")
        }
        db.insertWithOnConflict("amistad", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun obtenerSolicitudesPendientes(receptorId: Long): List<UsuarioModel> {
        val lista = mutableListOf<UsuarioModel>()
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT u.* FROM usuario u
            JOIN amistad a ON u.idUsuario = a.idEmisor
            WHERE a.idReceptor = ? AND a.estado = 'pendiente'
        """, arrayOf(receptorId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("idUsuario"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val correo = cursor.getString(cursor.getColumnIndexOrThrow("correo"))
                val avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatarUrl"))
                lista.add(UsuarioModel(idUsuario = id, nombre = nombre, correo = correo, avatarUrl = avatar))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun aceptarSolicitud(emisorId: Long, receptorId: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("estado", "aceptada")
        }
        db.update("amistad", values, "idEmisor = ? AND idReceptor = ?", arrayOf(emisorId.toString(), receptorId.toString()))
    }

    fun rechazarSolicitud(emisorId: Long, receptorId: Long) {
        val db = writableDatabase
        db.delete("amistad", "idEmisor = ? AND idReceptor = ?", arrayOf(emisorId.toString(), receptorId.toString()))
    }

    fun obtenerAmigosAceptados(userId: Long): List<UsuarioModel> {
        val lista = mutableListOf<UsuarioModel>()
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT DISTINCT u.* FROM usuario u
            JOIN amistad a ON (u.idUsuario = a.idEmisor OR u.idUsuario = a.idReceptor)
            WHERE (a.idEmisor = ? OR a.idReceptor = ?) AND a.estado = 'aceptada' AND u.idUsuario != ?
        """, arrayOf(userId.toString(), userId.toString(), userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("idUsuario"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val correo = cursor.getString(cursor.getColumnIndexOrThrow("correo"))
                val avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatarUrl"))
                lista.add(UsuarioModel(idUsuario = id, nombre = nombre, correo = correo, avatarUrl = avatar))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun obtenerEstadoAmistad(userId: Long, targetId: Long): String {
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT estado FROM amistad 
            WHERE (idEmisor = ? AND idReceptor = ?) OR (idEmisor = ? AND idReceptor = ?)
        """, arrayOf(userId.toString(), targetId.toString(), targetId.toString(), userId.toString()))
        var estado = "ninguna"
        if (cursor.moveToFirst()) {
            estado = cursor.getString(0)
        }
        cursor.close()
        return estado
    }

    fun contarMeGustaRecibidosPorUsuario(usuarioId: Long): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT COUNT(*) FROM me_gusta mg
            JOIN dibujo d ON mg.idDibujoFK = d.idDibujo
            WHERE d.idUsuarioFK = ?
        """, arrayOf(usuarioId.toString()))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }
}
