package com.jdl.practica1.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "me_gusta",
    primaryKeys = ["idUsuarioFK", "idDibujoFK"],
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
    indices = [Index("idDibujoFK")]
)
data class MeGustaEntity(
    val idUsuarioFK: Long,
    val idDibujoFK: Long,
    val fecha: Long
)
