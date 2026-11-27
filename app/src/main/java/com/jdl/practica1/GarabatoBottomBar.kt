package com.jdl.practica1

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

fun navigateToActivity(context: Context, clazz: Class<*>) {
    val intent = Intent(context, clazz).apply {
        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    }
    context.startActivity(intent)
}

@Composable
fun GarabatoBottomBar(context: Context, currentTab: Int) {
    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
            selected = currentTab == 0,
            onClick = { navigateToActivity(context, InicioActivity::class.java) },
            colors = navItemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_challenges)) },
            selected = currentTab == 1,
            onClick = { navigateToActivity(context, RetosActivity::class.java) },
            colors = navItemColors
        )
        NavigationBarItem(
            icon = {
                FloatingActionButton(
                    onClick = { navigateToActivity(context, LienzoActivity::class.java) },
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
            onClick = { navigateToActivity(context, LienzoActivity::class.java) },
            colors = navItemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_friends)) },
            selected = currentTab == 3,
            onClick = { navigateToActivity(context, AmigosActivity::class.java) },
            colors = navItemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_profile)) },
            selected = currentTab == 4,
            onClick = { navigateToActivity(context, PerfilActivity::class.java) },
            colors = navItemColors
        )
    }
}
