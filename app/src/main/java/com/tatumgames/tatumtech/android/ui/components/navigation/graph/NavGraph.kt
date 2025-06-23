package com.tatumgames.tatumtech.android.ui.components.navigation.graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.AuthScreen
import com.tatumgames.tatumtech.android.ui.components.screens.MainScreen
import com.tatumgames.tatumtech.android.ui.components.screens.SignInScreen


@Composable
fun AccountSetupGraph(
    navController: NavHostController,
    onSignInSuccess: () -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.AUTH_SCREEN
    ) {
        composable(NavRoutes.AUTH_SCREEN) {
            AuthScreen(
                navController = navController,
                onSignInSuccess = onSignInSuccess,
                // Pass navigation lambdas for sign-in and sign-up buttons
                onSignInClick = onSignInClick,
                onSignUpClick = onSignUpClick
            )
        }
        composable(NavRoutes.SIGNIN_SCREEN) {
            SignInScreen(navController, onSignInClick)

        }
        composable(NavRoutes.SIGNUP_SCREEN) {

        }
        composable(NavRoutes.FORGOT_PASSWORD_SCREEN) {
            // Assume ForgotPasswordScreen needs an onBackClick for navigation

        }
    }
}

@Composable
fun MainGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.MAIN_SCREEN
    ) {
        composable(NavRoutes.MAIN_SCREEN) {
            MainScreen(navController)
        }
    }
}