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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jdl.practica1.ui.theme.AppTheme

class InicioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                InicioScreen(
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
    onOpenLienzo: () -> Unit,
    onOpenRetos: () -> Unit,
    onOpenAmigos: () -> Unit,
    onOpenPerfil: () -> Unit
) {
    var selectedChip by remember { mutableStateOf(0) }
    val chips = listOf(
        stringResource(R.string.chip_for_you),
        stringResource(R.string.chip_friends),
        stringResource(R.string.chip_challenges),
        stringResource(R.string.chip_trends)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_garabato)) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.cd_search)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.cd_notifications)
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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_challenges)) },
                    selected = false,
                    onClick = onOpenRetos
                )
                NavigationBarItem(
                    icon = {
                        FloatingActionButton(
                            onClick = onOpenLienzo,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.nav_add)
                            )
                        }
                    },
                    label = {},
                    selected = false,
                    onClick = onOpenLienzo
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_friends)) },
                    selected = false,
                    onClick = onOpenAmigos
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_profile)) },
                    selected = false,
                    onClick = onOpenPerfil
                )
            }
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
                val initials = listOf("A", "B", "C", "D", "E", "F")
                initials.forEach { initial ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
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
                        label = { Text(chipText) }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "A",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.post_author),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val outlineColor = MaterialTheme.colorScheme.outline
                        val tertiaryColor = MaterialTheme.colorScheme.tertiary

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f

                            val leftEar = Path().apply {
                                moveTo(cx - 60f, cy - 30f)
                                lineTo(cx - 80f, cy - 90f)
                                lineTo(cx - 20f, cy - 60f)
                                close()
                            }
                            val rightEar = Path().apply {
                                moveTo(cx + 60f, cy - 30f)
                                lineTo(cx + 80f, cy - 90f)
                                lineTo(cx + 20f, cy - 60f)
                                close()
                            }
                            drawPath(leftEar, primaryColor)
                            drawPath(rightEar, primaryColor)

                            drawCircle(
                                color = primaryColor,
                                radius = 70f,
                                center = Offset(cx, cy)
                            )

                            drawCircle(color = outlineColor, radius = 8f, center = Offset(cx - 25f, cy - 10f))
                            drawCircle(color = outlineColor, radius = 8f, center = Offset(cx + 25f, cy - 10f))

                            drawCircle(color = tertiaryColor, radius = 6f, center = Offset(cx, cy + 10f))
                            drawLine(outlineColor, Offset(cx - 10f, cy + 15f), Offset(cx - 50f, cy + 10f), 3f)
                            drawLine(outlineColor, Offset(cx - 10f, cy + 20f), Offset(cx - 50f, cy + 30f), 3f)
                            drawLine(outlineColor, Offset(cx + 10f, cy + 15f), Offset(cx + 50f, cy + 10f), 3f)
                            drawLine(outlineColor, Offset(cx + 10f, cy + 20f), Offset(cx + 50f, cy + 30f), 3f)
                        }
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
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = stringResource(R.string.cd_like),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = stringResource(R.string.post_likes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Create,
                                    contentDescription = stringResource(R.string.cd_comment),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.post_comments),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onOpenLienzo,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(R.string.btn_redraw))
                        }
                    }
                }
            }
        }
    }
}
