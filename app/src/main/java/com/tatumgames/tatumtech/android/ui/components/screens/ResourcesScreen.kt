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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.resources.ResourceCategoryFilters
import com.tatumgames.tatumtech.android.ui.models.ResourceLink
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.Grey500
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.ui.utils.JsonImporter
import com.tatumgames.tatumtech.android.utils.Utils.openUrl

@Composable
fun ResourcesScreen(navController: NavController) {
    val context = LocalContext.current
    var resources by remember { mutableStateOf<List<ResourceLink>>(emptyList()) }
    var loadFinished by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(ResourceCategoryFilters.FILTER_ALL) }

    LaunchedEffect(Unit) {
        resources = JsonImporter.loadResources(context)
        loadFinished = true
    }

    val filteredResources = remember(resources, selectedCategory) {
        ResourceCategoryFilters.filter(resources, selectedCategory)
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.title_resources),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = ScreenScaffoldLight
    ) { paddingValues ->
        when {
            !loadFinished -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    StandardText(
                        text = stringResource(R.string.loading_resources),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            resources.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StandardText(
                        text = stringResource(R.string.resources_none_available),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "filters") {
                        ResourceCategoryChipRow(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )
                    }

                    if (filteredResources.isEmpty()) {
                        item(key = "category_empty") {
                            ResourcesCategoryEmptyState()
                        }
                    } else {
                        items(filteredResources, key = { it.id }) { resource ->
                            ResourceCard(
                                resource = resource,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceCategoryChipRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StandardText(
            text = stringResource(R.string.resources_filter_label),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = 8.dp),
            color = Black
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResourceCategoryFilters.categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = {
                        StandardText(
                            text = if (category == ResourceCategoryFilters.FILTER_ALL) {
                                stringResource(R.string.filter_all)
                            } else {
                                category
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ResourcesCategoryEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        StandardText(
            text = stringResource(R.string.resources_category_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = Grey500,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ResourceCard(
    resource: ResourceLink,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                role = Role.Button,
                onClick = { openUrl(context, resource.url) }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            StandardText(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            resource.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(4.dp))
                StandardText(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Grey500
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val meta = buildList {
                resource.source?.takeIf { it.isNotBlank() }?.let { add(it) }
                resource.resourceType?.takeIf { it.isNotBlank() }?.let { add(it) }
                if (resource.categories.isNotEmpty()) {
                    add(resource.categories.joinToString(" · "))
                }
            }.joinToString(" · ")
            if (meta.isNotBlank()) {
                StandardText(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            StandardText(
                text = stringResource(R.string.visit),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
