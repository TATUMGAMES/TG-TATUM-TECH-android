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

import com.tatumgames.tatumtech.android.ui.models.CareerListing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerListingFiltersTest {

    private val sample = listOf(
        listing(
            id = "job_0001",
            company = "Riot Games",
            title = "Software Engineer",
            category = "Software Engineering",
            employmentType = "Full-time",
            description = "Build live multiplayer services.",
            technologies = listOf("Kotlin", "Java")
        ),
        listing(
            id = "job_0002",
            company = "Epic Games",
            title = "Unreal Engine Developer",
            category = "Game Development",
            employmentType = "Full-time",
            description = "Extend Unreal Engine tooling.",
            technologies = listOf("C++", "Unreal Engine")
        ),
        listing(
            id = "job_0003",
            company = "OpenAI",
            title = "AI Engineer",
            category = "AI / Machine Learning",
            employmentType = "Internship",
            description = "Work on language models and evaluation.",
            technologies = listOf("Python", "LLMs")
        ),
        listing(
            id = "job_0004",
            company = "Microsoft",
            title = "Contract Cloud Engineer",
            category = "Backend / Cloud",
            employmentType = "Contract",
            description = "Azure cloud infrastructure.",
            technologies = listOf("C#", "Azure")
        )
    )

    @Test
    fun search_matchesCompanyCaseInsensitive() {
        val result = CareerListingFilters.filter(sample, "riot", "All", "All")
        assertEquals(listOf("job_0001"), result.map { it.id })
    }

    @Test
    fun search_matchesTechnology() {
        val result = CareerListingFilters.filter(sample, "Unreal", "All", "All")
        assertEquals(listOf("job_0002"), result.map { it.id })
    }

    @Test
    fun search_matchesDescription() {
        val result = CareerListingFilters.filter(sample, "language models", "All", "All")
        assertEquals(listOf("job_0003"), result.map { it.id })
    }

    @Test
    fun category_filtersIndependently() {
        val result = CareerListingFilters.filter(sample, "", "Game Development", "All")
        assertEquals(listOf("job_0002"), result.map { it.id })
    }

    @Test
    fun employmentType_filtersIndependently() {
        val result = CareerListingFilters.filter(sample, "", "All", "Internship")
        assertEquals(listOf("job_0003"), result.map { it.id })
    }

    @Test
    fun combined_searchCategoryAndEmployment() {
        val result = CareerListingFilters.filter(
            sample,
            searchQuery = "Engineer",
            category = "Software Engineering",
            employmentType = "Full-time"
        )
        assertEquals(listOf("job_0001"), result.map { it.id })
    }

    @Test
    fun allFilters_returnsEverything() {
        assertEquals(4, CareerListingFilters.filter(sample, "", "All", "All").size)
    }

    @Test
    fun emptySearch_withRestrictiveChips_canYieldEmpty() {
        val result = CareerListingFilters.filter(sample, "zzz-no-match", "Design", "Part-time")
        assertTrue(result.isEmpty())
    }

    @Test
    fun matches_doesNotRequireCategoryWhenAll() {
        assertTrue(
            CareerListingFilters.matches(sample[0], "Kotlin", "All", "All")
        )
        assertFalse(
            CareerListingFilters.matches(sample[0], "Kotlin", "Design", "All")
        )
    }

    private fun listing(
        id: String,
        company: String,
        title: String,
        category: String,
        employmentType: String,
        description: String,
        technologies: List<String>
    ) = CareerListing(
        id = id,
        company = company,
        title = title,
        category = category,
        employmentType = employmentType,
        description = description,
        technologies = technologies,
        applyUrl = "https://example.com/careers"
    )
}
