package com.jdl.practica1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.jdl.practica1.data.GarabatoDatabase
import com.jdl.practica1.data.entity.ComentarioEntity
import com.jdl.practica1.data.entity.DibujoEntity
import com.jdl.practica1.data.entity.MeGustaEntity
import com.jdl.practica1.data.entity.UsuarioEntity
import com.jdl.practica1.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val database = GarabatoDatabase.getInstance(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Insertar USUARIO
            val usuarioId = database.usuarioDao().insertarUsuario(
                UsuarioEntity(
                    nombre = "Ana García",
                    correo = "ana.garcia@garabato.com",
                    avatarUrl = null
                )
            )


            val dibujoId = database.dibujoDao().insertarDibujo(
                DibujoEntity(
                    titulo = "Atardecer en la Montaña",
                    fechaCreacion = System.currentTimeMillis(),
                    idUsuarioFK = usuarioId
                )
            )


            database.comentarioDao().insertarComentario(
                ComentarioEntity(
                    contenido = "¡Qué hermosos colores!",
                    fecha = System.currentTimeMillis(),
                    idUsuarioFK = usuarioId,
                    idDibujoFK = dibujoId
                )
            )


            database.meGustaDao().insertarMeGusta(
                MeGustaEntity(
                    idUsuarioFK = usuarioId,
                    idDibujoFK = dibujoId,
                    fecha = System.currentTimeMillis()
                )
            )
        }

        setContent {
            AppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo de la Aplicación",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(240.dp)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
