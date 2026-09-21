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
package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.games.GamesResourcesResolver
import com.tatumgames.tatumtech.android.ui.models.GamesResourceEntry
import com.tatumgames.tatumtech.android.ui.theme.Grey500
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.ui.utils.GameMediaResolver
import com.tatumgames.tatumtech.android.ui.utils.JsonImporter
import com.tatumgames.tatumtech.android.utils.Utils.openUrl

@Composable
fun GamesResourcesScreen(navController: NavController) {
    val context = LocalContext.current
    var grouped by remember {
        mutableStateOf<List<Pair<String, List<GamesResourceEntry>>>>(emptyList())
    }
    var loadFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val categories = JsonImporter.loadGamesResourceCategories(context)
        val partners = JsonImporter.loadPartners(context)
        val entries = GamesResourcesResolver.resolve(categories, partners)
        grouped = GamesResourcesResolver.groupByCategory(entries)
        loadFinished = true
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.title_games_resources),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = ScreenScaffoldLight
    ) { paddingValues ->
        when {
            !loadFinished -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    StandardText(
                        text = stringResource(R.string.loading_resources),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            grouped.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StandardText(
                        text = stringResource(R.string.games_resources_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    grouped.forEach { (category, entries) ->
                        item(key = "header_$category") {
                            StandardText(
                                text = category,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(
                            items = entries,
                            key = { "${it.category}_${it.partner.id}" }
                        ) { entry ->
                            GamesResourcePartnerCard(entry = entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GamesResourcePartnerCard(entry: GamesResourceEntry) {
    val context = LocalContext.current
    val partner = entry.partner
    val logoData = partner.logo?.let { GameMediaResolver.resolve(context, "drawable:$it") }
        ?: R.drawable.partners
    val website = partner.websiteUrl

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(logoData)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(
                        R.string.partners_logo_cd,
                        entry.displayName
                    ),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(R.drawable.partners),
                    error = painterResource(R.drawable.partners)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StandardText(
                    text = entry.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
            }
            partner.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                StandardText(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Grey500
                )
            }
            if (!website.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { openUrl(context, website) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    StandardText(
                        text = stringResource(R.string.partners_visit_website),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}
