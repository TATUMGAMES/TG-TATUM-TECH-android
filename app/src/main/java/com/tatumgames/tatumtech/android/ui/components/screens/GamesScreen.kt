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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.games.DiscoveryCardStyle
import com.tatumgames.tatumtech.android.ui.components.screens.games.DiscoveryCategoryRow
import com.tatumgames.tatumtech.android.ui.components.screens.games.GameDiscoverySearchFilters
import com.tatumgames.tatumtech.android.ui.components.screens.games.MikrosFeatureCtaCard
import com.tatumgames.tatumtech.android.ui.components.screens.games.ShowcaseGameCard
import com.tatumgames.tatumtech.android.ui.components.screens.stats.AchievementTrackingKeys
import com.tatumgames.tatumtech.android.ui.components.screens.stats.EngagementTracker
import com.tatumgames.tatumtech.android.ui.models.GameDiscoveryUiState
import com.tatumgames.tatumtech.android.ui.models.GameModel
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import com.tatumgames.tatumtech.android.ui.viewmodels.GamesViewModel
import com.tatumgames.tatumtech.android.ui.viewmodels.factory.GamesViewModelFactory
import kotlinx.coroutines.launch

private enum class GamesScreenTab {
    FEATURED,
    GAMES
}

@Composable
fun GamesScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: GamesViewModel = viewModel(factory = GamesViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(GamesScreenTab.FEATURED.ordinal) }

    LaunchedEffect(Unit) {
        viewModel.loadGames()
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.discover_games),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = ScreenScaffoldLight
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.allGames.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.allGames.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StandardText(
                        text = uiState.errorMessage
                            ?: stringResource(R.string.something_went_wrong),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val tabs = listOf(
                        stringResource(R.string.games_tab_featured),
                        stringResource(R.string.games_tab_games)
                    )
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    StandardText(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (selectedTab == index) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        }
                                    )
                                }
                            )
                        }
                    }

                    when (GamesScreenTab.entries[selectedTab]) {
                        GamesScreenTab.FEATURED -> FeaturedGamesTab(
                            uiState = uiState,
                            navController = navController
                        )

                        GamesScreenTab.GAMES -> MikrosGamesTab(
                            uiState = uiState,
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedGamesTab(
    uiState: GameDiscoveryUiState,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val showcaseGames = remember(uiState.allGames) {
        uiState.allGames.sortedByDescending { it.featuredPriority }
    }

    if (showcaseGames.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            StandardText(
                text = stringResource(R.string.games_featured_empty),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(showcaseGames, key = { it.appId }) { game ->
            ShowcaseGameCard(
                game = game,
                onClick = {
                    scope.launch {
                        EngagementTracker.record(
                            context,
                            AchievementTrackingKeys.GAME_DETAILS_VIEWED
                        )
                    }
                    navController.navigate(NavRoutes.gameDetailsRoute(game.appId))
                }
            )
        }

        item(key = "mikros_cta") {
            Spacer(modifier = Modifier.height(8.dp))
            MikrosFeatureCtaCard(
                onLearnMoreClick = {
                    navController.navigate(NavRoutes.GET_YOUR_GAME_DISCOVERED_SCREEN)
                }
            )
        }
    }
}

@Composable
private fun MikrosGamesTab(
    uiState: GameDiscoveryUiState,
    viewModel: GamesViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val onGameClick: (GameModel) -> Unit = { game ->
        scope.launch {
            EngagementTracker.record(context, AchievementTrackingKeys.GAME_DETAILS_VIEWED)
        }
        navController.navigate(NavRoutes.gameDetailsRoute(game.appId))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "search") {
            Spacer(modifier = Modifier.height(8.dp))
            GameDiscoverySearchFilters(
                searchQuery = uiState.searchQuery,
                onSearchChange = viewModel::setSearchQuery,
                availableGenres = uiState.availableGenres,
                selectedGenre = uiState.selectedGenre,
                onGenreSelected = viewModel::setGenre,
                gameplayTypes = uiState.gameplayTypes,
                selectedGameplay = uiState.selectedGameplayType,
                onGameplaySelected = viewModel::setGameplayType
            )
        }

        if (uiState.justTooFun.isNotEmpty()) {
            item(key = "justTooFun") {
                DiscoveryCategoryRow(
                    title = stringResource(R.string.games_section_just_too_fun),
                    games = uiState.justTooFun,
                    style = DiscoveryCardStyle.Wide,
                    onGameClick = onGameClick
                )
            }
        }

        if (uiState.tatumGamesFavorites.isNotEmpty()) {
            item(key = "tatumFavorites") {
                DiscoveryCategoryRow(
                    title = stringResource(R.string.games_section_tatum_favorites),
                    games = uiState.tatumGamesFavorites,
                    style = DiscoveryCardStyle.Tall,
                    onGameClick = onGameClick
                )
            }
        }

        if (uiState.appsInDevelopment.isNotEmpty()) {
            item(key = "inDevelopment") {
                DiscoveryCategoryRow(
                    title = stringResource(R.string.games_section_apps_in_development),
                    games = uiState.appsInDevelopment,
                    style = DiscoveryCardStyle.Compact,
                    onGameClick = onGameClick
                )
            }
        }

        if (uiState.casualGamer.isNotEmpty()) {
            item(key = "casualGamer") {
                DiscoveryCategoryRow(
                    title = stringResource(R.string.games_section_casual_gamer),
                    games = uiState.casualGamer,
                    style = DiscoveryCardStyle.Wide,
                    onGameClick = onGameClick
                )
            }
        }

        if (uiState.coreGamer.isNotEmpty()) {
            item(key = "coreGamer") {
                DiscoveryCategoryRow(
                    title = stringResource(R.string.games_section_core_gamer),
                    games = uiState.coreGamer,
                    style = DiscoveryCardStyle.Tall,
                    onGameClick = onGameClick
                )
            }
        }

        if (
            uiState.justTooFun.isEmpty() &&
            uiState.tatumGamesFavorites.isEmpty() &&
            uiState.appsInDevelopment.isEmpty() &&
            uiState.casualGamer.isEmpty() &&
            uiState.coreGamer.isEmpty()
        ) {
            item(key = "emptySections") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StandardText(
                        text = stringResource(R.string.games_sections_empty),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item(key = "mikros_cta") {
            MikrosFeatureCtaCard(
                onLearnMoreClick = {
                    navController.navigate(NavRoutes.GET_YOUR_GAME_DISCOVERED_SCREEN)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
