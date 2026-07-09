package com.ecalar.listaviva.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ecalar.listaviva.ui.screens.auth.AuthScreen
import com.ecalar.listaviva.ui.screens.family.create.CreateFamilyScreen
import com.ecalar.listaviva.ui.screens.family.join.JoinFamilyScreen
import com.ecalar.listaviva.ui.screens.home.HomeScreen
import com.ecalar.listaviva.ui.screens.onboarding.OnboardingScreen
import com.ecalar.listaviva.ui.screens.pantry.PantryScreen
import com.ecalar.listaviva.ui.screens.pantry.PantryViewModel
import com.ecalar.listaviva.ui.screens.pantry.add.AddProductScreen
import com.ecalar.listaviva.ui.screens.scanner.QRScannerScreen
import com.ecalar.listaviva.ui.screens.shopping.AddItemToListScreen
import com.ecalar.listaviva.ui.screens.shopping.ShoppingListsViewModel
import com.ecalar.listaviva.ui.screens.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val AUTH = "auth"
    const val CREATE_FAMILY = "create_family"
    const val JOIN_FAMILY = "join_family"
    const val QR_SCANNER = "qr_scanner"
    const val ADD_PRODUCT = "add_product"
    const val ADD_TO_LIST = "add_to_list"
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
                },
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(navController = navController)
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
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_PRODUCT) {
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

        composable(Routes.ADD_TO_LIST) {
            val pantryViewModel: PantryViewModel = hiltViewModel()
            val shoppingViewModel: ShoppingListsViewModel = hiltViewModel()
            val pantryItems by pantryViewModel.state.collectAsState()

            AddItemToListScreen(
                pantryItems = pantryItems.items,
                onAddFromPantry = { item ->
                    shoppingViewModel.addItemToList(name = item.name, pantryItemId = item.id, quantity = item.format)
                    navController.popBackStack()
                },
                onAddManual = { name ->
                    shoppingViewModel.addItemToList(name = name, pantryItemId = null)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
