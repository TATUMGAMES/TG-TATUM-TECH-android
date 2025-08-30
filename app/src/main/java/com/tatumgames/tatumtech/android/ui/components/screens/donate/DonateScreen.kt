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
package com.tatumgames.tatumtech.android.ui.components.screens.donate

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.TimelineEntity
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.enums.DonationTierType
import com.tatumgames.tatumtech.android.enums.TimelineType
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonateScreen(navController: NavController) {
    var selectedUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val db = remember { AppDatabase.getInstance(context) }
    val timelineRepository = remember { TimelineDatabaseRepository(db.timelineDao()) }
    val coroutineScope = rememberCoroutineScope()

    val onBack: () -> Unit = {
        if (selectedUrl != null) {
            selectedUrl = null
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.donate_title),
                onBackClick = onBack
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedUrl == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StandardText(
                        text = stringResource(R.string.donation_mission_text),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    StandardText(
                        text = stringResource(R.string.choose_donation_tier),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DonationTierType.entries.forEach { tier ->
                        RoundedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            text = stringResource(
                                R.string.donation_tier_format,
                                stringResource(tier.nameRes),
                                tier.amount
                            ),
                            onClick = {
                                selectedUrl = tier.url
                                coroutineScope.launch {
                                    timelineRepository.insertTimelineEvent(
                                        TimelineEntity(
                                            type = TimelineType.DONATION.typeValue,
                                            description = context.getString(
                                                R.string.visited_donation_page,
                                                context.getString(tier.nameRes),
                                                tier.amount
                                            ),
//                                            description = "Donated: ${context.getString(tier.nameRes)} – ${tier.amount}",
                                            relatedId = null,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                        )
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
