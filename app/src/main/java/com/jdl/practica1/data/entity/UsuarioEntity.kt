package com.jdl.practica1.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val idUsuario: Long = 0L,
    val nombre: String,
    val correo: String,
    val avatarUrl: String? = null
)
