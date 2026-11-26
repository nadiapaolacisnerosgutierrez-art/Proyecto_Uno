package com.jdl.practica1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jdl.practica1.ui.theme.AppTheme

data class AmigoItem(val id: Int, val nombre: String, val descripcion: String, var estado: String)

class AmigosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                AmigosScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmigosScreen() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val solicitudes = remember {
        mutableStateListOf(
            AmigoItem(1, "Fer Núñez", "Dibuja con acuarela", "Pendiente"),
            AmigoItem(2, "Gus Mora", "Amigo de reto", "Pendiente")
        )
    }

    val amigos = remember {
        mutableStateListOf(
            AmigoItem(3, "Ana López", "Dibujando ahora", "Amigo"),
            AmigoItem(4, "Beto Ruiz", "Compartió un dibujo hace 1 h", "Amigo"),
            AmigoItem(5, "Caro Vega", "Activa ayer", "Amigo")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amigos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            GarabatoBottomBar(context = context, currentTab = 3)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar amigos o usuarios") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Solicitudes · ${solicitudes.size}", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.heightIn(max = 180.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(solicitudes) { solicitud ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = { Text(solicitud.nombre) },
                            supportingContent = { Text(solicitud.descripcion) },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        solicitudes.remove(solicitud)
                                        amigos.add(solicitud.copy(estado = "Amigo"))
                                    }) { Text("Aceptar") }
                                    OutlinedButton(onClick = {
                                        solicitudes.remove(solicitud)
                                    }) { Text("X") }
                                }
                            }
                        )
                    }
                }
            }

            Text("Tus amigos", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(amigos) { amigo ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            headlineContent = { Text(amigo.nombre) },
                            supportingContent = { Text(amigo.descripcion) },
                            trailingContent = { Button(onClick = {}) { Text("Dibujar") } }
                        )
                    }
                }
            }
        }
    }
}
