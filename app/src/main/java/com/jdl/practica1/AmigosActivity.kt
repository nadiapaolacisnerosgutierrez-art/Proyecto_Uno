package com.jdl.practica1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jdl.practica1.data.GarabatoDbHelper
import com.jdl.practica1.domain.model.UsuarioModel
import com.jdl.practica1.ui.theme.AppTheme

class AmigosActivity : ComponentActivity() {
    private var currentUserId by mutableStateOf(1L)
    private var solicitudesList by mutableStateOf(emptyList<UsuarioModel>())
    private var amigosList by mutableStateOf(emptyList<UsuarioModel>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        loadData()

        setContent {
            AppTheme {
                var searchQuery by remember { mutableStateOf("") }
                var searchResults by remember { mutableStateOf(emptyList<UsuarioModel>()) }
                val dbHelper = GarabatoDbHelper.getInstance(applicationContext)

                LaunchedEffect(searchQuery, currentUserId) {
                    if (searchQuery.isNotBlank()) {
                        searchResults = dbHelper.buscarUsuarios(searchQuery, currentUserId)
                    } else {
                        searchResults = emptyList()
                    }
                }

                AmigosScreen(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    searchResults = searchResults,
                    solicitudes = solicitudesList,
                    amigos = amigosList,
                    currentUserId = currentUserId,
                    dbHelper = dbHelper,
                    onRefreshData = { loadData() },
                    onOpenLienzo = {
                        startActivity(Intent(this, LienzoActivity::class.java))
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val dbHelper = GarabatoDbHelper.getInstance(applicationContext)
        val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getLong("user_id", 1L)

        solicitudesList = dbHelper.obtenerSolicitudesPendientes(currentUserId)
        amigosList = dbHelper.obtenerAmigosAceptados(currentUserId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmigosScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<UsuarioModel>,
    solicitudes: List<UsuarioModel>,
    amigos: List<UsuarioModel>,
    currentUserId: Long,
    dbHelper: GarabatoDbHelper,
    onRefreshData: () -> Unit,
    onOpenLienzo: () -> Unit
) {
    val context = LocalContext.current

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Buscar amigos o usuarios") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Resultados de búsqueda
            if (searchResults.isNotEmpty()) {
                Text("Resultados de búsqueda", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                LazyColumn(
                    modifier = Modifier.heightIn(max = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { user ->
                        val estado = dbHelper.obtenerEstadoAmistad(currentUserId, user.idUsuario)
                        Card(modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = { Text(user.nombre) },
                                supportingContent = { Text(user.correo) },
                                trailingContent = {
                                    when (estado) {
                                        "aceptada" -> Text("Amigos", color = MaterialTheme.colorScheme.primary)
                                        "pendiente" -> Text("Solicitud enviada", color = MaterialTheme.colorScheme.secondary)
                                        else -> Button(
                                            onClick = {
                                                dbHelper.enviarSolicitudAmistad(currentUserId, user.idUsuario)
                                                onRefreshData()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Text("Enviar solicitud")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Tus amigos
            Text("Tus amigos · ${amigos.size}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            if (amigos.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Aún no tienes amigos agregados. Busca un usuario arriba para enviarle solicitud.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(amigos) { amigo ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = { Text(amigo.nombre) },
                                supportingContent = { Text(amigo.correo) },
                                trailingContent = {
                                    Button(
                                        onClick = onOpenLienzo,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    ) {
                                        Text("Dibujar")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Solicitudes recibidas (ubicadas debajo de Tus amigos)
            Text("Solicitudes recibidas · ${solicitudes.size}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

            if (solicitudes.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "No tienes solicitudes de amistad pendientes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(solicitudes) { sol ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = { Text(sol.nombre) },
                                supportingContent = { Text(sol.correo) },
                                trailingContent = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                dbHelper.aceptarSolicitud(sol.idUsuario, currentUserId)
                                                onRefreshData()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Text("Aceptar")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                dbHelper.rechazarSolicitud(sol.idUsuario, currentUserId)
                                                onRefreshData()
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text("Rechazar")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
