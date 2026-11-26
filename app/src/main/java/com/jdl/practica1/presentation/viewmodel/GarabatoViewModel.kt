package com.jdl.practica1.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdl.practica1.domain.model.DibujoModel
import com.jdl.practica1.domain.model.UsuarioModel
import com.jdl.practica1.domain.repository.GarabatoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class GarabatoViewModel(
    private val repository: GarabatoRepository
) : ViewModel() {

    val dibujos: StateFlow<List<DibujoModel>> = repository.obtenerDibujos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val usuarios: StateFlow<List<UsuarioModel>> = repository.obtenerUsuarios()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun registrarUsuario(nombre: String, correo: String): Long {
        val nuevoUsuario = UsuarioModel(idUsuario = 0, nombre = nombre, correo = correo, avatarUrl = null)
        return repository.insertarUsuario(nuevoUsuario)
    }

    suspend fun publicarDibujo(titulo: String, usuarioId: Long): Long {
        val nuevoDibujo = DibujoModel(idDibujo = 0, titulo = titulo, fechaCreacion = System.currentTimeMillis(), idUsuarioFK = usuarioId)
        return repository.insertarDibujo(nuevoDibujo)
    }
}
