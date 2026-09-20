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

import com.tatumgames.tatumtech.android.ui.models.GamesResourceCategory
import com.tatumgames.tatumtech.android.ui.models.Partner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamesResourcesResolverTest {

    private val partners = listOf(
        Partner(
            id = "technology_red_apple_tech",
            name = "Red Apple Tech",
            category = "Technology Partners",
            websiteUrl = "https://redappletech.com/"
        ),
        Partner(
            id = "technology_korgi",
            name = "Korgi",
            category = "Technology Partners",
            websiteUrl = "https://korgiboard.com"
        ),
        Partner(
            id = "technology_invo",
            name = "INVO Tech",
            category = "Technology Partners",
            websiteUrl = "https://www.ourinvo.com/"
        ),
        Partner(
            id = "technology_glitch",
            name = "Glitch",
            category = "Technology Partners",
            websiteUrl = "https://www.glitch.fun/"
        ),
        Partner(
            id = "technology_mikros",
            name = "Tatum Games",
            category = "Technology Partners",
            productName = "MIKROS",
            websiteUrl = "https://developer.tatumgames.com/"
        ),
        Partner(
            id = "corporate_disney_imagineering",
            name = "Disney Imagineering",
            category = "Corporate Partners"
        )
    )

    private val categories = listOf(
        GamesResourceCategory("Technology Builder", listOf("technology_red_apple_tech")),
        GamesResourceCategory("Product Management", listOf("technology_korgi")),
        GamesResourceCategory("Monetization", listOf("technology_invo")),
        GamesResourceCategory(
            "Distribution",
            listOf("technology_glitch", "technology_mikros")
        ),
        GamesResourceCategory("Infrastructure", listOf("technology_mikros"))
    )

    @Test
    fun resolve_mapsRequiredPartnersToCategories() {
        val entries = GamesResourcesResolver.resolve(categories, partners)
        val grouped = GamesResourcesResolver.groupByCategory(entries).toMap()

        assertEquals(
            listOf("Red Apple Tech"),
            grouped["Technology Builder"]?.map { it.displayName }
        )
        assertEquals(listOf("Korgi"), grouped["Product Management"]?.map { it.displayName })
        assertEquals(listOf("INVO Tech"), grouped["Monetization"]?.map { it.displayName })
        assertEquals(
            listOf("Glitch", "MIKROS"),
            grouped["Distribution"]?.map { it.displayName }
        )
        assertEquals(listOf("MIKROS"), grouped["Infrastructure"]?.map { it.displayName })
    }

    @Test
    fun resolve_mikrosAppearsInDistributionAndInfrastructure() {
        val entries = GamesResourcesResolver.resolve(categories, partners)
        val mikrosCategories = entries
            .filter { it.partner.id == "technology_mikros" }
            .map { it.category }

        assertEquals(listOf("Distribution", "Infrastructure"), mikrosCategories)
    }

    @Test
    fun resolve_excludesUnrelatedPartners() {
        val entries = GamesResourcesResolver.resolve(categories, partners)
        assertFalse(entries.any { it.partner.id == "corporate_disney_imagineering" })
        assertEquals(6, entries.size)
    }

    @Test
    fun resolve_omitsMissingPartnerIdsWithoutCrashing() {
        val withMissing = listOf(
            GamesResourceCategory(
                "Distribution",
                listOf("technology_glitch", "missing_partner", "technology_mikros")
            )
        )
        val entries = GamesResourcesResolver.resolve(withMissing, partners)
        assertEquals(2, entries.size)
        assertEquals(
            listOf("technology_glitch", "technology_mikros"),
            entries.map { it.partner.id }
        )
    }

    @Test
    fun resolve_partnerFieldsComeFromPartnersDataset() {
        val entries = GamesResourcesResolver.resolve(categories, partners)
        val mikros = entries.first { it.partner.id == "technology_mikros" }

        assertEquals("MIKROS", mikros.displayName)
        assertEquals("https://developer.tatumgames.com/", mikros.partner.websiteUrl)
        assertTrue(mikros.partner === partners.first { it.id == "technology_mikros" })
    }

    @Test
    fun groupByCategory_skipsEmptyCategories() {
        val entries = GamesResourcesResolver.resolve(
            categories = listOf(
                GamesResourceCategory("Empty", listOf("does_not_exist")),
                GamesResourceCategory("Monetization", listOf("technology_invo"))
            ),
            partners = partners
        )
        val grouped = GamesResourcesResolver.groupByCategory(entries)
        assertEquals(1, grouped.size)
        assertEquals("Monetization", grouped.first().first)
    }
}
