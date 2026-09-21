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
package com.tatumgames.tatumtech.android.ui.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.enums.HomePagerCategory
import com.tatumgames.tatumtech.android.ui.models.FeatureCardItem

/**
 * Composable for the horizontal pager layout containing ONLY FeatureCard grids.
 * PagerTabBar, notifications, etc. are rendered OUTSIDE this component.
 *
 * @param categories List of pager categories for the pager pages
 * @param getItemsForCategory Function to get FeatureCardItems for a given category
 * @param navController Navigation controller for feature card clicks
 * @param modifier Modifier for the layout
 * @param pagerState The pager state (shared with parent for tab sync)
 */
@Composable
fun HorizontalPagerLayout(
    categories: List<HomePagerCategory>,
    getItemsForCategory: (HomePagerCategory) -> List<FeatureCardItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
    pagerState: PagerState
) {
    Column(modifier = modifier) {
        // HorizontalPager contains ONLY FeatureCard grids
        // PagerTabBar, notifications, etc. are rendered OUTSIDE this component
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
        ) { page ->
            val category = categories[page]
            val items = getItemsForCategory(category)
            PagerPageContent(
                items = items,
                navController = navController
            )
        }
    }
}
