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
package com.tatumgames.tatumtech.android.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tatumgames.tatumtech.android.ui.components.screens.career.CareerListingFilters
import com.tatumgames.tatumtech.android.ui.models.CareerListing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CareerListingsJsonValidationTest {

    private val idRegex = Regex("""^job_\d{4}$""")
    private val allowedCategories = CareerListingFilters.categories.filter {
        it != CareerListingFilters.FILTER_ALL
    }.toSet()
    private val allowedEmployment = CareerListingFilters.employmentTypes.filter {
        it != CareerListingFilters.FILTER_ALL
    }.toSet()

    @Test
    fun careerListings_areValidStructuredDirectory() {
        val json = File("src/main/assets/career_listings.json").readText()
        assertFalse(json.contains("postedDate"))
        assertFalse(json.contains("expirationDate"))

        val type = object : TypeToken<List<CareerListing>>() {}.type
        val listings: List<CareerListing> = Gson().fromJson(json, type)

        assertTrue("Expected a substantial curated directory", listings.size >= 40)

        val ids = listings.map { it.id }
        assertEquals(ids.size, ids.toSet().size)

        listings.forEach { job ->
            assertTrue("Bad id: ${job.id}", idRegex.matches(job.id))
            assertTrue(job.company.isNotBlank())
            assertTrue(job.title.isNotBlank())
            assertTrue(job.description.isNotBlank())
            assertTrue(job.applyUrl.startsWith("https://"))
            assertTrue("Unknown category: ${job.category}", job.category in allowedCategories)
            assertTrue(
                "Unknown employmentType: ${job.employmentType}",
                job.employmentType in allowedEmployment
            )
            assertTrue(job.technologies.isNotEmpty())
            assertTrue(job.technologies.all { it.isNotBlank() })
        }
    }
}
