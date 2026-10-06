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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.ui.components.screens.main.FeatureCard
import com.tatumgames.tatumtech.android.ui.models.FeatureCardItem

/**
 * Composable that displays feature cards in a grid layout for a pager page.
 * Contains ONLY FeatureCards - no notifications section.
 * FeatureCard grid layout for a single home-pager category.
 *
 * @param items List of FeatureCardItem objects to display
 * @param navController Navigation controller for handling card clicks
 */
@Composable
fun PagerPageContent(
    items: List<FeatureCardItem>,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // FeatureCard grid
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Group items into rows of 2 (except last item if odd count, make it full width)
            // Use chunked(2) to safely handle pairs of items
            items.chunked(2).forEach { rowItems ->
                if (rowItems.size == 2) {
                    // Two cards side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { item ->
                            FeatureCard(
                                image = painterResource(id = item.imageResId),
                                text = stringResource(id = item.titleResId),
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(item.route) }
                            )
                        }
                    }
                } else {
                    // Single card full width (last item if odd count)
                    rowItems.forEach { item ->
                        FeatureCard(
                            image = painterResource(id = item.imageResId),
                            text = stringResource(id = item.titleResId),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate(item.route) }
                        )
                    }
                }
            }
        }
    }
}
