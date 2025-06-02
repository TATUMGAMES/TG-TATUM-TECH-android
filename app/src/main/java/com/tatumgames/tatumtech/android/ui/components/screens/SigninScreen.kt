package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes

@Composable
fun SigninScreen(
    navController: NavController,
    onSignInSuccess: () -> Unit
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
//        Text("Sign In Screen")
//        Button(onClick = { navController.navigate(NavRoutes.FORGOT_PASSWORD_SCREEN) }) {
//            Text("Forgot Password?")
//        }
//        Button(onClick = { onSignInSuccess() }) {
//            Text("Mock Sign In")
//        }
    }
}
