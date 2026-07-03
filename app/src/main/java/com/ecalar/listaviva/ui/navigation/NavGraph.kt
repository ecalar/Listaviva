package com.ecalar.listaviva.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ecalar.listaviva.ui.screens.auth.AuthScreen
import com.ecalar.listaviva.ui.screens.family.create.CreateFamilyScreen
import com.ecalar.listaviva.ui.screens.family.join.JoinFamilyScreen
import com.ecalar.listaviva.ui.screens.home.HomeScreen
import com.ecalar.listaviva.ui.screens.pantry.PantryScreen
import com.ecalar.listaviva.ui.screens.pantry.add.AddProductScreen
import com.ecalar.listaviva.ui.screens.scanner.QRScannerScreen
import com.ecalar.listaviva.ui.screens.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val AUTH = "auth"
    const val CREATE_FAMILY = "create_family"
    const val JOIN_FAMILY = "join_family"
    const val QR_SCANNER = "qr_scanner"
    const val PANTRY = "pantry"
    const val ADD_PRODUCT = "add_product"
    const val SHOPPING_LISTS = "shopping_lists"
    const val SETTINGS = "settings"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToAuth = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToPantry = {
                    navController.navigate(Routes.PANTRY)
                },
                onNavigateToShoppingList = {
                    navController.navigate(Routes.SHOPPING_LISTS)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.AUTH) {
            AuthScreen(
                onCreateFamily = {
                    navController.navigate(Routes.CREATE_FAMILY)
                },
                onJoinFamily = {
                    navController.navigate(Routes.JOIN_FAMILY)
                }
            )
        }

        composable(Routes.CREATE_FAMILY) {
            CreateFamilyScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.JOIN_FAMILY) {
            JoinFamilyScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToScanner = {
                    navController.navigate(Routes.QR_SCANNER)
                }
            )
        }

        composable(Routes.QR_SCANNER) {
            QRScannerScreen(
                onQrScanned = { code ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_code", code)
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.PANTRY) {
            PantryScreen(
                onNavigateToAdd = {
                    navController.navigate(Routes.ADD_PRODUCT)
                }
            )
        }

        composable(Routes.ADD_PRODUCT) {
            // Usamos un ViewModel compartido. Por ahora, placeholder.
            AddProductScreen(
                onNavigateBack = { navController.popBackStack() },
                onProductAdded = { name, category, subcategory, format, notes ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("new_product", listOf(name, category, subcategory, format, notes))
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SHOPPING_LISTS) {
            // Placeholder para Hito 5
            ShoppingListsPlaceholder()
        }

        composable(Routes.SETTINGS) {
            // Placeholder para Hito 6
            SettingsPlaceholder()
        }
    }
}

@Composable
fun ShoppingListsPlaceholder() {
    // TODO: Implementar en Hito 5
    androidx.compose.material3.Text("Listas de la compra - Próximamente")
}

@Composable
fun SettingsPlaceholder() {
    // TODO: Implementar en Hito 6
    androidx.compose.material3.Text("Ajustes - Próximamente")
}
