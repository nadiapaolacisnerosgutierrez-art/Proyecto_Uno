package com.jdl.practica1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jdl.practica1.ui.theme.AppTheme

class SubirDibujoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                SubirDibujoScreen(onClose = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubirDibujoScreen(onClose: () -> Unit) {
    var option by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                },
                title = { Text("Subir dibujo") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("arconts.png - 1.2 MB", style = MaterialTheme.typography.titleMedium)
                }
            }

            Text("¿Qué quieres que hagan con él?", style = MaterialTheme.typography.titleMedium)
            
            Card(modifier = Modifier.fillMaxWidth()) {
                RadioButton(selected = option == 0, onClick = { option = 0 })
                Text("Colorear (Otros pintan tus contornos)")
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                RadioButton(selected = option == 1, onClick = { option = 1 })
                Text("Terminar mi dibujo")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Publicar para redibujar")
            }
        }
    }
}
