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
import com.tatumgames.tatumtech.android.ui.components.screens.partners.PartnerCategoryFilters
import com.tatumgames.tatumtech.android.ui.models.Partner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PartnersJsonValidationTest {

    @Test
    fun partnersJson_isFlexibleCuratedDirectory() {
        val json = File("src/main/assets/partners.json").readText()
        assertFalse(json.contains("participationCount"))
        assertFalse(json.contains("partnerSince"))
        assertFalse(json.contains("pressReleaseUrls"))
        assertFalse(json.contains("Riot Games"))

        val type = object : TypeToken<List<Partner>>() {}.type
        val partners: List<Partner> = Gson().fromJson(json, type)

        assertTrue(partners.size >= 30)
        assertEquals(partners.size, partners.map { it.id }.toSet().size)
        assertTrue(partners.all { it.category in PartnerCategoryFilters.jsonCategories })

        val featured = partners.filter { it.featured }.map { it.name }.toSet()
        assertTrue(
            featured.containsAll(
                setOf(
                    "Create Now",
                    "TutorD",
                    "CSUN",
                    "The Laughing Otter",
                    "Glitch",
                    "Influencer",
                    "Red Apple Tech",
                    "INVO Tech",
                    "Korgi"
                )
            )
        )

        val robotCowboys = partners.first { it.id == "game_studio_robot_cowboys" }
        assertTrue(robotCowboys.additionalLinks.isNullOrEmpty())
        assertFalse(json.contains("Press Kit"))
        assertFalse(json.contains("notion.site"))

        val invo = partners.first { it.id == "technology_invo" }
        assertTrue(invo.featured)

        val korgi = partners.first { it.id == "technology_korgi" }
        assertTrue(korgi.featured)

        val tutord = partners.first { it.id == "community_tutord" }
        assertEquals(2, tutord.emails().size)
        assertTrue(tutord.linkList().any { it.url.contains("scholars.tutord.io") })

        val betterYouth = partners.first { it.id == "community_better_youth" }
        assertFalse(betterYouth.donationUrl.isNullOrBlank())

        val disney = partners.first { it.id == "corporate_disney_imagineering" }
        assertTrue(disney.contactList().any { !it.phone.isNullOrBlank() })

        val redApple = partners.first { it.id == "technology_red_apple_tech" }
        assertTrue(redApple.featured)
        assertEquals(2, redApple.emails().size)
        assertTrue(redApple.logo.isNullOrBlank())

        val filteredTech = PartnerCategoryFilters.filter(partners, "Technology")
        assertTrue(filteredTech.all { it.category == "Technology Partners" })
        assertTrue(filteredTech.first().featured) // featured sort first
    }
}
