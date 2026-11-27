package com.jdl.practica1

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jdl.practica1.data.GarabatoDbHelper
import com.jdl.practica1.ui.theme.AppTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dbHelper = GarabatoDbHelper.getInstance(applicationContext)

        setContent {
            AppTheme {
                var showPrivacyDialog by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                LoginScreen(
                    errorMessage = errorMessage,
                    onLoginAttempt = { email, password, name, isRegistering ->
                        if (email.isBlank() || password.isBlank() || (isRegistering && name.isBlank())) {
                            errorMessage = "Por favor, completa todos los campos."
                            return@LoginScreen
                        }

                        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            errorMessage = "El formato del correo electrónico no es válido."
                            return@LoginScreen
                        }

                        val db = dbHelper.writableDatabase

                        if (isRegistering) {
                            val values = ContentValues().apply {
                                put("nombre", name.trim())
                                put("correo", email.trim())
                            }
                            val userId = db.insert("usuario", null, values)
                            if (userId != -1L) {
                                val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
                                prefs.edit().apply {
                                    putLong("user_id", userId)
                                    putString("user_email", email.trim())
                                    putString("user_name", name.trim())
                                    putBoolean("es_invitado", false)
                                    apply()
                                }
                                startActivity(Intent(this, InicioActivity::class.java))
                                finish()
                            } else {
                                errorMessage = "Error al registrar el usuario en la base de datos."
                            }
                        } else {
                            val cursor = db.rawQuery("SELECT idUsuario, nombre FROM usuario WHERE correo = ?", arrayOf(email.trim()))
                            if (cursor.moveToFirst()) {
                                val userId = cursor.getLong(0)
                                val dbName = cursor.getString(1)
                                cursor.close()

                                val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
                                prefs.edit().apply {
                                    putLong("user_id", userId)
                                    putString("user_email", email.trim())
                                    putString("user_name", dbName)
                                    putBoolean("es_invitado", false)
                                    apply()
                                }

                                startActivity(Intent(this, InicioActivity::class.java))
                                finish()
                            } else {
                                cursor.close()
                                errorMessage = "El correo no está registrado. Regístrate primero."
                            }
                        }
                    },
                    onGuestLogin = {
                        val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putLong("user_id", -1L)
                            putString("user_email", "invitado@garabato.com")
                            putString("user_name", "Invitado")
                            putBoolean("es_invitado", true)
                            apply()
                        }

                        startActivity(Intent(this, InicioActivity::class.java))
                        finish()
                    },
                    onShowPrivacy = { showPrivacyDialog = true }
                )

                if (showPrivacyDialog) {
                    PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    errorMessage: String?,
    onLoginAttempt: (String, String, String, Boolean) -> Unit,
    onGuestLogin: () -> Unit,
    onShowPrivacy: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    // Fondo con degradado suave estilo arcoíris pastel / luminoso
    val rainbowGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.background
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(rainbowGradient),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logotipo de Garabato arriba del título
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo Garabato",
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.login_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (isRegistering) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.hint_name)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.hint_email)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.hint_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onLoginAttempt(email, password, name, isRegistering) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (isRegistering) stringResource(R.string.btn_register) else stringResource(R.string.btn_login)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onGuestLogin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(stringResource(R.string.btn_guest))
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { isRegistering = !isRegistering }) {
                    Text(
                        text = if (isRegistering) stringResource(R.string.toggle_to_login) else stringResource(R.string.toggle_to_register),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onShowPrivacy) {
                    Text("Aviso de Privacidad", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
