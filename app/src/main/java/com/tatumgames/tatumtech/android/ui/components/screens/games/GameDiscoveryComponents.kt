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
package com.tatumgames.tatumtech.android.ui.components.screens.games

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.models.GameModel
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.ui.utils.GameMediaResolver
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class DiscoveryCardStyle {
    Compact,
    Tall,
    Wide
}

@Composable
fun DiscoveryHeroPager(
    games: List<GameModel>,
    onGameClick: (GameModel) -> Unit,
    onCtaClick: (GameModel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (games.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { games.size })
    val context = LocalContext.current

    LaunchedEffect(games.size) {
        if (games.size <= 1) return@LaunchedEffect
        while (isActive) {
            delay(5500)
            val next = (pagerState.currentPage + 1) % games.size
            pagerState.scrollToPage(next)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val game = games[page]
            DiscoveryHeroCard(
                game = game,
                onClick = { onGameClick(game) },
                onCtaClick = { onCtaClick(game) },
                imageLoaderContext = context
            )
        }

        if (games.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(games.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(if (selected) 16.dp else 8.dp)
                            .background(
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                },
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoveryHeroCard(
    game: GameModel,
    onClick: () -> Unit,
    onCtaClick: () -> Unit,
    imageLoaderContext: android.content.Context
) {
    val heroUrl = GameMediaResolver.resolve(
        imageLoaderContext,
        game.images.featureGraphics.firstOrNull() ?: game.campaign?.images?.appLogo
    )
    val comingSoon = GameMediaResolver.isComingSoon(game)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(imageLoaderContext)
                    .data(heroUrl)
                    .crossfade(300)
                    .build(),
                contentDescription = game.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.games),
                error = painterResource(R.drawable.games)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                // Dynamic catalog titles need ellipsis via StandardText maxLines.
                StandardText(
                    text = game.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = White
                    ),
                    color = White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                StandardText(
                    text = if (comingSoon) {
                        stringResource(R.string.games_status_coming_soon)
                    } else {
                        game.shortDesc
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = White.copy(alpha = 0.92f)),
                    color = White.copy(alpha = 0.92f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onCtaClick,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    StandardText(
                        text = stringResource(
                            if (comingSoon) R.string.games_view_details else R.string.play_now
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoveryCategoryRow(
    title: String,
    games: List<GameModel>,
    style: DiscoveryCardStyle,
    onGameClick: (GameModel) -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        StandardText(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(games) { _, game ->
                when (style) {
                    DiscoveryCardStyle.Compact -> DiscoveryGameCardCompact(
                        game,
                        context,
                        onGameClick
                    )

                    DiscoveryCardStyle.Tall -> DiscoveryGameCardTall(game, context, onGameClick)
                    DiscoveryCardStyle.Wide -> DiscoveryGameCardWide(game, context, onGameClick)
                }
            }
        }
    }
}

@Composable
private fun DiscoveryGameCardCompact(
    game: GameModel,
    context: android.content.Context,
    onGameClick: (GameModel) -> Unit
) {
    Card(
        onClick = { onGameClick(game) },
        modifier = Modifier
            .width(130.dp)
            .height(180.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(
                        GameMediaResolver.resolve(
                            context,
                            game.images.featureGraphics.firstOrNull()
                        )
                    )
                    .crossfade(200).build(),
                contentDescription = game.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.games),
                error = painterResource(R.drawable.games)
            )
            StandardText(
                text = game.title,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DiscoveryGameCardTall(
    game: GameModel,
    context: android.content.Context,
    onGameClick: (GameModel) -> Unit
) {
    Card(
        onClick = { onGameClick(game) },
        modifier = Modifier
            .width(120.dp)
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(
                        GameMediaResolver.resolve(
                            context,
                            game.images.featureGraphics.firstOrNull()
                        )
                    )
                    .crossfade(200).build(),
                contentDescription = game.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.games),
                error = painterResource(R.drawable.games)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Black.copy(alpha = 0.55f))
                    .padding(8.dp)
            ) {
                StandardText(
                    text = game.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = White,
                        fontWeight = FontWeight.Bold
                    ),
                    color = White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DiscoveryGameCardWide(
    game: GameModel,
    context: android.content.Context,
    onGameClick: (GameModel) -> Unit
) {
    Card(
        onClick = { onGameClick(game) },
        modifier = Modifier
            .width(240.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(
                        GameMediaResolver.resolve(
                            context,
                            game.images.featureGraphics.firstOrNull()
                        )
                    )
                    .crossfade(200).build(),
                contentDescription = game.title,
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.games),
                error = painterResource(R.drawable.games)
            )
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                StandardText(
                    text = game.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                StandardText(
                    text = game.shortDesc,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
