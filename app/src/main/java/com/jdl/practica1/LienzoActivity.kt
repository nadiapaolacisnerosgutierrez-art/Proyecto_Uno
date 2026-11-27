package com.jdl.practica1

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.jdl.practica1.data.GarabatoDbHelper
import com.jdl.practica1.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getLong("user_id", 1L)

        setContent {
            AppTheme {
                val coroutineScope = rememberCoroutineScope()
                LienzoScreen(
                    initialTitle = initialTitle,
                    onClose = { finish() },
                    onPublish = { drawingTitle, drawingLines, drawingSize ->
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                val bitmap = renderDrawingBitmap(drawingLines, drawingSize)
                                val imageFile = File(filesDir, "dibujo_${UUID.randomUUID()}.png")
                                var inserted = false
                                try {
                                    val compressed = FileOutputStream(imageFile).use {
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                                    }
                                    if (!compressed) {
                                        throw IllegalStateException("No se pudo guardar la imagen del dibujo.")
                                    }
                                    val db = dbHelper.writableDatabase
                                    val values = android.content.ContentValues().apply {
                                        put("titulo", if (drawingTitle.isBlank()) "Lienzo sin título" else drawingTitle)
                                        put("fechaCreacion", System.currentTimeMillis())
                                        put("idUsuarioFK", currentUserId)
                                        put("rutaImagen", imageFile.absolutePath)
                                    }
                                    val insertedId = db.insert("dibujo", null, values)
                                    if (insertedId == -1L) {
                                        throw IllegalStateException("No se pudo publicar el dibujo en la base de datos.")
                                    }
                                    inserted = true
                                } finally {
                                    bitmap.recycle()
                                    if (!inserted) {
                                        imageFile.delete()
                                    }
                                }
                            }
                            val intent = Intent(this@LienzoActivity, PerfilActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(intent)
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LienzoScreen(
    initialTitle: String,
    onClose: () -> Unit,
    onPublish: (String, List<LineData>, IntSize) -> Unit
) {
    var thickness by remember { mutableStateOf(8f) }
    val lines = remember { mutableStateListOf<LineData>() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer

    var selectedColor by remember(primaryColor) { mutableStateOf(primaryColor) }
    var drawingTitle by remember { mutableStateOf(initialTitle) }

    // Paleta con colores de tema y variados
    val colors = listOf(
        primaryColor,
        secondaryColor,
        tertiaryColor,
        primaryContainerColor,
        secondaryContainerColor,
        tertiaryContainerColor,
        Color(0xFFFF6B6B), // Rojo coral
        Color(0xFFFF9F1C), // Naranja brillante
        Color(0xFFFFC93C), // Amarillo sol
        Color(0xFF52B788), // Verde claro
        Color(0xFF4EA8DE), // Azul cielo
        Color(0xFFFF70A6), // Rosa pastel
        Color.Black,       // Negro
        Color.White        // Blanco
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
                        onClick = {
                            onPublish(drawingTitle, lines.toList(), canvasSize)
                        },
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
                        .onSizeChanged { canvasSize = it }
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
                    IconButton(onClick = { selectedColor = primaryColor }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_tool_pencil), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { selectedColor = secondaryColor }) {
                        Icon(Icons.Default.Create, contentDescription = stringResource(R.string.cd_tool_brush), tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = { selectedColor = Color.White }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_tool_eraser))
                    }
                    IconButton(onClick = { selectedColor = tertiaryColor }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.cd_tool_bucket), tint = MaterialTheme.colorScheme.tertiary)
                    }
                    IconButton(onClick = { selectedColor = primaryContainerColor }) {
                        Icon(Icons.Default.Star, contentDescription = stringResource(R.string.cd_tool_shapes), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Fila deslizable ampliada con más de 12 colores e indicador de selección visual
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
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
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

private fun renderDrawingBitmap(lines: List<LineData>, size: IntSize): Bitmap {
    val bitmap = Bitmap.createBitmap(
        size.width.coerceAtLeast(1),
        size.height.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(AndroidColor.WHITE)
    lines.forEach { line ->
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = line.color.toArgb()
            strokeWidth = line.strokeWidth
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        canvas.drawLine(line.start.x, line.start.y, line.end.x, line.end.y, paint)
    }
    return bitmap
}
