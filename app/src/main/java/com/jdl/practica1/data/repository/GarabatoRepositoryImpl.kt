package com.jdl.practica1.data.repository

import com.jdl.practica1.data.GarabatoDbHelper
import com.jdl.practica1.domain.model.ComentarioModel
import com.jdl.practica1.domain.model.DibujoModel
import com.jdl.practica1.domain.model.MeGustaModel
import com.jdl.practica1.domain.model.UsuarioModel
import com.jdl.practica1.domain.repository.GarabatoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GarabatoRepositoryImpl(
    private val dbHelper: GarabatoDbHelper
) : GarabatoRepository {

    override fun obtenerUsuarios(): Flow<List<UsuarioModel>> = flow {
        emit(dbHelper.obtenerUsuarios())
    }

    override suspend fun insertarUsuario(usuario: UsuarioModel): Long {
        return 0L
    }

    override fun obtenerDibujos(): Flow<List<DibujoModel>> = flow {
        emit(dbHelper.obtenerDibujos())
    }

    override suspend fun insertarDibujo(dibujo: DibujoModel): Long {
        return 0L
    }

    override fun obtenerComentarios(dibujoId: Long): Flow<List<ComentarioModel>> = flow {
        emit(emptyList())
    }

    override suspend fun insertarComentario(comentario: ComentarioModel) {}

    override fun contarLikes(dibujoId: Long): Flow<Int> = flow {
        emit(0)
    }

    override suspend fun insertarLike(meGusta: MeGustaModel) {}
}
