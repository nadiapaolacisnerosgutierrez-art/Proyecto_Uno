package com.jdl.practica1

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

                LoginScreen(
                    onLoginSuccess = { email, password, name, isReg ->
                        val db = dbHelper.writableDatabase
                        var userId = 1L

                        if (isReg) {
                            val values = ContentValues().apply {
                                put("nombre", if (name.isBlank()) "Usuario" else name)
                                put("correo", email)
                            }
                            userId = db.insert("usuario", null, values)
                        } else {
                            // Buscar usuario por correo o crear por defecto
                            val cursor = db.rawQuery("SELECT idUsuario FROM usuario WHERE correo = ?", arrayOf(email))
                            if (cursor.moveToFirst()) {
                                userId = cursor.getLong(0)
                            } else {
                                val values = ContentValues().apply {
                                    put("nombre", email.substringBefore("@"))
                                    put("correo", email)
                                }
                                userId = db.insert("usuario", null, values)
                            }
                            cursor.close()
                        }

                        // Guardar sesión activa en SharedPreferences
                        val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putLong("user_id", userId)
                            putString("user_email", email)
                            putString("user_name", if (isReg && name.isNotBlank()) name else email.substringBefore("@"))
                            apply()
                        }

                        startActivity(Intent(this, InicioActivity::class.java))
                        finish()
                    },
                    onGuestLogin = {
                        val prefs = getSharedPreferences("garabato_user_prefs", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putLong("user_id", 0L)
                            putString("user_email", "invitado@garabato.com")
                            putString("user_name", "Invitado")
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
    onLoginSuccess: (String, String, String, Boolean) -> Unit,
    onGuestLogin: () -> Unit,
    onShowPrivacy: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            Spacer(modifier = Modifier.height(32.dp))

            if (isRegistering) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.hint_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.hint_email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.hint_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onLoginSuccess(email, password, name, isRegistering) },
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
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.btn_guest))
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { isRegistering = !isRegistering }) {
                Text(
                    text = if (isRegistering) stringResource(R.string.toggle_to_login) else stringResource(R.string.toggle_to_register),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onShowPrivacy) {
                Text("Aviso de Privacidad", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
