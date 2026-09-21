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
import com.tatumgames.tatumtech.android.ui.components.screens.games.GamesResourcesResolver
import com.tatumgames.tatumtech.android.ui.models.GamesResourceCategory
import com.tatumgames.tatumtech.android.ui.models.Partner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GamesResourcesJsonValidationTest {

    @Test
    fun gamesResourcesJson_resolvesAgainstPartnersCatalog() {
        val categoriesType = object : TypeToken<List<GamesResourceCategory>>() {}.type
        val partnersType = object : TypeToken<List<Partner>>() {}.type
        val categories: List<GamesResourceCategory> = Gson().fromJson(
            File("src/main/assets/games_resources.json").readText(),
            categoriesType
        )
        val partners: List<Partner> = Gson().fromJson(
            File("src/main/assets/partners.json").readText(),
            partnersType
        )

        val entries = GamesResourcesResolver.resolve(categories, partners)
        val grouped = GamesResourcesResolver.groupByCategory(entries).toMap()

        assertEquals(
            listOf(
                "Technology Builder",
                "Product Management",
                "Monetization",
                "Distribution",
                "Infrastructure"
            ),
            grouped.keys.toList()
        )
        assertEquals(
            listOf("technology_red_apple_tech"),
            grouped["Technology Builder"]?.map { it.partner.id }
        )
        assertEquals(
            listOf("technology_korgi"),
            grouped["Product Management"]?.map { it.partner.id })
        assertEquals(listOf("technology_invo"), grouped["Monetization"]?.map { it.partner.id })
        assertEquals(
            listOf("technology_glitch", "technology_mikros"),
            grouped["Distribution"]?.map { it.partner.id }
        )
        assertEquals(
            listOf("technology_mikros"),
            grouped["Infrastructure"]?.map { it.partner.id }
        )

        val mikros = partners.first { it.id == "technology_mikros" }
        assertEquals("MIKROS", mikros.productName)
        assertEquals("https://developer.tatumgames.com/", mikros.websiteUrl)
        assertTrue(entries.count { it.partner.id == "technology_mikros" } == 2)
    }
}
