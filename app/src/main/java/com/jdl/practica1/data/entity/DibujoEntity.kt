package com.jdl.practica1.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dibujo",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["idUsuario"],
            childColumns = ["idUsuarioFK"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idUsuarioFK")]
)
data class DibujoEntity(
    @PrimaryKey(autoGenerate = true)
    val idDibujo: Long = 0L,
    val titulo: String,
    val fechaCreacion: Long,
    val idUsuarioFK: Long
)
