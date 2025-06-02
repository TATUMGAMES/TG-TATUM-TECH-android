package com.tatumgames.tatumtech.android.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.tatumgames.tatumtech.android.ui.components.screens.AuthScreen

class AuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // This installs the splash screen and shows it until the first draw
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            AuthScreen(
                navController = navController,
                onSignInSuccess = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onSignInClick = {
                    // Handle sign-in click
                },
                onSignUpClick = {
                    // Handle sign-up click
                }
            )
        }


//        setContent {
//            val navController = rememberNavController()
//            AuthScreen(
//                navController = navController,
//                onSignInSuccess = {
//                    // Navigate to MainActivity on successful sign-in
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
//                }
//            )
//        }
    }
}
