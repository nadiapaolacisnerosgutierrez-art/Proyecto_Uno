package com.jdl.practica1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jdl.practica1.ui.theme.AppTheme

class LienzoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                LienzoScreen(
                    onClose = { finish() },
                    onPublish = {
                        val intent = Intent(this, CompartirActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LienzoScreen(onClose: () -> Unit, onPublish: () -> Unit) {
    var thickness by remember { mutableStateOf(5f) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close)
                        )
                    }
                },
                title = { Text(stringResource(R.string.title_canvas_untitled)) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.cd_undo)
                        )
                    }
                    // Botón de paloma / checkmark para publicar o confirmar
                    IconButton(onClick = onPublish) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.cd_redo)
                        )
                    }
                    Button(
                        onClick = onPublish,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(R.string.btn_publish))
                    }
                },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val tertiaryColor = MaterialTheme.colorScheme.tertiary

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = tertiaryColor,
                        radius = 45f,
                        center = Offset(size.width - 80f, 90f)
                    )

                    val houseWidth = 180f
                    val houseHeight = 140f
                    val left = (size.width - houseWidth) / 2f
                    val top = (size.height - houseHeight) / 2f

                    drawRect(
                        color = primaryColor,
                        topLeft = Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(houseWidth, houseHeight),
                        style = Stroke(width = 4f)
                    )

                    val roofPath = Path().apply {
                        moveTo(left - 20f, top)
                        lineTo(left + houseWidth / 2f, top - 90f)
                        lineTo(left + houseWidth + 20f, top)
                        close()
                    }
                    drawPath(
                        path = roofPath,
                        color = secondaryColor,
                        style = Stroke(width = 4f)
                    )

                    val wavePath = Path().apply {
                        moveTo(0f, size.height - 80f)
                        quadraticTo(size.width / 4f, size.height - 120f, size.width / 2f, size.height - 80f)
                        quadraticTo(3f * size.width / 4f, size.height - 40f, size.width, size.height - 80f)
                    }
                    drawPath(
                        path = wavePath,
                        color = secondaryColor,
                        style = Stroke(width = 4f)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_tool_pencil))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.cd_tool_brush))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_tool_eraser))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.cd_tool_bucket))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Star, contentDescription = stringResource(R.string.cd_tool_shapes))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.error,
                    MaterialTheme.colorScheme.inversePrimary
                )
                colors.forEach { color ->
                    Surface(
                        shape = CircleShape,
                        color = color,
                        modifier = Modifier
                            .size(36.dp)
                    ) {}
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.cd_thickness_slider),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = thickness,
                    onValueChange = { thickness = it },
                    valueRange = 1f..50f
                )
            }
        }
    }
}
