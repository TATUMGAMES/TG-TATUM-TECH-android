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
package com.tatumgames.tatumtech.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tatumgames.tatumtech.android.data.games.GameRepository
import com.tatumgames.tatumtech.android.ui.models.GameDiscoveryUiState
import com.tatumgames.tatumtech.android.ui.models.GameModel
import com.tatumgames.tatumtech.android.ui.models.GamesCatalogSections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * ViewModel for game discovery and details. Loads catalog via [GameRepository].
 */
class GamesViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDiscoveryUiState())
    val uiState: StateFlow<GameDiscoveryUiState> = _uiState.asStateFlow()

    fun loadGames() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = withContext(Dispatchers.IO) { gameRepository.loadCatalog() }
            result.fold(
                onSuccess = { games ->
                    _uiState.update {
                        val base = it.copy(
                            isLoading = false,
                            errorMessage = null,
                            allGames = games,
                            availableGenres = buildGenreList(games)
                        )
                        recompute(base)
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message,
                            allGames = emptyList()
                        ).let { s -> recompute(s) }
                    }
                }
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { recompute(it.copy(searchQuery = query)) }
    }

    fun setGenre(genre: String?) {
        _uiState.update { recompute(it.copy(selectedGenre = genre)) }
    }

    fun setGameplayType(type: String?) {
        _uiState.update { recompute(it.copy(selectedGameplayType = type)) }
    }

    fun getGameById(gameId: String): GameModel? =
        _uiState.value.allGames.find { it.appId == gameId }

    private fun buildGenreList(games: List<GameModel>): List<String> {
        val set = linkedSetOf<String>()
        set.add("All")
        games.forEach { g ->
            g.genres?.forEach { x -> if (x.isNotBlank()) set.add(x) }
            g.gameGenre?.takeIf { it.isNotBlank() }?.let { set.add(it) }
            g.appCategory.takeIf { it.isNotBlank() }?.let { set.add(it) }
        }
        return set.toList()
    }

    private fun recompute(state: GameDiscoveryUiState): GameDiscoveryUiState {
        val q = state.searchQuery.trim()
        val now = Instant.now()
        var list = state.allGames

        if (q.isNotEmpty()) {
            list = list.filter { game ->
                game.appName.contains(q, ignoreCase = true) ||
                        game.title.contains(q, ignoreCase = true) ||
                        game.shortDesc.contains(q, ignoreCase = true) ||
                        game.fullDesc.contains(q, ignoreCase = true)
            }
        }

        val genreFilter = state.selectedGenre?.takeIf { it != "All" }
        if (genreFilter != null) {
            list = list.filter { game ->
                game.genres?.any { it.equals(genreFilter, ignoreCase = true) } == true ||
                        game.gameGenre?.equals(genreFilter, ignoreCase = true) == true ||
                        game.appCategory.equals(genreFilter, ignoreCase = true)
            }
        }

        val gpFilter = state.selectedGameplayType?.takeIf { it != "All" }
        if (gpFilter != null) {
            list = list.filter { game ->
                game.gameplayType?.equals(gpFilter, ignoreCase = true) == true
            }
        }

        val hero = list
            .filter { isHeroEligible(it, now) }
            .sortedWith(
                compareByDescending<GameModel> { if (it.isFeatured) it.featuredPriority else 0 }
                    .thenByDescending { it.popularityScore }
            )

        return state.copy(
            filteredGames = list,
            heroFeatured = hero,
            justTooFun = GamesCatalogSections.justTooFun(list),
            tatumGamesFavorites = GamesCatalogSections.tatumGamesFavorites(list),
            appsInDevelopment = GamesCatalogSections.appsInDevelopment(list),
            casualGamer = GamesCatalogSections.casualGamer(list),
            coreGamer = GamesCatalogSections.coreGamer(list)
        )
    }

    /**
     * Inclusive window: [featuredStartDate, featuredEndDate] when both set; open-ended if one side null.
     */
    private fun inFeaturedWindow(game: GameModel, now: Instant): Boolean {
        if (!game.isFeatured) return false
        val start = game.featuredStartDate?.let { parseInstantOrNull(it) }
        val end = game.featuredEndDate?.let { parseInstantOrNull(it) }
        if (start == null && end == null) return true
        val afterStart = start == null || !now.isBefore(start)
        val beforeEnd = end == null || !now.isAfter(end)
        return afterStart && beforeEnd
    }

    /** New schema: [isFeatured] + window; legacy JSON: [marketingCampaignActive] only. */
    private fun isHeroEligible(game: GameModel, now: Instant): Boolean {
        return (game.isFeatured && inFeaturedWindow(game, now)) ||
                (!game.isFeatured && game.marketingCampaignActive)
    }

    private fun parseInstantOrNull(s: String): Instant? {
        return try {
            Instant.parse(s)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
