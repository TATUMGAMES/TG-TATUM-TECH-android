/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tatumgames.tatumtech.android.ui.components.screens.community

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.constants.Constants.URL_DISCORD_LOGIN
import com.tatumgames.tatumtech.android.ui.components.common.Header

//@SuppressLint("SetJavaScriptEnabled")
//@Composable
//fun CommunityScreen(navController: NavController) {
//    val context = LocalContext.current
//
//    Scaffold(
//        topBar = {
//            Header(
//                text = stringResource(R.string.community),
//                onBackClick = { navController.popBackStack() }
//            )
//        },
//        containerColor = Color(0xFFF0F0F0)
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            // Placeholder since Discord won't load
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("Discord community must be opened in your browser.")
//                // Optional: Open externally
//                Button(onClick = {
//                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(URL_DISCORD_LOGIN))
//                    context.startActivity(intent)
//                }) {
//                    Text("Open Discord")
//                }
//            }
//
//            // Sponsor CTA Section
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Your support goes a long way",
//                    fontSize = 14.sp,
//                    color = Color.DarkGray,
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//
//                Button(
//                    onClick = {
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://payrole.io/app/store?id=65e729cff968f40012f47835&nojoin"))
//                        context.startActivity(intent)
//                    },
//                    shape = RoundedCornerShape(8.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFF7B61FF), // Purple
//                        contentColor = Color.White
//                    )
//                ) {
//                    Text(text = "Sponsor")
//                }
//            }
//        }
//    }
//}

@Composable
fun CommunityScreen(navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Header(
                text = "Join Our Community",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Replace with your actual image
            Image(
                painter = painterResource(R.drawable.logo_text),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Connect with other players, get updates, and join giveaways.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Try deep link first
                    val discordIntent = Intent(Intent.ACTION_VIEW, Uri.parse("discord://invite/6FzqSUDRXQ"))
                    if (discordIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(discordIntent)
                    } else {
                        // Fallback to browser
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/invite/6FzqSUDRXQ")))
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7289DA) // Discord brand color
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join Discord", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(
                color = Color.LightGray,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Support Our Mission",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your donations go directly toward giving kids free computers and training in tech and game development.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val sponsorIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://payrole.io/app/store?id=65e729cff968f40012f47835&nojoin"))
                    context.startActivity(sponsorIntent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B61FF)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Become a Sponsor", color = Color.White)
            }
        }
    }
}
