package com.jdl.practica1.data.dao

import androidx.room.*
import com.jdl.practica1.data.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    @Delete
    suspend fun eliminarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuario WHERE idUsuario = :id")
    suspend fun obtenerUsuarioPorId(id: Long): UsuarioEntity?

    @Query("SELECT * FROM usuario")
    fun obtenerTodosLosUsuarios(): Flow<List<UsuarioEntity>>
}
