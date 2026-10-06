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
import com.tatumgames.tatumtech.android.ui.models.PartnerSocialLinks
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
                    "TicToc Games",
                    "Glitch",
                    "Influencer",
                    "Red Apple Tech",
                    "INVO Tech",
                    "Korgi",
                    "Da Rib Crib",
                    "NOI - Nation of Islam"
                )
            )
        )

        val tictoc = partners.first { it.id == "game_studio_tictoc_games" }
        assertTrue(tictoc.featured)
        assertEquals("Game Studio Partners", tictoc.category)
        assertEquals("partner_logo_tictoc_games", tictoc.logo)
        assertEquals("https://tictocgames.com/", tictoc.websiteUrl)
        assertTrue(tictoc.contactList().any { it.email == "scott.prather@tictocgames.com" })
        assertEquals("https://discord.com/invite/w9sPbPt26H", tictoc.socialLinks?.discord)
        assertEquals("https://www.instagram.com/tictocgames/", tictoc.socialLinks?.instagram)
        assertEquals("https://x.com/tictocgames", tictoc.socialLinks?.x)
        assertTrue(tictoc.description!!.contains("Game Development Partner"))
        assertTrue(partners.none { it.description.isNullOrBlank() })
        assertTrue(
            partners.all {
                it.description!!.contains("Partner in the Tatum Games ecosystem")
            }
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

        val daRibCrib = partners.first { it.id == "community_da_rib_crib" }
        assertEquals("partner_logo_da_rib_crib", daRibCrib.logo)
        assertEquals("https://daribcrib.com/", daRibCrib.websiteUrl)
        assertTrue(daRibCrib.contactList().any { it.email == "jonearlgreen@gmail.com" })
        assertEquals("https://www.instagram.com/daribcrib", daRibCrib.socialLinks?.instagram)

        val noi = partners.first { it.id == "community_noi" }
        assertEquals("partner_logo_noi", noi.logo)
        assertEquals("https://www.noilosangeles.org/", noi.websiteUrl)

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

    @Test
    fun partnersJson_creativeMediaCommerceUsesTheSharedPartnerFields() {
        val json = File("src/main/assets/partners.json").readText()
        val partners: List<Partner> = Gson().fromJson(json, object : TypeToken<List<Partner>>() {}.type)

        val partner = partners.single { it.id == "community_creative_media_commerce" }
        assertEquals("Creative Media Commerce", partner.name)
        assertEquals("Community Partners", partner.category)
        assertEquals("partner_logo_creative_media_commerce", partner.logo)
        assertEquals("https://www.itsaquestnotatest.com/", partner.websiteUrl)
        assertEquals(
            PartnerSocialLinks(
                instagram = "https://www.instagram.com/itsaquestnotatest/",
                tiktok = "https://www.tiktok.com/@itsaquestnotatest"
            ),
            partner.socialLinks
        )
        assertEquals(
            listOf("contact@itsaquestnotatest.com", "creativemediacommerce@gmail.com"),
            partner.emails().map { it.email }
        )
        val founder = partner.contactList().first()
        assertEquals("David Ashe", founder.name)
        assertEquals("Founder & CEO", founder.title)
        assertTrue(partner in PartnerCategoryFilters.filter(partners, "Community"))
        val domainAddresses = Regex("[\\w.+-]+@itsaquestnotatest\\.com", RegexOption.IGNORE_CASE)
            .findAll(json).map { it.value.lowercase() }.toSet()
        assertEquals(setOf("contact@itsaquestnotatest.com"), domainAddresses)
    }

    @Test
    fun partnersJson_globalBusinessIncubationAndAthleticInterpretations() {
        val json = File("src/main/assets/partners.json").readText()
        val partners: List<Partner> = Gson().fromJson(json, object : TypeToken<List<Partner>>() {}.type)

        val gbi = partners.single { it.id == "community_global_business_incubation" }
        assertEquals("Global Business Incubation", gbi.name)
        assertEquals("Community Partners", gbi.category)
        assertEquals("gbi_logo", gbi.logo)
        assertFalse(gbi.featured)
        assertEquals("http://www.gbiinc.org/", gbi.websiteUrl)
        assertEquals(
            listOf("philbrown2020@yahoo.com", "gbi@globalbusinessincubation.com"),
            gbi.emails().map { it.email }
        )
        assertEquals("Phillip Brown", gbi.contactList().first().name)
        assertEquals("Executive Director", gbi.contactList().first().title)

        val ai = partners.single { it.id == "technology_athletic_interpretations" }
        assertEquals("Athletic Interpretations Inc.", ai.name)
        assertEquals("Technology Partners", ai.category)
        assertEquals("ai_logo", ai.logo)
        assertFalse(ai.featured)
        assertEquals("https://athleticinterpretations.com", ai.websiteUrl)
        assertEquals("Speedbag Champ", ai.productName)
        assertEquals("https://speedbagchamp.com", ai.productUrl)
        val founder = ai.contactList().single()
        assertEquals("Eras Noel III", founder.name)
        assertEquals("Founder", founder.title)
        assertEquals("eras.noel@athleticinterpretations.com", founder.email)
        assertEquals(
            listOf("Shop", "Starter Bundle", "LED Edition", "iOS App", "Android App"),
            ai.linkList().map { it.label }
        )
        assertEquals(
            PartnerSocialLinks(
                tiktok = "https://tiktok.com/@speedbagchamp",
                instagram = "https://instagram.com/speedbagchamp",
                youtube = "https://www.youtube.com/@SpeedbagChamp"
            ),
            ai.socialLinks
        )
        assertTrue(ai.description!!.contains("U.S. Patent 9,937,402"))
        assertTrue(ai in PartnerCategoryFilters.filter(partners, "Technology"))
        assertTrue(gbi in PartnerCategoryFilters.filter(partners, "Community"))
    }

    @Test
    fun partnersJson_logosReferenceExistingDrawables() {
        val json = File("src/main/assets/partners.json").readText()
        val partners: List<Partner> = Gson().fromJson(json, object : TypeToken<List<Partner>>() {}.type)
        val drawables = File("src/main/res/drawable").listFiles().orEmpty().map { it.nameWithoutExtension }.toSet()

        val missing = partners.mapNotNull { it.logo?.takeIf { logo -> logo.isNotBlank() } } - drawables
        assertTrue("Missing logo drawables: $missing", missing.isEmpty())
    }
}
