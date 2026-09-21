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
 * Builds Mikros Games-tab section lists from catalog fields already present on [GameModel].
 *
 * Filtering uses [GameModel.userSubscriptionType], [GameModel.releaseStatus], and
 * [GameModel.gameplayType]. Games may appear in more than one section when criteria overlap
 * (for example Enterprise + not released).
 */
object GamesCatalogSections {

    fun justTooFun(games: List<GameModel>): List<GameModel> =
        games.filter { it.userSubscriptionType.equals(SUBSCRIPTION_ENTERPRISE, ignoreCase = true) }

    fun tatumGamesFavorites(games: List<GameModel>): List<GameModel> =
        games.filter { it.userSubscriptionType.equals(SUBSCRIPTION_STARTUP, ignoreCase = true) }

    fun appsInDevelopment(games: List<GameModel>): List<GameModel> =
        games.filter { !it.releaseStatus.equals(STATUS_RELEASED, ignoreCase = true) }

    fun casualGamer(games: List<GameModel>): List<GameModel> =
        games.filter {
            it.userSubscriptionType.equals(SUBSCRIPTION_FREE, ignoreCase = true) &&
                    it.gameplayType.equals(GAMEPLAY_CASUAL, ignoreCase = true)
        }

    fun coreGamer(games: List<GameModel>): List<GameModel> =
        games.filter {
            it.userSubscriptionType.equals(SUBSCRIPTION_FREE, ignoreCase = true) &&
                    it.gameplayType.equals(GAMEPLAY_NON_CASUAL, ignoreCase = true)
        }

    private const val SUBSCRIPTION_ENTERPRISE = "Enterprise"
    private const val SUBSCRIPTION_STARTUP = "Startup"
    private const val SUBSCRIPTION_FREE = "Free"
    private const val STATUS_RELEASED = "Released"
    private const val GAMEPLAY_CASUAL = "Casual"
    private const val GAMEPLAY_NON_CASUAL = "Non-Casual"
}
