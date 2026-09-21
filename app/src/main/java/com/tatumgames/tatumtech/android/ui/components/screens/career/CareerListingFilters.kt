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
package com.tatumgames.tatumtech.android.ui.components.screens.career

import com.tatumgames.tatumtech.android.ui.components.screens.career.CareerListingFilters.FILTER_ALL
import com.tatumgames.tatumtech.android.ui.models.CareerListing

/**
 * Controlled vocabularies and combined search/filter for career listings.
 */
object CareerListingFilters {

    const val FILTER_ALL = "All"

    val categories: List<String> = listOf(
        FILTER_ALL,
        "Software Engineering",
        "Game Development",
        "AI / Machine Learning",
        "Product",
        "Design",
        "Data / Analytics",
        "Graphics / Rendering",
        "Backend / Cloud",
        "Other"
    )

    val employmentTypes: List<String> = listOf(
        FILTER_ALL,
        "Full-time",
        "Part-time",
        "Contract",
        "Internship"
    )

    /**
     * Case-insensitive match across title, company, description, and technologies.
     * Category / employment type of [FILTER_ALL] means no restriction.
     */
    fun matches(
        listing: CareerListing,
        searchQuery: String,
        category: String,
        employmentType: String
    ): Boolean {
        if (category != FILTER_ALL && listing.category != category) return false
        if (employmentType != FILTER_ALL && listing.employmentType != employmentType) return false

        val query = searchQuery.trim()
        if (query.isEmpty()) return true

        val haystack = buildString {
            append(listing.title)
            append(' ')
            append(listing.company)
            append(' ')
            append(listing.description)
            append(' ')
            listing.technologies.forEach { tech ->
                append(tech)
                append(' ')
            }
        }
        return haystack.contains(query, ignoreCase = true)
    }

    fun filter(
        listings: List<CareerListing>,
        searchQuery: String,
        category: String,
        employmentType: String
    ): List<CareerListing> =
        listings.filter { matches(it, searchQuery, category, employmentType) }
}
