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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.constants.Constants.URL_DISCORD_INVITE
import com.tatumgames.tatumtech.android.constants.Constants.URL_DISCORD_PAYROLE
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.common.TitleText
import com.tatumgames.tatumtech.android.ui.theme.Purple200
import com.tatumgames.tatumtech.android.ui.components.screens.community.apiclient.DiscordApiClient
import com.tatumgames.tatumtech.android.ui.components.screens.community.apiclient.ApiResult
import com.tatumgames.tatumtech.android.ui.components.screens.community.apiclient.DiscordServerInfo

@Composable
fun CommunityScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val discordInviteUrl = URL_DISCORD_INVITE
    val sponsorshipUrl = URL_DISCORD_PAYROLE
    val fallbackInviteCode = "6FzqSUDRXQ"

    var serverInfo by remember { mutableStateOf<DiscordServerInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val apiClient = remember { DiscordApiClient() }

    // Fetch Discord server info using the new API client
    LaunchedEffect(Unit) {
        val result = apiClient.fetchDiscordServerInfo(fallbackInviteCode)
        when (result) {
            is ApiResult.Success -> {
                serverInfo = result.data
                isLoading = false
            }
            is ApiResult.Error -> {
                errorMessage = result.message
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.join_community_title),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    StandardText(text = "Error: $errorMessage")
                }
            } else {
                // Content
                serverInfo?.let { info ->
                    // Server Banner
                    // 🟠 Banner Section with fallback, border, and rounded corners
                    val bannerModifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(2.dp, Purple200, RoundedCornerShape(4.dp))

                    val hasValidBanner = !info.serverBanner.isNullOrBlank() && info.serverBanner != "null"
                    if (hasValidBanner && !info.guildId.isNullOrBlank()) {
                        val bannerUrl = "https://cdn.discordapp.com/banners/${info.guildId}/${info.serverBanner}.png"
                        Image(
                            painter = rememberAsyncImagePainter(bannerUrl),
                            contentDescription = stringResource(R.string.content_description_server_banner),
                            modifier = bannerModifier,
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.discord_banner),
                            contentDescription = stringResource(R.string.content_description_server_banner),
                            modifier = bannerModifier,
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Server Icon and Name Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Server Icon
                        if (!info.serverIcon.isNullOrBlank() && !info.guildId.isNullOrBlank()) {
                            val iconUrl = "https://cdn.discordapp.com/icons/${info.guildId}/${info.serverIcon}.png"
                            Image(
                                painter = rememberAsyncImagePainter(iconUrl),
                                contentDescription = stringResource(R.string.content_description_server_icon),
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        } else {
                            // Fallback icon
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                StandardText(
                                    text = info.serverName?.firstOrNull()?.toString() ?: "M",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Server Name and Invite Link
                        Column(modifier = Modifier.weight(1f)) {
                            StandardText(
                                text = info.serverName ?: stringResource(R.string.mikros_mafia),
                                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Start
                            )

                            // Invite Link with fallback
                            val inviteCode = if (info.vanityUrl.isNullOrBlank() || info.vanityUrl == "null") {
                                fallbackInviteCode
                            } else {
                                info.vanityUrl
                            }
                            val inviteLabel = stringResource(R.string.discord_invite)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StandardText(
                                    text = "discord.gg/$inviteCode",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF5865F2),
                                    modifier = Modifier.clickable {
                                        val clip = ClipData.newPlainText(
                                            inviteLabel,
                                            "https://discord.gg/$inviteCode"
                                        )
                                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                            .setPrimaryClip(clip)
                                    }
                                )
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = stringResource(R.string.content_description_share_invite),
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(20.dp)
                                        .clickable {
                                            val intent = Intent(Intent.ACTION_SEND)
                                            intent.type = "text/plain"
                                            intent.putExtra(
                                                Intent.EXTRA_TEXT,
                                                "https://discord.gg/$inviteCode"
                                            )
                                            context.startActivity(
                                                Intent.createChooser(
                                                    intent,
                                                    "Share Discord Invite"
                                                )
                                            )
                                        },
                                    tint = Color(0xFF5865F2)
                                )
                            }
                        }
                    }

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Face,
                                contentDescription = stringResource(R.string.content_description_members),
                                tint = Color(0xFF5865F2)
                            )
                            StandardText(
                                text = "${info.memberCount ?: "-"} Members",
                                textAlign = TextAlign.Center
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ThumbUp,
                                contentDescription = stringResource(R.string.content_description_online),
                                tint = Color(0xFF43B581)
                            )
                            StandardText(
                                text = "${info.onlineCount ?: "-"} Online",
                                textAlign = TextAlign.Center
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = stringResource(R.string.content_description_boost_level),
                                tint = Color(0xFFF47FFF)
                            )
                            StandardText(
                                text = "Boost Lv. ${info.boostLevel ?: "-"}",
                                textAlign = TextAlign.Center
                            )
                            if (info.boostCount != null) {
                                StandardText(
                                    text = "${info.boostCount} boosts",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Description
                    if (!info.serverDescription.isNullOrBlank()) {
                        StandardText(
                            text = info.serverDescription,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Online Avatars - Show placeholder avatars instead of question marks
                    if (info.onlineCount != null && info.onlineCount > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat((info.onlineCount).coerceAtMost(8)) { idx ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (idx % 4) {
                                                0 -> Color(0xFF5865F2)
                                                1 -> Color(0xFF43B581)
                                                2 -> Color(0xFFF47FFF)
                                                else -> Color(0xFFFAA61A)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    StandardText(
                                        text = "👤",
                                        textAlign = TextAlign.Center,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (info.onlineCount > 8) {
                                StandardText(
                                    text = "+${info.onlineCount - 8} more",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // CTA Buttons
                    RoundedButton(
                        text = stringResource(R.string.join_mikros_mafia),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, discordInviteUrl.toUri())
                            context.startActivity(intent)
                        }
                    )
                    RoundedButton(
                        text = stringResource(R.string.support_us),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, sponsorshipUrl.toUri())
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
