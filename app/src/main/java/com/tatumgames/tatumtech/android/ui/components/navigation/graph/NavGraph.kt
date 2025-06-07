package com.tatumgames.tatumtech.android.ui.components.navigation.graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.AuthScreen
//import com.tatumgames.tatumtech.android.ui.components.screens.ForgotPasswordScreen
import com.tatumgames.tatumtech.android.ui.components.screens.MainScreen
import com.tatumgames.tatumtech.android.ui.components.screens.SignInScreen // Renamed from SigninScreen to match definition
import com.tatumgames.tatumtech.android.ui.components.screens.SignUpScreen   // Renamed from SignupScreen to match definition

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
            // Pass the required onBackClick and onSignInClick lambdas
            SignInScreen(
                onBackClick = { navController.popBackStack() }, // Navigates back
                onSignInClick = { email, password ->

                    // This is where you'd typically call a ViewModel function for authentication.
                    // For example: authViewModel.signIn(email, password)
                    // If authentication is successful:
                    onSignInSuccess()
                    // You might also want to pop the back stack to clear the sign-in screen
                    // navController.popBackStack(NavRoutes.AUTH_SCREEN) { inclusive = true }
                }
                // FIX: Uncomment this line to enable navigation to ForgotPasswordScreen
//                onForgotPasswordClick = { navController.navigate(NavRoutes.FORGOT_PASSWORD_SCREEN) }
            )
        }
        composable(NavRoutes.SIGNUP_SCREEN) {
            // Pass the required onBackClick, onSignUpClick, and onSignInLinkClick lambdas
            SignUpScreen(
                onBackClick = { navController.popBackStack() }, // Navigates back
                onSignUpClick = { email, password, confirmPassword ->
                    // TODO: Implement your actual sign-up logic here.
                    // For example: authViewModel.signUp(email, password, confirmPassword)
                    // After successful sign-up, you might navigate them to the sign-in screen
                    // or directly to the main app based on your flow.
                    // Example: navController.navigate(NavRoutes.SIGNIN_SCREEN) {
                    //     popUpTo(NavRoutes.AUTH_SCREEN) { inclusive = true } // Clear back stack to Auth screen
                    // }
                },
                onSignInLinkClick = { navController.navigate(NavRoutes.SIGNIN_SCREEN) } // Navigates to sign-in screen
            )
        }
        composable(NavRoutes.FORGOT_PASSWORD_SCREEN) {
            // Assume ForgotPasswordScreen needs an onBackClick for navigation
//            ForgotPasswordScreen(
//                onBackClick = { navController.popBackStack() } // Navigates back
//            )
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