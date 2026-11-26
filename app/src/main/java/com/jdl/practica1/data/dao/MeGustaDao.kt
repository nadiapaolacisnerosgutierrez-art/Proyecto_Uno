package com.jdl.practica1.data.dao

import androidx.room.*
import com.jdl.practica1.data.entity.MeGustaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeGustaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMeGusta(meGusta: MeGustaEntity)

    @Delete
    suspend fun eliminarMeGusta(meGusta: MeGustaEntity)

    @Query("SELECT COUNT(*) FROM me_gusta WHERE idDibujoFK = :dibujoId")
    fun contarMeGustasPorDibujo(dibujoId: Long): Flow<Int>

    @Query("SELECT * FROM me_gusta WHERE idUsuarioFK = :usuarioId AND idDibujoFK = :dibujoId")
    suspend fun obtenerMeGustaEspecifico(usuarioId: Long, dibujoId: Long): MeGustaEntity?
}
