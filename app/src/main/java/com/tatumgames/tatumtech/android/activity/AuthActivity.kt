package com.tatumgames.tatumtech.android.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.tatumgames.tatumtech.android.ui.components.navigation.graph.MainGraph



class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // This installs the splash screen and shows it until the first draw
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            MainGraph(
                navController = navController
            )



            // Fix: Host the AccountSetupGraph instead of directly calling AuthScreen
//            AccountSetupGraph(
//                navController = navController,
//                onSignInSuccess = {
//                    // This lambda is correctly defined. When a sign-in within the
//                    // AccountSetupGraph is successful, this callback is triggered,
//                    // and you transition to MainActivity.
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
//
//                },
//                onSignInClick = {
//                    navController.navigate(NavRoutes.SIGNIN_SCREEN)
//
//                },
//                onSignUpClick = {
//                    navController.navigate(NavRoutes.SIGNUP_SCREEN)
//
//                }
//            )
        }
    }
}