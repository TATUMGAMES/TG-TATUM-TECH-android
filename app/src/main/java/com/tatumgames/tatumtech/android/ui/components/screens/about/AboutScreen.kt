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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.constants.Constants
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.ClickableText
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.common.TitleText
import com.tatumgames.tatumtech.android.ui.theme.Grey200
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import com.tatumgames.tatumtech.android.ui.theme.SpringPurple100
import com.tatumgames.tatumtech.android.utils.Utils.openUrl

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
    val context = LocalContext.current
    val title = when (contentType) {
        AboutContentType.ABOUT -> stringResource(R.string.about_tatum_games)
        AboutContentType.FAQ -> stringResource(R.string.faq)
    }

    val mikrosResources = listOf(
        AboutResourceLink(
            titleRes = R.string.about_mikros_resource_explainer,
            descriptionRes = R.string.about_mikros_resource_explainer_desc,
            url = Constants.URL_MIKROS_EXPLAINER_VIDEO
        ),
        AboutResourceLink(
            titleRes = R.string.about_mikros_resource_marketing_tutorial,
            descriptionRes = R.string.about_mikros_resource_marketing_tutorial_desc,
            url = Constants.URL_MIKROS_MARKETING_TUTORIAL
        ),
        AboutResourceLink(
            titleRes = R.string.about_mikros_resource_analytics_integration,
            descriptionRes = R.string.about_mikros_resource_analytics_integration_desc,
            url = Constants.URL_MIKROS_ANALYTICS_INTEGRATION
        ),
        AboutResourceLink(
            titleRes = R.string.about_mikros_resource_analytics_logging,
            descriptionRes = R.string.about_mikros_resource_analytics_logging_desc,
            url = Constants.URL_MIKROS_ANALYTICS_LOGGING
        ),
        AboutResourceLink(
            titleRes = R.string.about_mikros_resource_docs,
            descriptionRes = R.string.about_mikros_resource_docs_desc,
            url = Constants.URL_MIKROS_TECHNICAL_DOCS
        )
    )

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
        containerColor = ScreenScaffoldLight
    ) { paddingValues ->
        when (contentType) {
            AboutContentType.ABOUT -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        ClickableText(
                            text = stringResource(R.string.about_tatum_games_description),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    item {
                        Column {
                            TitleText(
                                Modifier.background(SpringPurple100),
                                text = stringResource(R.string.about_mission_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StandardText(
                                text = stringResource(R.string.about_mission_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    item {
                        Column {
                            TitleText(
                                Modifier.background(SpringPurple100),
                                text = stringResource(R.string.about_impact_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StandardText(
                                text = stringResource(R.string.about_impact_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    item {
                        Column {
                            TitleText(
                                Modifier.background(SpringPurple100),
                                text = stringResource(R.string.about_tatum_tech_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StandardText(
                                text = stringResource(R.string.about_tatum_tech_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            AboutResourceRow(
                                title = stringResource(R.string.about_visit_tatum_tech),
                                description = null,
                                onClick = { openUrl(context, Constants.URL_TATUM_TECH) }
                            )
                        }
                    }

//                    item {
//                        TitleText(text = stringResource(R.string.about_our_technology_title))
//                    }

                    item {
                        Column {
                            TitleText(
                                Modifier.background(SpringPurple100),
                                text = stringResource(R.string.about_orchestra_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StandardText(
                                text = stringResource(R.string.about_orchestra_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    item {
                        Column {
                            TitleText(
                                Modifier.background(SpringPurple100),
                                text = stringResource(R.string.about_mikros_analytics_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StandardText(
                                text = stringResource(R.string.about_mikros_analytics_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    item {
                        Column {
                            TitleText(
                                Modifier.background(SpringPurple100),
                                text = stringResource(R.string.about_mikros_marketing_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StandardText(
                                text = stringResource(R.string.about_mikros_marketing_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TitleText(
                                Modifier.background(SpringPurple100),
                                text = stringResource(R.string.about_mikros_resources_title)
                            )
                            mikrosResources.forEach { resource ->
                                AboutResourceRow(
                                    title = stringResource(resource.titleRes),
                                    description = resource.descriptionRes?.let {
                                        stringResource(it)
                                    },
                                    onClick = { openUrl(context, resource.url) }
                                )
                            }
                        }
                    }

                    item {
                        Column {
                            TitleText(
                                Modifier.background(SpringPurple100),
                                text = stringResource(R.string.about_community_title)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ClickableText(
                                text = stringResource(R.string.about_community_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            AboutContentType.FAQ -> {
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
                        TitleText(text = stringResource(R.string.faq_general_questions))
                    }
                    items(faqs) { (questionRes, answerRes) ->
                        Column {
                            StandardText(
                                text = stringResource(id = questionRes),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
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

@Composable
private fun AboutResourceRow(
    title: String,
    description: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Grey200)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            StandardText(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                StandardText(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Purple500
        )
    }
}
