package com.ecalar.listaviva.ui.navigation

sealed class Route(val route: String) {
    object Splash : Route("splash")
    object CrearUnirse : Route("crear_unirse")
    object Home : Route("home") // El Home contendrá el BottomNavigationBar
}