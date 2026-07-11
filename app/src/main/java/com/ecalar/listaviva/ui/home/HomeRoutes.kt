package com.ecalar.listaviva.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Despensa : BottomNavItem("tab_despensa", "Despensa", Icons.Default.Home)
    object Listas : BottomNavItem("tab_listas", "Listas", Icons.Default.ShoppingCart)
    object Estadisticas : BottomNavItem("tab_estadisticas", "Estadísticas", Icons.Default.Assessment)
    object Ajustes : BottomNavItem("tab_ajustes", "Ajustes", Icons.Default.Settings)
}