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
package com.tatumgames.tatumtech.android.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tatumgames.tatumtech.android.enums.HomePagerCategory

/**
 * Composable for displaying category tabs in the horizontal pager.
 *
 * @param categories List of pager categories to display as tabs
 * @param selectedIndex Index of the currently selected tab
 * @param onTabSelected Callback when a tab is selected
 */
@Composable
fun PagerTabBar(
    categories: List<HomePagerCategory>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        categories.forEachIndexed { index, category ->
            val isSelected = index == selectedIndex
            StandardText(
                text = stringResource(id = category.displayNameResId),
                style = if (isSelected) {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.titleMedium
                },
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                },
                modifier = Modifier
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
