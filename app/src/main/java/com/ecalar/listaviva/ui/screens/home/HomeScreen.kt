package com.ecalar.listaviva.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPantry: () -> Unit,
    onNavigateToShoppingList: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listaviva") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Kitchen, contentDescription = null) },
                    label = { Text("Despensa") },
                    selected = true,
                    onClick = onNavigateToPantry
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("Listas") },
                    selected = false,
                    onClick = onNavigateToShoppingList
                )
            }
        }
    ) { padding ->
        PantryScreen(
            modifier = Modifier.padding(padding),
            onNavigateToAdd = { /* Navegar a añadir producto */ }
        )
    }
}

@Composable
fun PantryScreen(
    modifier: Modifier = Modifier,
    onNavigateToAdd: () -> Unit
) {
    // Placeholder - se reemplazará con la PantryScreen real
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Pantalla de Despensa")
    }
}
