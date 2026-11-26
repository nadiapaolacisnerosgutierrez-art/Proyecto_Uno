package com.jdl.practica1.domain.repository

import com.jdl.practica1.domain.model.ComentarioModel
import com.jdl.practica1.domain.model.DibujoModel
import com.jdl.practica1.domain.model.MeGustaModel
import com.jdl.practica1.domain.model.UsuarioModel
import kotlinx.coroutines.flow.Flow

interface GarabatoRepository {
    fun obtenerUsuarios(): Flow<List<UsuarioModel>>
    suspend fun insertarUsuario(usuario: UsuarioModel): Long

    fun obtenerDibujos(): Flow<List<DibujoModel>>
    suspend fun insertarDibujo(dibujo: DibujoModel): Long

    fun obtenerComentarios(dibujoId: Long): Flow<List<ComentarioModel>>
    suspend fun insertarComentario(comentario: ComentarioModel)

    fun contarLikes(dibujoId: Long): Flow<Int>
    suspend fun insertarLike(meGusta: MeGustaModel)
}
