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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.entity.UserEntity
import com.tatumgames.tatumtech.android.database.repository.UserDatabaseRepository
import com.tatumgames.tatumtech.android.enums.HomePagerCategory
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.models.FeatureCardItem
import com.tatumgames.tatumtech.android.utils.Utils.generateAnonymousId
import com.tatumgames.tatumtech.android.utils.Utils.getUserNameOrAnonymous
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the home pager state and providing feature cards for each category.
 * Handles the horizontal pager logic and feature card data for the home screen.
 */
class HomePagerViewModel(
    private val userRepository: UserDatabaseRepository
) : ViewModel() {

    var userName by mutableStateOf("")
        private set

    init {
        initializeUser()
    }

    val pagerCategories: List<HomePagerCategory> = HomePagerCategory.entries

    /**
     * Returns the list of feature cards for a given category.
     * These will be displayed as FeatureCard components in the horizontal pager.
     *
     * @param category The pager category to get feature cards for
     * @return List of FeatureCardItem objects for the category
     */
    fun getItemsForCategory(category: HomePagerCategory): List<FeatureCardItem> {
        return when (category) {
            HomePagerCategory.EVENTS -> listOf(
                FeatureCardItem(
                    R.drawable.upcoming_events,
                    R.string.upcoming_events,
                    NavRoutes.UPCOMING_EVENTS_SCREEN
                ),
                FeatureCardItem(
                    R.drawable.scanner,
                    R.string.scanner,
                    NavRoutes.SCANNER_SCREEN
                ),
                FeatureCardItem(
                    R.drawable.partners,
                    R.string.title_partners,
                    NavRoutes.PARTNERS_SCREEN
                )
            )

            HomePagerCategory.CODING -> listOf(
                FeatureCardItem(
                    R.drawable.coding_challenges,
                    R.string.coding,
                    NavRoutes.CODING_CHALLENGES_SCREEN
                ),
                FeatureCardItem(
                    R.drawable.apps,
                    R.string.title_ai_llm_challenges,
                    NavRoutes.AI_LLM_CHALLENGES_SCREEN
                ),
                FeatureCardItem(
                    R.drawable.stats,
                    R.string.stats,
                    NavRoutes.STATS_SCREEN
                ),
                FeatureCardItem(
                    R.drawable.opportunity,
                    R.string.title_resources,
                    NavRoutes.RESOURCES_SCREEN
                )
            )

            HomePagerCategory.COMMUNITY -> listOf(
                FeatureCardItem(
                    R.drawable.community,
                    R.string.community,
                    NavRoutes.COMMUNITY_SCREEN
                ),
                FeatureCardItem(
                    R.drawable.donate,
                    R.string.donate,
                    NavRoutes.DONATE_SCREEN
                )
            )

            HomePagerCategory.CAREER -> listOf(
                FeatureCardItem(
                    R.drawable.jobs,
                    R.string.apply_for_jobs,
                    NavRoutes.CAREER_SCREEN
                ),
                FeatureCardItem(
                    R.drawable.coding_challenges,
                    R.string.title_leet_code_challenges,
                    NavRoutes.LEET_CODE_CHALLENGES_SCREEN
                ),
                FeatureCardItem(
                    R.drawable.career,
                    R.string.title_mock_interview_challenges,
                    NavRoutes.MOCK_INTERVIEW_CHALLENGES_SCREEN
                )
            )

            HomePagerCategory.GAMES -> listOf(
                FeatureCardItem(
                    R.drawable.games,
                    R.string.discover,
                    NavRoutes.GAMES_SCREEN
                ),
                FeatureCardItem(
                    R.drawable.opportunity,
                    R.string.title_resources,
                    NavRoutes.GAMES_RESOURCES_SCREEN
                )
            )
        }
    }

    /**
     * Initializes the current user information asynchronously in [viewModelScope].
     *
     * Creates an anonymous user when none exists; otherwise retrieves the existing
     * user's information. Updates [userName] as a result.
     */
    private fun initializeUser() {
        viewModelScope.launch {
            if (!userRepository.userExists()) {
                val anonymousId = generateAnonymousId()

                val newUser = UserEntity(
                    anonymousId = anonymousId,
                    firstName = null,
                    lastName = null,
                    name = anonymousId,
                    email = null
                )

                userRepository.insertUser(newUser)
                userName = anonymousId
            } else {
                val currentUser = userRepository.getCurrentUser()
                userName = getUserNameOrAnonymous(currentUser)
            }
        }
    }

    /**
     * Reinitializes/refreshes the current user information and updates [userName].
     */
    fun refreshUser() {
        initializeUser()
    }
}
