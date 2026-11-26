package com.jdl.practica1.data.mapper

import com.jdl.practica1.data.entity.ComentarioEntity
import com.jdl.practica1.data.entity.DibujoEntity
import com.jdl.practica1.data.entity.MeGustaEntity
import com.jdl.practica1.data.entity.UsuarioEntity
import com.jdl.practica1.domain.model.ComentarioModel
import com.jdl.practica1.domain.model.DibujoModel
import com.jdl.practica1.domain.model.MeGustaModel
import com.jdl.practica1.domain.model.UsuarioModel

// Mapper para Usuario
fun UsuarioEntity.toDomain() = UsuarioModel(
    idUsuario = idUsuario,
    nombre = nombre,
    correo = correo,
    avatarUrl = avatarUrl
)

fun UsuarioModel.toEntity() = UsuarioEntity(
    idUsuario = idUsuario,
    nombre = nombre,
    correo = correo,
    avatarUrl = avatarUrl
)

// Mapper para Dibujo
fun DibujoEntity.toDomain() = DibujoModel(
    idDibujo = idDibujo,
    titulo = titulo,
    fechaCreacion = fechaCreacion,
    idUsuarioFK = idUsuarioFK
)

fun DibujoModel.toEntity() = DibujoEntity(
    idDibujo = idDibujo,
    titulo = titulo,
    fechaCreacion = fechaCreacion,
    idUsuarioFK = idUsuarioFK
)

// Mapper para Comentario
fun ComentarioEntity.toDomain() = ComentarioModel(
    idComentario = idComentario,
    contenido = contenido,
    fecha = fecha,
    idUsuarioFK = idUsuarioFK,
    idDibujoFK = idDibujoFK
)

fun ComentarioModel.toEntity() = ComentarioEntity(
    idComentario = idComentario,
    contenido = contenido,
    fecha = fecha,
    idUsuarioFK = idUsuarioFK,
    idDibujoFK = idDibujoFK
)

// Mapper para MeGusta
fun MeGustaEntity.toDomain() = MeGustaModel(
    idUsuarioFK = idUsuarioFK,
    idDibujoFK = idDibujoFK,
    fecha = fecha
)

fun MeGustaModel.toEntity() = MeGustaEntity(
    idUsuarioFK = idUsuarioFK,
    idDibujoFK = idDibujoFK,
    fecha = fecha
)
