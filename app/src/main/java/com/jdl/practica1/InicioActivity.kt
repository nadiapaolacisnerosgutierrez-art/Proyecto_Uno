package com.jdl.practica1

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import com.jdl.practica1.data.GarabatoDbHelper
import com.jdl.practica1.domain.model.DibujoModel
import com.jdl.practica1.domain.model.UsuarioModel
import com.jdl.practica1.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val InicioPastelColors = listOf(
    Color(0xFF9CF1EF),
    Color(0xFFD2E4FF),
    Color(0xFFFFDAD6),
    Color(0xFFFFF0C2),
    Color(0xFFE8D8FF),
    Color(0xFFCCE8E6)
)
private val InicioPostCardColors = listOf(
    Color(0xFFE2F5F3),
    Color(0xFFE6F0FF),
    Color(0xFFFFECEA),
    Color(0xFFFFF8DF),
    Color(0xFFF2EAFF)
)

private data class InicioFeedData(
    val dibujos: List<DibujoModel>,
    val autores: Map<Long, String>,
    val amigosIds: Set<Long>,
    val likesPorDibujo: Map<Long, Int>
)

class InicioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dbHelper = GarabatoDbHelper.getInstance(applicationContext)
        val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getLong("user_id", 1L)

        setContent {
            AppTheme {
                var dibujosList by remember { mutableStateOf(emptyList<DibujoModel>()) }
                var autores by remember { mutableStateOf(emptyMap<Long, String>()) }
                var amigosIds by remember { mutableStateOf(emptySet<Long>()) }
                var likesPorDibujo by remember { mutableStateOf(emptyMap<Long, Int>()) }
                val coroutineScope = rememberCoroutineScope()

                val refreshFeed: suspend () -> Unit = {
                    val feed = withContext(Dispatchers.IO) {
                        val drawings = dbHelper.obtenerDibujos()
                        val userNames = dbHelper.obtenerUsuarios().associate { it.idUsuario to it.nombre }
                        val friendIds = dbHelper.obtenerAmigosAceptados(currentUserId)
                            .mapTo(mutableSetOf()) { it.idUsuario }
                        val likes = mutableMapOf<Long, Int>()
                        val cursor = dbHelper.readableDatabase.rawQuery(
                            "SELECT idDibujoFK, COUNT(*) FROM me_gusta GROUP BY idDibujoFK",
                            null
                        )
                        if (cursor.moveToFirst()) {
                            do {
                                likes[cursor.getLong(0)] = cursor.getInt(1)
                            } while (cursor.moveToNext())
                        }
                        cursor.close()
                        InicioFeedData(drawings, userNames, friendIds, likes)
                    }
                    dibujosList = feed.dibujos
                    autores = feed.autores
                    amigosIds = feed.amigosIds
                    likesPorDibujo = feed.likesPorDibujo
                }

                LaunchedEffect(Unit) {
                    refreshFeed()
                }

                InicioScreen(
                    dibujos = dibujosList,
                    currentUserId = currentUserId,
                    dbHelper = dbHelper,
                    coroutineScope = coroutineScope,
                    autores = autores,
                    amigosIds = amigosIds,
                    likesPorDibujo = likesPorDibujo,
                    onLikesChanged = { dibujoId, count ->
                        likesPorDibujo = likesPorDibujo + (dibujoId to count)
                    },
                    onDeleteDrawing = { dibujo ->
                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                dbHelper.eliminarDibujoPropio(dibujo.idDibujo, currentUserId)
                            }
                            if (result.deleted) {
                                refreshFeed()
                                if (!result.imageDeleted) {
                                    Toast.makeText(
                                        this@InicioActivity,
                                        "El dibujo se eliminó, pero no se pudo borrar su archivo de imagen.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@InicioActivity,
                                    "No se pudo eliminar el dibujo.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    onOpenLienzo = {
                        startActivity(Intent(this, LienzoActivity::class.java))
                    },
                    onOpenRetos = {
                        startActivity(Intent(this, RetosActivity::class.java))
                    },
                    onOpenAmigos = {
                        startActivity(Intent(this, AmigosActivity::class.java))
                    },
                    onOpenPerfil = {
                        startActivity(Intent(this, PerfilActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    dibujos: List<DibujoModel>,
    currentUserId: Long,
    dbHelper: GarabatoDbHelper,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    autores: Map<Long, String>,
    amigosIds: Set<Long>,
    likesPorDibujo: Map<Long, Int>,
    onLikesChanged: (Long, Int) -> Unit,
    onDeleteDrawing: (DibujoModel) -> Unit,
    onOpenLienzo: () -> Unit,
    onOpenRetos: () -> Unit,
    onOpenAmigos: () -> Unit,
    onOpenPerfil: () -> Unit
) {
    var selectedChip by remember { mutableStateOf(0) }
    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showNotifications by remember { mutableStateOf(false) }
    var solicitudesPendientes by remember { mutableStateOf(emptyList<UsuarioModel>()) }
    var likesRecientes by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    val chips = listOf(
        stringResource(R.string.chip_for_you),
        stringResource(R.string.chip_friends),
        stringResource(R.string.chip_challenges),
        stringResource(R.string.chip_trends)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "Logo de la app",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            stringResource(R.string.title_garabato),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { searchActive = !searchActive }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.cd_search),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(onClick = {
                        showNotifications = true
                        coroutineScope.launch(Dispatchers.IO) {
                            solicitudesPendientes = dbHelper.obtenerSolicitudesPendientes(currentUserId)
                            val recientes = mutableListOf<Pair<String, String>>()
                            val cursor = dbHelper.readableDatabase.rawQuery(
                                """
                                    SELECT u.nombre, d.titulo
                                    FROM me_gusta mg
                                    JOIN dibujo d ON d.idDibujo = mg.idDibujoFK
                                    JOIN usuario u ON u.idUsuario = mg.idUsuarioFK
                                    WHERE d.idUsuarioFK = ? AND mg.idUsuarioFK != ?
                                    ORDER BY mg.fecha DESC
                                    LIMIT 10
                                """.trimIndent(),
                                arrayOf(currentUserId.toString(), currentUserId.toString())
                            )
                            if (cursor.moveToFirst()) {
                                do {
                                    recientes.add(cursor.getString(0) to cursor.getString(1))
                                } while (cursor.moveToNext())
                            }
                            cursor.close()
                            likesRecientes = recientes
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.cd_notifications),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            GarabatoBottomBar(context = LocalContext.current, currentTab = 0)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val initials = listOf("A", "B", "C", "D", "E")
                initials.forEachIndexed { index, initial ->
                    Surface(
                        shape = CircleShape,
                        color = InicioPastelColors[index % InicioPastelColors.size],
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEachIndexed { index, chipText ->
                    FilterChip(
                        selected = selectedChip == index,
                        onClick = { selectedChip = index },
                        label = { Text(chipText) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = InicioPastelColors[index % InicioPastelColors.size],
                            selectedLabelColor = Color(0xFF34434A)
                        )
                    )
                }
            }

            if (searchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar dibujos por título o autor") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchActive = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda")
                        }
                    },
                    singleLine = true
                )
            }

            val recentThreshold = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
            val feedByTab = when (selectedChip) {
                1 -> dibujos.filter { it.idUsuarioFK in amigosIds }
                2 -> dibujos.filter { it.fechaCreacion >= recentThreshold }
                3 -> dibujos.sortedWith(
                    compareByDescending<DibujoModel> { likesPorDibujo[it.idDibujo] ?: 0 }
                        .thenByDescending { it.fechaCreacion }
                )
                else -> dibujos
            }
            val visibleDrawings = feedByTab.filter { dibujo ->
                searchQuery.isBlank() ||
                    dibujo.titulo.contains(searchQuery, ignoreCase = true) ||
                    autores[dibujo.idUsuarioFK].orEmpty().contains(searchQuery, ignoreCase = true)
            }

            if (visibleDrawings.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (searchQuery.isNotBlank()) "No se encontraron dibujos para esta búsqueda."
                            else "No hay dibujos para mostrar en esta pestaña.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                visibleDrawings.forEachIndexed { index, dibujo ->
                    key(dibujo.idDibujo) {
                        PostCardItem(
                            dibujo = dibujo,
                            index = index,
                            autorNombre = autores[dibujo.idUsuarioFK] ?: "Usuario",
                            currentUserId = currentUserId,
                            dbHelper = dbHelper,
                            coroutineScope = coroutineScope,
                            onLikesChanged = onLikesChanged,
                            onDeleteDrawing = onDeleteDrawing,
                            onOpenLienzo = onOpenLienzo
                        )
                    }
                }
            }
        }
    }

    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            title = { Text("Notificaciones") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Solicitudes de amistad · ${solicitudesPendientes.size}", style = MaterialTheme.typography.titleSmall)
                    if (solicitudesPendientes.isEmpty()) {
                        Text("No tienes solicitudes pendientes.")
                    } else {
                        solicitudesPendientes.forEach { solicitud ->
                            Text(solicitud.nombre, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    HorizontalDivider()
                    Text("Me gusta recientes", style = MaterialTheme.typography.titleSmall)
                    if (likesRecientes.isEmpty()) {
                        Text("Aún no tienes likes recientes.")
                    } else {
                        likesRecientes.forEach { (autor, titulo) ->
                            Text("$autor indicó que le gusta «$titulo».", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifications = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun InicioDrawingPreview(ruta: String?, modifier: Modifier = Modifier) {
    val bitmapState = remember(ruta) { mutableStateOf<Bitmap?>(null) }
    val bitmap by bitmapState

    LaunchedEffect(ruta) {
        bitmapState.value = withContext(Dispatchers.IO) {
            ruta?.let { BitmapFactory.decodeFile(it) }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Previsualización del dibujo",
            contentScale = ContentScale.Fit,
            modifier = modifier.padding(8.dp)
        )
    } else {
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        Canvas(modifier = modifier) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(color = primaryColor, radius = 60f, center = Offset(cx, cy))
            drawCircle(color = secondaryColor, radius = 6f, center = Offset(cx - 20f, cy - 10f))
            drawCircle(color = secondaryColor, radius = 6f, center = Offset(cx + 20f, cy - 10f))
        }
    }
}

@Composable
fun PostCardItem(
    dibujo: DibujoModel,
    index: Int,
    autorNombre: String,
    currentUserId: Long,
    dbHelper: GarabatoDbHelper,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onLikesChanged: (Long, Int) -> Unit,
    onDeleteDrawing: (DibujoModel) -> Unit,
    onOpenLienzo: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(0) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var commentsList by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    var newCommentText by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(dibujo.idDibujo) {
        coroutineScope.launch(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursorLikes = db.rawQuery("SELECT COUNT(*) FROM me_gusta WHERE idDibujoFK = ?", arrayOf(dibujo.idDibujo.toString()))
            if (cursorLikes.moveToFirst()) likesCount = cursorLikes.getInt(0)
            cursorLikes.close()

            val cursorUserLike = db.rawQuery("SELECT * FROM me_gusta WHERE idUsuarioFK = ? AND idDibujoFK = ?", arrayOf(currentUserId.toString(), dibujo.idDibujo.toString()))
            isLiked = cursorUserLike.moveToFirst()
            cursorUserLike.close()

            val comments = mutableListOf<Pair<String, String>>()
            val cursorCom = db.rawQuery("""
                SELECT u.nombre, c.contenido FROM comentario c 
                JOIN usuario u ON c.idUsuarioFK = u.idUsuario 
                WHERE c.idDibujoFK = ? ORDER BY c.fecha ASC
            """, arrayOf(dibujo.idDibujo.toString()))
            if (cursorCom.moveToFirst()) {
                do {
                    val autor = cursorCom.getString(0)
                    val texto = cursorCom.getString(1)
                    comments.add(autor to texto)
                } while (cursorCom.moveToNext())
            }
            cursorCom.close()
            commentsList = comments
        }
    }

    val accentColor = InicioPastelColors[(dibujo.idDibujo % InicioPastelColors.size).toInt()]
    val cardShape = MaterialTheme.shapes.medium
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor, cardShape),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = InicioPostCardColors[index % InicioPostCardColors.size]
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = autorNombre.take(1).uppercase(),
                            color = Color(0xFF34434A),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = autorNombre,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = dibujo.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (dibujo.idUsuarioFK == currentUserId) {
                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar dibujo",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color.White
            ) {
                InicioDrawingPreview(
                    ruta = dibujo.rutaImagen,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("¿Eliminar este dibujo?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirmation = false
                                onDeleteDrawing(dibujo)
                            }
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val db = dbHelper.writableDatabase
                                    if (isLiked) {
                                        db.delete("me_gusta", "idUsuarioFK = ? AND idDibujoFK = ?", arrayOf(currentUserId.toString(), dibujo.idDibujo.toString()))
                                        isLiked = false
                                        likesCount = (likesCount - 1).coerceAtLeast(0)
                                    } else {
                                        val values = android.content.ContentValues().apply {
                                            put("idUsuarioFK", currentUserId)
                                            put("idDibujoFK", dibujo.idDibujo)
                                            put("fecha", System.currentTimeMillis())
                                        }
                                        db.insert("me_gusta", null, values)
                                        isLiked = true
                                        likesCount += 1
                                    }
                                    onLikesChanged(dibujo.idDibujo, likesCount)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = stringResource(R.string.cd_like),
                                tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$likesCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { showCommentsDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = stringResource(R.string.cd_comment),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = "${commentsList.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onOpenLienzo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Text(stringResource(R.string.btn_redraw))
                }
            }
        }
    }

    if (showCommentsDialog) {
        AlertDialog(
            onDismissRequest = { showCommentsDialog = false },
            title = { Text("Comentarios (${commentsList.size})") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (commentsList.isEmpty()) {
                        Text("No hay comentarios aún. ¡Sé el primero en comentar!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        commentsList.forEach { (autor, texto) ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = autor, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = texto, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Escribe un comentario...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCommentText.isNotBlank()) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val db = dbHelper.writableDatabase
                                val values = android.content.ContentValues().apply {
                                    put("contenido", newCommentText.trim())
                                    put("fecha", System.currentTimeMillis())
                                    put("idUsuarioFK", currentUserId)
                                    put("idDibujoFK", dibujo.idDibujo)
                                }
                                db.insert("comentario", null, values)

                                val comments = mutableListOf<Pair<String, String>>()
                                val cursorCom = db.rawQuery("""
                                    SELECT u.nombre, c.contenido FROM comentario c 
                                    JOIN usuario u ON c.idUsuarioFK = u.idUsuario 
                                    WHERE c.idDibujoFK = ? ORDER BY c.fecha ASC
                                """, arrayOf(dibujo.idDibujo.toString()))
                                if (cursorCom.moveToFirst()) {
                                    do {
                                        val autor = cursorCom.getString(0)
                                        val texto = cursorCom.getString(1)
                                        comments.add(autor to texto)
                                    } while (cursorCom.moveToNext())
                                }
                                cursorCom.close()
                                commentsList = comments
                                newCommentText = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Comentar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommentsDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
