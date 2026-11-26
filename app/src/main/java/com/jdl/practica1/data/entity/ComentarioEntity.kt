package com.jdl.practica1.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comentario",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["idUsuario"],
            childColumns = ["idUsuarioFK"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DibujoEntity::class,
            parentColumns = ["idDibujo"],
            childColumns = ["idDibujoFK"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idUsuarioFK"), Index("idDibujoFK")]
)
data class ComentarioEntity(
    @PrimaryKey(autoGenerate = true)
    val idComentario: Long = 0L,
    val contenido: String,
    val fecha: Long,
    val idUsuarioFK: Long,
    val idDibujoFK: Long
)
