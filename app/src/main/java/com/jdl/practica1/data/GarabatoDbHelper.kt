package com.jdl.practica1.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jdl.practica1.domain.model.DibujoModel
import com.jdl.practica1.domain.model.UsuarioModel

class GarabatoDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "garabato.db"
        private const val DATABASE_VERSION = 1

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
                avatarUrl TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE dibujo (
                idDibujo INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT NOT NULL,
                fechaCreacion INTEGER NOT NULL,
                idUsuarioFK INTEGER NOT NULL,
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

        insertTestData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS me_gusta")
        db.execSQL("DROP TABLE IF EXISTS comentario")
        db.execSQL("DROP TABLE IF EXISTS dibujo")
        db.execSQL("DROP TABLE IF EXISTS usuario")
        onCreate(db)
    }

    private fun insertTestData(db: SQLiteDatabase) {
        val u1 = ContentValues().apply { put("nombre", "Ana García"); put("correo", "ana@garabato.com") }
        val idU1 = db.insert("usuario", null, u1)

        val u2 = ContentValues().apply { put("nombre", "Carlos Ruiz"); put("correo", "carlos@garabato.com") }
        val idU2 = db.insert("usuario", null, u2)

        val u3 = ContentValues().apply { put("nombre", "Sofía López"); put("correo", "sofia@garabato.com") }
        val idU3 = db.insert("usuario", null, u3)

        val d1 = ContentValues().apply {
            put("titulo", "Atardecer en la Montaña")
            put("fechaCreacion", System.currentTimeMillis() - 100000)
            put("idUsuarioFK", idU1)
        }
        db.insert("dibujo", null, d1)

        val d2 = ContentValues().apply {
            put("titulo", "Gato Galáctico")
            put("fechaCreacion", System.currentTimeMillis() - 50000)
            put("idUsuarioFK", idU2)
        }
        db.insert("dibujo", null, d2)

        val d3 = ContentValues().apply {
            put("titulo", "Bosque Encantado")
            put("fechaCreacion", System.currentTimeMillis())
            put("idUsuarioFK", idU3)
        }
        db.insert("dibujo", null, d3)
    }

    // Métodos de consulta para conectar la BD con las vistas y Clean Architecture
    fun obtenerDibujos(): List<DibujoModel> {
        val lista = mutableListOf<DibujoModel>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM dibujo ORDER BY fechaCreacion DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("idDibujo"))
                val titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"))
                val fecha = cursor.getLong(cursor.getColumnIndexOrThrow("fechaCreacion"))
                val usuarioFk = cursor.getLong(cursor.getColumnIndexOrThrow("idUsuarioFK"))
                lista.add(DibujoModel(idDibujo = id, titulo = titulo, fechaCreacion = fecha, idUsuarioFK = usuarioFk))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
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
}
