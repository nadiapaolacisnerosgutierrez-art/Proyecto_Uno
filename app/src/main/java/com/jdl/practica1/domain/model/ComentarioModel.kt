package com.jdl.practica1.domain.model

data class ComentarioModel(
    val idComentario: Long,
    val contenido: String,
    val fecha: Long,
    val idUsuarioFK: Long,
    val idDibujoFK: Long
)
