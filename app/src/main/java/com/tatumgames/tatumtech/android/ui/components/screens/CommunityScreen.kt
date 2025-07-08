package com.tatumgames.tatumtech.android.ui.components.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header

@Composable
fun CommunityScreen(
    navController: NavController
) {
    var selectedUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Header(text = "Donate", onBackClick = { navController.popBackStack() })
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (selectedUrl == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Our mission is to increase representation of minorities and women in the video game industry. With only 2% of game devs being Black and 3% Latinx, your donation helps us rewrite the future—ensuring diverse voices are empowered to create, lead, and innovate.",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Choose a donation tier:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEEEEE))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { selectedUrl = null }, shape = RoundedCornerShape(8.dp)) {
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Secure Stripe Payment", fontWeight = FontWeight.Bold)
                    }
                    AndroidView(
                        factory = {
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                loadUrl(selectedUrl!!)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
} 
