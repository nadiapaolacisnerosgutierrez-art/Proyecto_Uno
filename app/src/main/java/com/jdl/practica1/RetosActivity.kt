package com.jdl.practica1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jdl.practica1.ui.theme.AppTheme

class RetosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                RetosScreen(
                    onParticipar = { retoTitulo ->
                        val intent = Intent(this, LienzoActivity::class.java).apply {
                            putExtra("RETO_TITULO", retoTitulo)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetosScreen(onParticipar: (String) -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Retos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            GarabatoBottomBar(context = context, currentTab = 1)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reto del día destacado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("RETO DEL DÍA", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Dibuja tu comida favorita", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onParticipar("Dibuja tu comida favorita") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Participar")
                    }
                }
            }

            Text("Más retos", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onParticipar("Flores a mano alzada") }
            ) {
                ListItem(
                    headlineContent = { Text("Flores a mano alzada") },
                    supportingContent = { Text("80 participantes · Termina en 2 días") },
                    leadingContent = { Icon(Icons.Default.Star, contentDescription = null) }
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onParticipar("Un día en la playa") }
            ) {
                ListItem(
                    headlineContent = { Text("Un día en la playa") },
                    supportingContent = { Text("152 participantes · Termina en 4 días") },
                    leadingContent = { Icon(Icons.Default.Star, contentDescription = null) }
                )
            }
        }
    }
}
