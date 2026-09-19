/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package com.tatumgames.tatumtech.android.ui.components.screens.career

import android.content.Intent
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.stats.AchievementTrackingKeys
import com.tatumgames.tatumtech.android.ui.components.screens.stats.EngagementTracker
import com.tatumgames.tatumtech.android.ui.models.CareerListing
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.ui.utils.JsonImporter
import kotlinx.coroutines.launch

@Composable
fun CareerScreen(navController: NavController) {
    val context = LocalContext.current
    var allListings by remember { mutableStateOf<List<CareerListing>>(emptyList()) }
    var loadFinished by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CareerListingFilters.FILTER_ALL) }
    var selectedEmploymentType by remember { mutableStateOf(CareerListingFilters.FILTER_ALL) }

    LaunchedEffect(Unit) {
        allListings = JsonImporter.loadCareerListings(context)
        loadFinished = true
    }

    val filteredListings =
        remember(allListings, searchQuery, selectedCategory, selectedEmploymentType) {
            CareerListingFilters.filter(
                listings = allListings,
                searchQuery = searchQuery,
                category = selectedCategory,
                employmentType = selectedEmploymentType
            )
        }

    val filtersActive = searchQuery.isNotBlank() ||
            selectedCategory != CareerListingFilters.FILTER_ALL ||
            selectedEmploymentType != CareerListingFilters.FILTER_ALL

    fun clearFilters() {
        searchQuery = ""
        selectedCategory = CareerListingFilters.FILTER_ALL
        selectedEmploymentType = CareerListingFilters.FILTER_ALL
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.title_career),
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
                        text = stringResource(R.string.career_loading),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            allListings.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    StandardText(
                        text = stringResource(R.string.career_no_listings),
                        style = MaterialTheme.typography.bodyMedium
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
                        CareerSearchFilters(
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it },
                            selectedEmploymentType = selectedEmploymentType,
                            onEmploymentTypeSelected = { selectedEmploymentType = it },
                            showClearFilters = filtersActive,
                            onClearFilters = { clearFilters() }
                        )
                    }

                    if (filteredListings.isEmpty()) {
                        item(key = "empty") {
                            CareerEmptyState(onClearFilters = { clearFilters() })
                        }
                    } else {
                        items(filteredListings, key = { it.id }) { listing ->
                            CareerCard(
                                listing = listing,
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
private fun CareerSearchFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedEmploymentType: String,
    onEmploymentTypeSelected: (String) -> Unit,
    showClearFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                StandardText(text = stringResource(R.string.career_search_hint))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { })
        )

        StandardText(
            text = stringResource(R.string.career_job_category),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CareerListingFilters.categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = {
                        StandardText(
                            text = if (category == CareerListingFilters.FILTER_ALL) {
                                stringResource(R.string.filter_all)
                            } else {
                                category
                            }
                        )
                    }
                )
            }
        }

        StandardText(
            text = stringResource(R.string.career_employment_type),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CareerListingFilters.employmentTypes.forEach { type ->
                FilterChip(
                    selected = selectedEmploymentType == type,
                    onClick = { onEmploymentTypeSelected(type) },
                    label = {
                        StandardText(
                            text = if (type == CareerListingFilters.FILTER_ALL) {
                                stringResource(R.string.filter_all)
                            } else {
                                type
                            }
                        )
                    }
                )
            }
        }

        if (showClearFilters) {
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.align(Alignment.End)
            ) {
                StandardText(
                    text = stringResource(R.string.career_clear_filters),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun CareerEmptyState(onClearFilters: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StandardText(
            text = stringResource(R.string.career_no_jobs_found),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        StandardText(
            text = stringResource(R.string.career_no_jobs_found_hint),
            style = MaterialTheme.typography.bodyMedium
        )
        TextButton(onClick = onClearFilters) {
            StandardText(
                text = stringResource(R.string.career_clear_filters),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun CareerCard(
    listing: CareerListing,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                text = listing.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            StandardText(
                text = listing.company,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        StandardText(
                            text = listing.category,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
                SuggestionChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        StandardText(
                            text = listing.employmentType,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            StandardText(
                text = listing.description,
                style = MaterialTheme.typography.bodyMedium
            )
            if (listing.technologies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                StandardText(
                    text = listing.technologies.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    scope.launch {
                        EngagementTracker.record(
                            context,
                            AchievementTrackingKeys.JOB_APPLY_CLICKED
                        )
                    }
                    val intent = Intent(Intent.ACTION_VIEW, listing.applyUrl.toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                StandardText(
                    text = stringResource(R.string.career_apply_now),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}
