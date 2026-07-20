package com.ecalar.listaviva.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ecalar.listaviva.ui.despensa.DespensaScreen
import com.ecalar.listaviva.ui.listas.ListasScreen
import com.ecalar.listaviva.ui.ajustes.AjustesScreen
import com.ecalar.listaviva.ui.estadisticas.EstadisticasScreen

@Composable
fun HomeScreen(
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToCrearUnirse: () -> Unit,
    onNavigateToScanner: () -> Unit
) {
    val navController = rememberNavController()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = onSurfaceColor, thickness = 3.dp)
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val items = listOf(
                        BottomNavItem.Despensa,
                        BottomNavItem.Listas,
                        BottomNavItem.Estadisticas,
                        BottomNavItem.Ajustes
                    )

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title, fontWeight = FontWeight.Bold) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = onSurfaceColor,
                                selectedTextColor = onSurfaceColor,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = onSurfaceColor.copy(alpha = 0.6f),
                                unselectedTextColor = onSurfaceColor.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Despensa.route
            ) {

                composable(BottomNavItem.Despensa.route) {
                    DespensaScreen(
                        navController = navController,
                        onNavigateToAddProduct = onNavigateToAddProduct,
                        onNavigateToEditProduct = onNavigateToEditProduct,
                        onNavigateToScanner = onNavigateToScanner
                    )
                }

                composable(BottomNavItem.Listas.route) {
                    ListasScreen()
                }

                composable(BottomNavItem.Ajustes.route) {
                    AjustesScreen(
                        onNavigateToLogin = onLogout,
                        onNavigateToCrearUnirse = onNavigateToCrearUnirse
                    )
                }
                composable(BottomNavItem.Estadisticas.route) {
                    EstadisticasScreen()
                }
            }
        }
    }
}