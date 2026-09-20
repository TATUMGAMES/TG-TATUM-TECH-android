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
package com.tatumgames.tatumtech.android.ui.models

/**
 * UI state for Discover Games (Featured carousel + Mikros Games sections).
 */
data class GameDiscoveryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    /** Null or "All" = no genre filter */
    val selectedGenre: String? = null,
    /** Null or "All" = no gameplay filter */
    val selectedGameplayType: String? = null,
    val allGames: List<GameModel> = emptyList(),
    val filteredGames: List<GameModel> = emptyList(),
    val heroFeatured: List<GameModel> = emptyList(),
    val justTooFun: List<GameModel> = emptyList(),
    val tatumGamesFavorites: List<GameModel> = emptyList(),
    val appsInDevelopment: List<GameModel> = emptyList(),
    val casualGamer: List<GameModel> = emptyList(),
    val coreGamer: List<GameModel> = emptyList(),
    val availableGenres: List<String> = emptyList(),
    val gameplayTypes: List<String> = listOf("All", "Casual", "Non-Casual")
)
