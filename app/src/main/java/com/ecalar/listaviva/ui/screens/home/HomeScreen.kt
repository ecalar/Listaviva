package com.ecalar.listaviva.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.ecalar.listaviva.ui.navigation.Routes
import com.ecalar.listaviva.ui.screens.pantry.PantryScreen
import com.ecalar.listaviva.ui.screens.shopping.ShoppingListsScreen
import com.ecalar.listaviva.ui.screens.settings.SettingsScreen
import com.ecalar.listaviva.ui.screens.stats.StatsScreen

enum class BottomTab(val icon: @Composable () -> Unit, val label: String) {
    PANTRY({ Icon(Icons.Outlined.Kitchen, contentDescription = null) }, "Despensa"),
    LISTS({ Icon(Icons.Default.ShoppingCart, contentDescription = null) }, "Listas"),
    STATS({ Icon(Icons.Outlined.BarChart, contentDescription = null) }, "Estadísticas"),
    SETTINGS({ Icon(Icons.Default.Settings, contentDescription = null) }, "Ajustes")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(BottomTab.PANTRY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listaviva") },
                actions = {
                    IconButton(onClick = { selectedTab = BottomTab.SETTINGS }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = tab.icon,
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                BottomTab.PANTRY -> PantryScreen(
                    onNavigateToAdd = { navController.navigate(Routes.ADD_PRODUCT) },
                    showTopBar = false
                )
                BottomTab.LISTS -> ShoppingListsScreen(
                    onNavigateToAddItem = { navController.navigate(Routes.ADD_TO_LIST) },
                    showTopBar = false
                )
                BottomTab.STATS -> StatsScreen()
                BottomTab.SETTINGS -> SettingsScreen(
                    onNavigateToAuth = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    showTopBar = false
                )
            }
        }
    }
}
