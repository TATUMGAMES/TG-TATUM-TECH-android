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

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.games.ScreenshotFullscreenViewer
import com.tatumgames.tatumtech.android.ui.models.GameModel
import com.tatumgames.tatumtech.android.ui.models.GameVideoLink
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.ui.utils.GameMediaResolver
import com.tatumgames.tatumtech.android.ui.viewmodels.GamesViewModel
import com.tatumgames.tatumtech.android.ui.viewmodels.factory.GamesViewModelFactory

@Composable
fun GameDetailsScreen(
    navController: NavController,
    gameId: String
) {
    val context = LocalContext.current
    val viewModel: GamesViewModel = viewModel(factory = GamesViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(gameId) {
        if (uiState.allGames.isEmpty()) {
            viewModel.loadGames()
        }
    }

    val game = viewModel.getGameById(gameId)

    when {
        uiState.isLoading && game == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        !uiState.isLoading && game == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                StandardText(
                    text = uiState.errorMessage ?: stringResource(R.string.games_not_found),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        else -> {
            val g = game!!
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Header(
                        text = stringResource(R.string.games_details_title),
                        onBackClick = { navController.popBackStack() }
                    )
                }
                item {
                    GameDetailsBody(game = g)
                }
            }
        }
    }
}

@Composable
private fun GameDetailsBody(game: GameModel) {
    val context = LocalContext.current
    val comingSoon = GameMediaResolver.isComingSoon(game)
    val heroRef = game.images.featureGraphics.firstOrNull()
        ?: game.campaign?.images?.appLogo
    val logoRef = game.campaign?.images?.appLogo

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        logoRef?.let { ref ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(GameMediaResolver.resolve(context, ref))
                    .crossfade(200)
                    .build(),
                contentDescription = stringResource(R.string.games_logo_cd, game.title),
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.games),
                error = painterResource(R.drawable.games)
            )
        }

        StandardText(
            text = game.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        StandardText(
            text = game.companyName,
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryChip(
                text = if (comingSoon) {
                    stringResource(R.string.games_status_coming_soon)
                } else {
                    stringResource(R.string.games_status_featured)
                },
                isHighlighted = true
            )
            CategoryChip(
                text = if (comingSoon) {
                    stringResource(R.string.games_platforms_ios_android)
                } else {
                    stringResource(R.string.games_status_available)
                }
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(GameMediaResolver.resolve(context, heroRef))
                    .crossfade(300)
                    .build(),
                contentDescription = stringResource(R.string.games_screenshot_cd, game.title),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.games),
                error = painterResource(R.drawable.games)
            )
        }

        StandardText(
            text = stringResource(R.string.games_about),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        StandardText(
            text = game.fullDesc,
            style = MaterialTheme.typography.bodyMedium
        )

        if (comingSoon) {
            StandardText(
                text = stringResource(R.string.games_coming_soon_notice),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        }

        VideoSection(game = game)
        ScreenshotSection(game = game)

        if (!comingSoon) {
            StandardText(
                text = stringResource(R.string.games_get_the_game),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            CtaButtons(game = game)
        }

        game.discoveryTags?.takeIf { it.isNotEmpty() }?.let { tags ->
            StandardText(
                text = stringResource(R.string.games_tags),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    SuggestionChip(
                        onClick = {},
                        enabled = false,
                        label = {
                            StandardText(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoSection(game: GameModel) {
    val context = LocalContext.current
    val videos = game.videos.promotional
    if (videos.isEmpty()) return

    StandardText(
        text = stringResource(R.string.games_videos),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        videos.forEach { video ->
            VideoCard(gameTitle = game.title, video = video) {
                openUrl(context, video.url)
            }
        }
    }
}

@Composable
private fun VideoCard(
    gameTitle: String,
    video: GameVideoLink,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(video.thumbnailUrl)
                    .crossfade(200)
                    .build(),
                contentDescription = stringResource(R.string.games_video_cd, gameTitle),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.games),
                error = painterResource(R.drawable.games)
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Black.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                StandardText(
                    text = "▶",
                    style = MaterialTheme.typography.headlineMedium.copy(color = White)
                )
            }
        }
    }
}

@Composable
private fun ScreenshotSection(game: GameModel) {
    val context = LocalContext.current
    val screenshots = game.images.screenshots
    if (screenshots.isEmpty()) return

    var viewerInitialPage by remember { mutableStateOf<Int?>(null) }

    StandardText(
        text = stringResource(R.string.games_screenshots),
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(screenshots) { index, ref ->
            Card(
                onClick = { viewerInitialPage = index },
                modifier = Modifier
                    .width(220.dp)
                    .height(124.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(GameMediaResolver.resolve(context, ref))
                        .crossfade(200)
                        .build(),
                    contentDescription = stringResource(R.string.games_screenshot_cd, game.title),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.games),
                    error = painterResource(R.drawable.games)
                )
            }
        }
    }

    viewerInitialPage?.let { page ->
        ScreenshotFullscreenViewer(
            screenshots = screenshots,
            initialPage = page,
            gameTitle = game.title,
            onDismiss = { viewerInitialPage = null }
        )
    }
}

@Composable
private fun CtaButtons(game: GameModel) {
    val context = LocalContext.current
    val c = game.campaign?.ctas
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        c?.googleStore?.takeIf { it.isNotBlank() }?.let { url ->
            StoreButton(
                label = stringResource(R.string.games_download_android),
                onClick = { openUrl(context, url) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
        c?.appleStore?.takeIf { it.isNotBlank() }?.let { url ->
            StoreButton(
                label = stringResource(R.string.games_download_ios),
                onClick = { openUrl(context, url) },
                containerColor = MaterialTheme.colorScheme.secondary
            )
        }
        c?.website?.takeIf { it.isNotBlank() }?.let { url ->
            StoreButton(
                label = stringResource(R.string.games_visit_website),
                onClick = { openUrl(context, url) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = Black
            )
        }
        game.website?.takeIf { it.isNotBlank() && c?.website == null }?.let { url ->
            StoreButton(
                label = stringResource(R.string.games_visit_website),
                onClick = { openUrl(context, url) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = Black
            )
        }
        game.campaign?.socialMedia?.discord?.takeIf { it.isNotBlank() }?.let { url ->
            OutlinedButton(
                onClick = { openUrl(context, url) },
                modifier = Modifier.fillMaxWidth()
            ) {
                StandardText(text = stringResource(R.string.games_join_discord))
            }
        }
    }
}

@Composable
private fun StoreButton(
    label: String,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color = White
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = textColor
        )
    ) {
        StandardText(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

@Composable
private fun CategoryChip(
    text: String,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = Modifier.padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        StandardText(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
