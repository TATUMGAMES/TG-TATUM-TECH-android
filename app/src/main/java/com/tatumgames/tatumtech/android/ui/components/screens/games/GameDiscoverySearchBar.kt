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

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.StandardText

@Composable
fun GameDiscoverySearchFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    availableGenres: List<String>,
    selectedGenre: String?,
    onGenreSelected: (String?) -> Unit,
    gameplayTypes: List<String>,
    selectedGameplay: String?,
    onGameplaySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                StandardText(text = stringResource(R.string.search_games))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { })
        )
        StandardText(
            text = stringResource(R.string.genre),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableGenres.forEach { g ->
                val selected = when {
                    g == "All" -> selectedGenre == null
                    else -> selectedGenre == g
                }
                FilterChip(
                    selected = selected,
                    onClick = {
                        onGenreSelected(if (g == "All") null else g)
                    },
                    label = {
                        StandardText(
                            text = if (g == "All") stringResource(R.string.filter_all) else g
                        )
                    }
                )
            }
        }
        StandardText(
            text = stringResource(R.string.gameplay),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            gameplayTypes.forEach { t ->
                val selected = when {
                    t == "All" -> selectedGameplay == null
                    else -> selectedGameplay == t
                }
                FilterChip(
                    selected = selected,
                    onClick = {
                        onGameplaySelected(if (t == "All") null else t)
                    },
                    label = {
                        StandardText(
                            text = when (t) {
                                "All" -> stringResource(R.string.filter_all)
                                "Casual" -> stringResource(R.string.gameplay_casual)
                                "Non-Casual" -> stringResource(R.string.gameplay_non_casual)
                                else -> t
                            }
                        )
                    }
                )
            }
        }
    }
}
