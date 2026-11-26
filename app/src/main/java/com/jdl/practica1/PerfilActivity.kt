package com.jdl.practica1

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jdl.practica1.data.GarabatoDbHelper
import com.jdl.practica1.domain.model.DibujoModel
import com.jdl.practica1.ui.theme.AppTheme

class PerfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dbHelper = GarabatoDbHelper.getInstance(applicationContext)
        val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getLong("user_id", 1L)
        val userName = prefs.getString("user_name", "Usuario Garabato") ?: "Usuario Garabato"
        val userEmail = prefs.getString("user_email", "usuario@garabato.com") ?: "usuario@garabato.com"

        setContent {
            AppTheme {
                var dibujosList by remember { mutableStateOf(emptyList<DibujoModel>()) }
                var totalDibujos by remember { mutableStateOf(0) }
                var showPrivacyDialog by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    dibujosList = dbHelper.obtenerDibujosPorUsuario(userId)
                    totalDibujos = dbHelper.contarDibujosPorUsuario(userId)
                }

                PerfilScreen(
                    userName = userName,
                    userEmail = userEmail,
                    totalDibujos = totalDibujos,
                    dibujos = dibujosList,
                    onShowPrivacy = { showPrivacyDialog = true }
                )

                if (showPrivacyDialog) {
                    PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    userName: String,
    userEmail: String,
    totalDibujos: Int,
    dibujos: List<DibujoModel>,
    onShowPrivacy: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            GarabatoBottomBar(context = context, currentTab = 4)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Text(userName, style = MaterialTheme.typography.titleLarge)
            Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Comunidad de arte y dibujos", style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$totalDibujos", style = MaterialTheme.typography.titleMedium)
                    Text("Dibujos")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("126", style = MaterialTheme.typography.titleMedium)
                    Text("Amigos")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1.2 k", style = MaterialTheme.typography.titleMedium)
                    Text("Me gusta")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}) { Text("Editar perfil") }
                OutlinedButton(onClick = onShowPrivacy) { Text("Aviso de Privacidad") }
            }

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Dibujos") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Redibujos") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Guardados") })
            }

            if (selectedTab == 0) {
                if (dibujos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay dibujos publicados aún. ¡Crea uno!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dibujos) { dibujo ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "🎨",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        text = dibujo.titulo,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Contenido de la pestaña", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aviso de Privacidad - Garabato") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Bienvenido a la política de privacidad de Garabato.")
                Text("1. Información que recopilamos: Recopilamos el nombre de usuario, correo electrónico y los dibujos creados o compartidos dentro de la aplicación para ofrecerte una experiencia comunitaria segura.")
                Text("2. Uso de los datos: Tu nombre y correo se utilizan exclusivamente para gestionar tu cuenta e identidad en la app. Tus dibujos se almacenan localmente y se comparten con tus amigos y seguidores dentro de la plataforma.")
                Text("3. Seguridad: Protegemos tus datos almacenándolos mediante una base de datos local segura (SQLite / Room).")
                Text("4. Tus derechos: Puedes acceder, modificar o eliminar tus dibujos y datos en cualquier momento desde la configuración de tu perfil.")
                Text("Fecha de última actualización: Noviembre de 2026.")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Aceptar / Cerrar")
            }
        }
    )
}
