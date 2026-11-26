package com.jdl.practica1.data.dao

import androidx.room.*
import com.jdl.practica1.data.entity.ComentarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComentarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarComentario(comentario: ComentarioEntity): Long

    @Delete
    suspend fun eliminarComentario(comentario: ComentarioEntity)

    @Query("SELECT * FROM comentario WHERE idDibujoFK = :dibujoId ORDER BY fecha ASC")
    fun obtenerComentariosPorDibujo(dibujoId: Long): Flow<List<ComentarioEntity>>
}
