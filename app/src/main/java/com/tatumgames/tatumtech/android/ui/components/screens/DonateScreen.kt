package com.tatumgames.tatumtech.android.ui.components.screens

import android.annotation.SuppressLint
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

private val donationTiers = listOf(
    DonationTier("Founding Supporter", "Custom", "https://buy.stripe.com/7sI3cH8m6bmd5ck4gj"),
    DonationTier("Silver Sponsor", "$2,500", "https://buy.stripe.com/28oaF9cCmdul7kscMO"),
    DonationTier("Gold Sponsor", "$5,000", "https://buy.stripe.com/6oEbJd6dY0Hz8ow145"),
    DonationTier("Premier Sponsor", "$10,000", "https://buy.stripe.com/3cs28D45QgGxgV2fYY")
)

data class DonationTier(val name: String, val amount: String, val url: String)

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateScreen(navController: NavController) {
    var selectedUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Header(
                text = "Donate",
                onBackClick = { navController.popBackStack() }
            )
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
                    donationTiers.forEach { tier ->
                        Button(
                            onClick = { selectedUrl = tier.url },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("${tier.name} – ${tier.amount}")
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
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
