package com.jdl.practica1.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jdl.practica1.data.dao.ComentarioDao
import com.jdl.practica1.data.dao.DibujoDao
import com.jdl.practica1.data.dao.MeGustaDao
import com.jdl.practica1.data.dao.UsuarioDao
import com.jdl.practica1.data.entity.ComentarioEntity
import com.jdl.practica1.data.entity.DibujoEntity
import com.jdl.practica1.data.entity.MeGustaEntity
import com.jdl.practica1.data.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        DibujoEntity::class,
        ComentarioEntity::class,
        MeGustaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GarabatoDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun dibujoDao(): DibujoDao
    abstract fun comentarioDao(): ComentarioDao
    abstract fun meGustaDao(): MeGustaDao

    companion object {
        @Volatile
        private var INSTANCE: GarabatoDatabase? = null

        fun getInstance(context: Context): GarabatoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GarabatoDatabase::class.java,
                    "garabato_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
