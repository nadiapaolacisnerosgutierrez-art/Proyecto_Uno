package com.jdl.practica1.data.dao

import androidx.room.*
import com.jdl.practica1.data.entity.DibujoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DibujoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDibujo(dibujo: DibujoEntity): Long

    @Update
    suspend fun actualizarDibujo(dibujo: DibujoEntity)

    @Delete
    suspend fun eliminarDibujo(dibujo: DibujoEntity)

    @Query("SELECT * FROM dibujo WHERE idDibujo = :id")
    suspend fun obtenerDibujoPorId(id: Long): DibujoEntity?

    @Query("SELECT * FROM dibujo ORDER BY fechaCreacion DESC")
    fun obtenerTodosLosDibujos(): Flow<List<DibujoEntity>>

    @Query("SELECT * FROM dibujo WHERE idUsuarioFK = :usuarioId")
    fun obtenerDibujosPorUsuario(usuarioId: Long): Flow<List<DibujoEntity>>
}
