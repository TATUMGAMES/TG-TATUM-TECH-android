package com.tatumgames.tatumtech.android.ui.components.navigation.graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.AuthScreen
import com.tatumgames.tatumtech.android.ui.components.screens.ForgotPasswordScreen
import com.tatumgames.tatumtech.android.ui.components.screens.MainScreen
import com.tatumgames.tatumtech.android.ui.components.screens.SigninScreen
import com.tatumgames.tatumtech.android.ui.components.screens.SignupScreen

@Composable
fun AccountSetupGraph(
    navController: NavHostController,
    onSignInSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.AUTH_SCREEN
    ) {
        composable(NavRoutes.AUTH_SCREEN) {
            AuthScreen(navController, onSignInSuccess)
        }
        composable(NavRoutes.SIGNIN_SCREEN) {
            SigninScreen(navController, onSignInSuccess)
        }
        composable(NavRoutes.SIGNUP_SCREEN) {
            SignupScreen(navController)
        }
        composable(NavRoutes.FORGOT_PASSWORD_SCREEN) {
            ForgotPasswordScreen(navController)
        }
    }
}

@Composable
fun MainGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.MAIN_SCREEN
    ) {
        composable(NavRoutes.MAIN_SCREEN) {
            MainScreen(navController)
        }
    }
}
