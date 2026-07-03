package com.ecalar.listaviva.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ecalar.listaviva.ui.screens.auth.AuthScreen
import com.ecalar.listaviva.ui.screens.family.create.CreateFamilyScreen
import com.ecalar.listaviva.ui.screens.family.join.JoinFamilyScreen
import com.ecalar.listaviva.ui.screens.home.HomeScreen
import com.ecalar.listaviva.ui.screens.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val AUTH = "auth"
    const val CREATE_FAMILY = "create_family"
    const val JOIN_FAMILY = "join_family"
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
            HomeScreen()
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
                }
            )
        }
    }
}
