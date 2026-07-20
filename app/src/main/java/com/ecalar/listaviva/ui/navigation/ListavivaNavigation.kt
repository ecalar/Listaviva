package com.ecalar.listaviva.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ecalar.listaviva.R
import com.ecalar.listaviva.ui.add_producto.AddProductoScreen
import com.ecalar.listaviva.ui.auth.AuthState
import com.ecalar.listaviva.ui.auth.AuthViewModel
import com.ecalar.listaviva.ui.crear_unirse.CrearUnirseScreen
import com.ecalar.listaviva.ui.crear_unirse.UnirseSettingsScreen
import com.ecalar.listaviva.ui.edit_producto.EditProductoScreen
import com.ecalar.listaviva.ui.estadisticas.EstadisticasScreen
import com.ecalar.listaviva.ui.home.BottomNavItem
import com.ecalar.listaviva.ui.home.HomeScreen
import com.ecalar.listaviva.ui.onboarding.OnboardingScreen
import com.ecalar.listaviva.ui.scanner.ScannerScreen

@Composable
fun ListavivaNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Splash.route
    ) {
        // --- RUTA: SPLASH / AUTH ---
        composable(Route.Splash.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()

            LaunchedEffect(authState) {
                if (authState is AuthState.Authenticated) {
                    val state = authState as AuthState.Authenticated

                    val destination = when {
                        state.isFirstTime -> "onboarding"
                        state.hasFamilia -> Route.Home.route
                        else -> Route.CrearUnirse.route
                    }

                    navController.navigate(destination) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                when (authState) {
                    is AuthState.Loading -> {
                        // AQUÍ MOSTRAMOS EL LOGO MIENTRAS CARGA
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo ListaViva",
                                modifier = Modifier.size(150.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            CircularProgressIndicator(color = Color(0xFF2E8B57))
                        }
                    }
                    is AuthState.Error -> Text(text = "Error: ${(authState as AuthState.Error).message}")
                    else -> {
                        // Mostramos el logo por defecto por si hay un micro-retraso antes de navegar
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo ListaViva",
                            modifier = Modifier.size(150.dp)
                        )
                    }
                }
            }
        }

        // --- RUTA: ONBOARDING ---
        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Route.CrearUnirse.route) {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // --- RUTA: CREAR O UNIRSE A FAMILIA ---
        composable(Route.CrearUnirse.route) {
            CrearUnirseScreen(
                onNavigateToHome = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.CrearUnirse.route) { inclusive = true }
                    }
                }
            )
        }

        // --- RUTA: HOME (Despensa y Listas) ---
        composable(Route.Home.route) {
            HomeScreen(
                onNavigateToAddProduct = {
                    navController.navigate("add_producto")
                },
                onNavigateToEditProduct = { productId ->
                    // Navegamos a la ruta de edición pasando el ID
                    navController.navigate("edit_producto/$productId")
                },
                onLogout = {
                    navController.navigate(Route.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToCrearUnirse = {
                    navController.navigate("unirse_ajustes")
                }
            )
        }

        // --- RUTA: ESTADÍSTICAS ---
        composable(BottomNavItem.Estadisticas.route) {
            EstadisticasScreen()
        }

        // --- RUTA: AÑADIR PRODUCTO ---
        composable("add_producto") {
            AddProductoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("scanner") {
            ScannerScreen(
                onNavigateBack = { navController.popBackStack() },
                onBarcodeScanned = { codigoLeido ->
                    // Cuando la cámara lee el código, volvemos a la despensa y le pasamos el código
                    navController.previousBackStackEntry?.savedStateHandle?.set("codigo_escaneado", codigoLeido)
                    navController.popBackStack()
                }
            )
        }

        // --- RUTA: EDITAR PRODUCTO ---
        composable(
            route = "edit_producto/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""

            EditProductoScreen(
                productoId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- RUTA: UNIRSE DESDE AJUSTES ---
        composable("unirse_ajustes") {
            UnirseSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}