package com.jdl.practica1

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jdl.practica1.data.GarabatoDbHelper
import com.jdl.practica1.domain.model.DibujoModel
import com.jdl.practica1.ui.theme.AppTheme
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PerfilPastelColors = listOf(
    Color(0xFF9CF1EF),
    Color(0xFFD2E4FF),
    Color(0xFFFFDAD6),
    Color(0xFFFFF0C2),
    Color(0xFFE8D8FF),
    Color(0xFFCCE8E6)
)
private val PerfilPostCardColors = listOf(
    Color(0xFFE2F5F3),
    Color(0xFFE6F0FF),
    Color(0xFFFFECEA),
    Color(0xFFFFF8DF),
    Color(0xFFF2EAFF)
)

class PerfilActivity : ComponentActivity() {
    private var dibujosList by mutableStateOf(emptyList<DibujoModel>())
    private var currentUserId by mutableStateOf(-1L)
    private var totalDibujos by mutableStateOf(0)
    private var totalAmigos by mutableStateOf(0)
    private var totalMeGusta by mutableStateOf(0)
    private var userName by mutableStateOf("Usuario Garabato")
    private var userEmail by mutableStateOf("usuario@garabato.com")
    private var userDescription by mutableStateOf("")
    private var avatarPath by mutableStateOf<String?>(null)
    private var isGuest by mutableStateOf(false)

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { saveAvatarImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        loadUserData()

        setContent {
            AppTheme {
                var showPrivacyDialog by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                PerfilScreen(
                    isGuest = isGuest,
                    userName = userName,
                    userEmail = userEmail,
                    userDescription = userDescription,
                    totalDibujos = totalDibujos,
                    totalAmigos = totalAmigos,
                    totalMeGusta = totalMeGusta,
                    dibujos = dibujosList,
                    currentUserId = currentUserId,
                    avatarPath = avatarPath,
                    onAvatarClick = { if (!isGuest) imagePickerLauncher.launch("image/*") },
                    onLoginRedirect = {
                        val intent = Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    },
                    onLogout = {
                        val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
                        prefs.edit().clear().apply()

                        val intent = Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    },
                    onShowPrivacy = { showPrivacyDialog = true },
                    onDeleteDrawing = { dibujo ->
                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                GarabatoDbHelper.getInstance(applicationContext)
                                    .eliminarDibujoPropio(dibujo.idDibujo, currentUserId)
                            }
                            if (result.deleted) {
                                loadUserData()
                                if (!result.imageDeleted) {
                                    Toast.makeText(
                                        this@PerfilActivity,
                                        "El dibujo se eliminó, pero no se pudo borrar su archivo de imagen.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@PerfilActivity,
                                    "No se pudo eliminar el dibujo.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    onSaveProfile = { name, description, email ->
                        if (name.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            false
                        } else {
                            val values = android.content.ContentValues().apply {
                                put("nombre", name.trim())
                                put("descripcion", description.trim())
                                put("correo", email.trim())
                            }
                            val updated = GarabatoDbHelper.getInstance(applicationContext)
                                .writableDatabase
                                .update("usuario", values, "idUsuario = ?", arrayOf(getSharedPreferences(
                                    "garabato_user_prefs",
                                    Context.MODE_PRIVATE
                                ).getLong("user_id", 1L).toString()))
                            if (updated == 1) {
                                userName = name.trim()
                                userDescription = description.trim()
                                userEmail = email.trim()
                                getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE).edit()
                                    .putString("user_name", userName)
                                    .putString("user_email", userEmail)
                                    .putString("user_description", userDescription)
                                    .apply()
                                true
                            } else {
                                false
                            }
                        }
                    }
                )

                if (showPrivacyDialog) {
                    PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val dbHelper = GarabatoDbHelper.getInstance(applicationContext)
        val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
        isGuest = prefs.getBoolean("es_invitado", false)
        val userId = prefs.getLong("user_id", 1L)
        currentUserId = userId

        if (isGuest || userId == -1L) {
            userName = "Invitado"
            userEmail = "invitado@garabato.com"
            userDescription = ""
            dibujosList = emptyList()
            totalDibujos = 0
            totalAmigos = 0
            totalMeGusta = 0
            return
        }

        val usuarioModel = dbHelper.obtenerUsuarioPorId(userId)
        if (usuarioModel != null) {
            userName = usuarioModel.nombre
            userEmail = usuarioModel.correo
            userDescription = usuarioModel.descripcion
            avatarPath = usuarioModel.avatarUrl
        } else {
            userName = prefs.getString("user_name", "Usuario Garabato") ?: "Usuario Garabato"
            userEmail = prefs.getString("user_email", "usuario@garabato.com") ?: "usuario@garabato.com"
            userDescription = prefs.getString("user_description", "") ?: ""
        }

        dibujosList = dbHelper.obtenerDibujosPorUsuario(userId)
        totalDibujos = dbHelper.contarDibujosPorUsuario(userId)
        totalMeGusta = dbHelper.contarMeGustaRecibidosPorUsuario(userId)

        val friendsSet = prefs.getStringSet("friends_$userId", emptySet()) ?: emptySet()
        totalAmigos = friendsSet.size
    }

    private fun saveAvatarImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "avatar_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val path = file.absolutePath
            val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
            val userId = prefs.getLong("user_id", 1L)

            val dbHelper = GarabatoDbHelper.getInstance(applicationContext)
            val db = dbHelper.writableDatabase
            val values = android.content.ContentValues().apply {
                put("avatarUrl", path)
            }
            db.update("usuario", values, "idUsuario = ?", arrayOf(userId.toString()))

            avatarPath = path
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    isGuest: Boolean,
    userName: String,
    userEmail: String,
    userDescription: String,
    totalDibujos: Int,
    totalAmigos: Int,
    totalMeGusta: Int,
    dibujos: List<DibujoModel>,
    currentUserId: Long,
    avatarPath: String?,
    onAvatarClick: () -> Unit,
    onLoginRedirect: () -> Unit,
    onLogout: () -> Unit,
    onShowPrivacy: () -> Unit,
    onDeleteDrawing: (DibujoModel) -> Unit,
    onSaveProfile: (String, String, String) -> Boolean
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    if (!isGuest) {
                        TextButton(onClick = onLogout) {
                            Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
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
            if (isGuest) {
                // Mensaje claro para modo invitado
                Spacer(modifier = Modifier.height(32.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Estás en modo invitado. Inicia sesión o regístrate para ver tu perfil y guardar tus dibujos.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Button(
                            onClick = onLoginRedirect,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Iniciar Sesión")
                        }
                    }
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = PerfilPastelColors[4],
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAvatarClick)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF34434A)
                        )
                    }
                }

                Text(userName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    userDescription.ifBlank { "Comunidad de arte y dibujos" },
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalDibujos", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                        Text("Dibujos")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalAmigos", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                        Text("Amigos")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalMeGusta", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                        Text("Me gusta")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showEditProfileDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) { Text("Editar perfil") }
                    OutlinedButton(
                        onClick = onShowPrivacy,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) { Text("Aviso de Privacidad") }
                }

                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White
                ) {
                    listOf("Dibujos", "Redibujos", "Guardados").forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier.background(
                                if (selectedTab == index) PerfilPastelColors[index].copy(alpha = 0.45f)
                                else Color.Transparent
                            ),
                            selectedContentColor = PerfilPastelColors[index],
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            text = {
                                Text(
                                    label,
                                    color = if (selectedTab == index) Color(0xFF34434A)
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
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
                            itemsIndexed(
                                items = dibujos,
                                key = { _, dibujo -> dibujo.idDibujo }
                            ) { index, dibujo ->
                                val accentColor = PerfilPastelColors[index % PerfilPastelColors.size]
                                val cardShape = MaterialTheme.shapes.medium
                                var showDeleteConfirmation by remember { mutableStateOf(false) }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(155.dp)
                                        .border(1.dp, accentColor, cardShape),
                                    shape = cardShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = PerfilPostCardColors[index % PerfilPostCardColors.size]
                                    )
                                ) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        PerfilDrawingPreview(
                                            ruta = dibujo.rutaImagen,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .background(Color.White)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(accentColor.copy(alpha = 0.35f))
                                                .padding(start = 8.dp, end = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = dibujo.titulo,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.weight(1f)
                                            )
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
                                    }
                                }

                                if (showDeleteConfirmation) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteConfirmation = false },
                                        title = { Text("¿Eliminar este dibujo?") },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                showDeleteConfirmation = false
                                                onDeleteDrawing(dibujo)
                                            }) {
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

                                if (showEditProfileDialog) {
                                    var nameDraft by remember(userName, userEmail, userDescription) { mutableStateOf(userName) }
                                    var descriptionDraft by remember(userName, userEmail, userDescription) { mutableStateOf(userDescription) }
                                    var emailDraft by remember(userName, userEmail, userDescription) { mutableStateOf(userEmail) }
                                    var errorMessage by remember { mutableStateOf<String?>(null) }

                                    AlertDialog(
                                        onDismissRequest = { showEditProfileDialog = false },
                                        title = { Text("Editar perfil") },
                                        text = {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                    value = nameDraft,
                                                    onValueChange = { nameDraft = it },
                                                    label = { Text("Nombre") },
                                                    singleLine = true
                                                )
                                                OutlinedTextField(
                                                    value = descriptionDraft,
                                                    onValueChange = { descriptionDraft = it },
                                                    label = { Text("Descripción") }
                                                )
                                                OutlinedTextField(
                                                    value = emailDraft,
                                                    onValueChange = { emailDraft = it },
                                                    label = { Text("Correo") },
                                                    singleLine = true
                                                )
                                                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                if (nameDraft.isBlank() || emailDraft.isBlank()) {
                                                    errorMessage = "El nombre y el correo son obligatorios."
                                                } else if (onSaveProfile(nameDraft, descriptionDraft, emailDraft)) {
                                                    showEditProfileDialog = false
                                                } else {
                                                    errorMessage = "Revisa el correo e intenta guardar de nuevo."
                                                }
                                            }) {
                                                Text("Guardar")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showEditProfileDialog = false }) {
                                                Text("Cancelar")
                                            }
                                        }
                                    )
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
}

@Composable
private fun PerfilDrawingPreview(ruta: String?, modifier: Modifier = Modifier) {
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
            modifier = modifier.padding(6.dp)
        )
    } else {
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        Canvas(modifier = modifier) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(color = primaryColor, radius = 30f, center = Offset(cx, cy))
            drawCircle(color = secondaryColor, radius = 4f, center = Offset(cx - 10f, cy - 5f))
            drawCircle(color = secondaryColor, radius = 4f, center = Offset(cx + 10f, cy - 5f))
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
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Aceptar / Cerrar")
            }
        }
    )
}
