package com.jdl.practica1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jdl.practica1.ui.theme.AppTheme

class PerfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                PerfilScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
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
                    Text("S", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Text("Sofía Ramos", style = MaterialTheme.typography.titleLarge)
            Text("@sofia.dibuja", style = MaterialTheme.typography.bodyMedium)
            Text("Gatos, flores y monstruos tiernos.", style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("48", style = MaterialTheme.typography.titleMedium)
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
                OutlinedButton(onClick = {}) { Text("Compartir perfil") }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Dibujos") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Redibujos") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Guardados") })
            }
        }
    }
}
