package com.jdl.practica1.domain.model

data class UsuarioModel(
    val idUsuario: Long,
    val nombre: String,
    val correo: String,
    val avatarUrl: String?,
    val descripcion: String = ""
)
