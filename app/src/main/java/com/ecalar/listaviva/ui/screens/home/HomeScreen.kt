package com.ecalar.listaviva.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPantry: () -> Unit,
    onNavigateToShoppingList: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

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
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        onNavigateToPantry()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("Listas") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        onNavigateToShoppingList()
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> Text("Despensa - Usa el botón para navegar", modifier = Modifier.fillMaxSize())
                1 -> Text("Listas - Usa el botón para navegar", modifier = Modifier.fillMaxSize())
            }
        }
    }
}
