package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.ui.components.common.Header
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text

@Composable
fun CodingChallengesScreen(navController: NavController) {
    Scaffold(
        topBar = {
            Header(text = "Coding Challenges", onBackClick = { navController.popBackStack() })
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Coding Challenges content coming soon!")
        }
    }
} 