package com.jdl.practica1.domain.model

data class DibujoModel(
    val idDibujo: Long,
    val titulo: String,
    val fechaCreacion: Long,
    val idUsuarioFK: Long,
    val rutaImagen: String? = null
)
