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
package com.tatumgames.tatumtech.android.ui.components.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.common.TitleText

/**
 * About Screen composable that displays either "About Tatum Games" or "FAQ" content.
 * 
 * @param navController Navigation controller for screen navigation.
 * @param contentType The type of content to display.
 */
@Composable
fun AboutScreen(
    navController: NavController,
    contentType: AboutContentType
) {
    val title = when (contentType) {
        AboutContentType.ABOUT -> stringResource(R.string.about_tatum_games)
        AboutContentType.FAQ -> stringResource(R.string.faq)
    }

    Scaffold(
        topBar = {
            Header(
                text = title,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        when (contentType) {
            AboutContentType.ABOUT -> {
                // About Tatum Games Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Tatum Games Description
                    item {
                        StandardText(
                            text = stringResource(R.string.about_tatum_games_description),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    // Our Mission
                    item {
                        Column {
                            TitleText(
                                text = stringResource(R.string.about_mission_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StandardText(
                                text = stringResource(R.string.about_mission_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Our Impact
                    item {
                        Column {
                            TitleText(
                                text = stringResource(R.string.about_impact_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StandardText(
                                text = stringResource(R.string.about_impact_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // What is Tatum Tech?
                    item {
                        Column {
                            TitleText(
                                text = stringResource(R.string.about_tatum_tech_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StandardText(
                                text = stringResource(R.string.about_tatum_tech_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Powered by MIKROS
                    item {
                        Column {
                            TitleText(
                                text = stringResource(R.string.about_mikros_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StandardText(
                                text = stringResource(R.string.about_mikros_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Join the MIKROS Mafia
                    item {
                        Column {
                            TitleText(
                                text = stringResource(R.string.about_community_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StandardText(
                                text = stringResource(R.string.about_community_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            AboutContentType.FAQ -> {
                // FAQ Content
                val faqs = listOf(
                    R.string.faq_what_is_tatum_tech to R.string.faq_what_is_tatum_tech_answer,
                    R.string.faq_who_can_attend to R.string.faq_who_can_attend_answer,
                    R.string.faq_how_to_register to R.string.faq_how_to_register_answer
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        TitleText(
                            text = stringResource(R.string.faq_general_questions)
                        )
                    }
                    items(faqs) { (questionRes, answerRes) ->
                        Column {
                            StandardText(
                                text = stringResource(id = questionRes),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StandardText(
                                text = stringResource(id = answerRes),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
