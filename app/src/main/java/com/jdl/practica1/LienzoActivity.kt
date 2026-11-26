package com.jdl.practica1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jdl.practica1.data.GarabatoDbHelper
import com.jdl.practica1.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class LineData(
    val start: Offset,
    val end: Offset,
    val color: Color,
    val strokeWidth: Float
)

class LienzoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dbHelper = GarabatoDbHelper.getInstance(applicationContext)
        val initialTitle = intent.getStringExtra("RETO_TITULO") ?: ""

        setContent {
            AppTheme {
                val coroutineScope = rememberCoroutineScope()
                LienzoScreen(
                    initialTitle = initialTitle,
                    onClose = { finish() },
                    onPublish = { drawingTitle ->
                        // Obtener idUsuario activo desde SharedPreferences con respaldo en garabato.db
                        val prefs = getSharedPreferences("garabato_user_prefs", MODE_PRIVATE)
                        var activeUserId = prefs.getLong("user_id", 1L)
                        if (activeUserId <= 0L) {
                            val usuarios = dbHelper.obtenerUsuarios()
                            if (usuarios.isNotEmpty()) {
                                activeUserId = usuarios.last().idUsuario
                            } else {
                                val values = android.content.ContentValues().apply {
                                    put("nombre", "Nadis")
                                    put("correo", "nadis@garabato.com")
                                }
                                activeUserId = dbHelper.writableDatabase.insert("usuario", null, values)
                            }
                        }

                        coroutineScope.launch(Dispatchers.IO) {
                            val db = dbHelper.writableDatabase
                            val values = android.content.ContentValues().apply {
                                put("titulo", if (drawingTitle.isBlank()) "Lienzo sin título" else drawingTitle)
                                put("fechaCreacion", System.currentTimeMillis())
                                put("idUsuarioFK", activeUserId)
                            }
                            db.insert("dibujo", null, values)
                        }

                        val intent = Intent(this, PerfilActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LienzoScreen(initialTitle: String, onClose: () -> Unit, onPublish: (String) -> Unit) {
    var thickness by remember { mutableStateOf(8f) }
    val lines = remember { mutableStateListOf<LineData>() }
    var selectedColor by remember { mutableStateOf(Color(0xFF6C4CF1)) }
    var drawingTitle by remember { mutableStateOf(initialTitle) }

    val colors = listOf(
        Color(0xFF6C4CF1),
        Color(0xFF2EC4B6),
        Color(0xFFFF6B6B),
        Color(0xFFFFC93C),
        Color(0xFF1F1B2E),
        Color.Black,
        Color.White
    )

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
                title = {
                    OutlinedTextField(
                        value = drawingTitle,
                        onValueChange = { drawingTitle = it },
                        placeholder = { Text(stringResource(R.string.title_canvas_untitled)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )
                },
                actions = {
                    IconButton(onClick = { lines.clear() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.cd_undo)
                        )
                    }
                    Button(
                        onClick = { onPublish(drawingTitle) },
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
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedColor, thickness) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val line = LineData(
                                    start = change.position - dragAmount,
                                    end = change.position,
                                    color = selectedColor,
                                    strokeWidth = thickness
                                )
                                lines.add(line)
                            }
                        }
                ) {
                    lines.forEach { line ->
                        drawLine(
                            color = line.color,
                            start = line.start,
                            end = line.end,
                            strokeWidth = line.strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
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
                    IconButton(onClick = { selectedColor = Color(0xFF6C4CF1) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_tool_pencil))
                    }
                    IconButton(onClick = { selectedColor = Color(0xFF2EC4B6) }) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.cd_tool_brush))
                    }
                    IconButton(onClick = { selectedColor = Color.White }) {
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
                colors.forEach { color ->
                    val isSelected = selectedColor == color
                    Surface(
                        shape = CircleShape,
                        color = color,
                        modifier = Modifier
                            .size(if (isSelected) 42.dp else 36.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = CircleShape
                            )
                            .then(
                                Modifier.clickable { selectedColor = color }
                            )
                    ) {}
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${stringResource(R.string.cd_thickness_slider)}: ${thickness.toInt()} px",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = thickness,
                    onValueChange = { thickness = it },
                    valueRange = 2f..40f
                )
            }
        }
    }
}
