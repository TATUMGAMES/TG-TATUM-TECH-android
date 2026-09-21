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
import com.tatumgames.tatumtech.android.ui.models.GamesCatalogResponse
import com.tatumgames.tatumtech.android.ui.utils.GameMediaResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ShowcaseGamesJsonValidationTest {

    @Test
    fun gamesJson_containsOnlyCuratedShowcaseTitles() {
        val json = File("src/main/assets/games.json").readText()
        val response = Gson().fromJson(json, GamesCatalogResponse::class.java)
        val apps = response.data.apps

        assertEquals(2, apps.size)
        assertEquals(
            setOf("Price of Glory", "Heroes Vs Villains: Nemesis"),
            apps.map { it.title }.toSet()
        )

        val pog = apps.first { it.title == "Price of Glory" }
        val hvn = apps.first { it.title == "Heroes Vs Villains: Nemesis" }

        assertEquals("Marauder Tech Games", pog.companyName)
        assertFalse(pog.companyName == "Tatum Games")
        assertTrue(GameMediaResolver.isAvailable(pog))
        assertTrue(GameMediaResolver.isComingSoon(hvn))
        assertEquals(6, pog.images.screenshots.size)
        assertEquals(4, hvn.images.screenshots.size)
        assertEquals(3, pog.videos.promotional.size)
        assertTrue(pog.videos.promotional.all { it.url.contains("tg-api-new.uc.r.appspot.com") })
        assertTrue(pog.videos.promotional.all { !it.thumbnailUrl.isNullOrBlank() })
        assertTrue(pog.campaign?.ctas?.googleStore?.contains("appspot") == true)
        assertTrue(pog.campaign?.ctas?.appleStore?.contains("appspot") == true)
        assertTrue(pog.campaign?.socialMedia?.discord?.contains("appspot") == true)
        assertTrue(hvn.campaign?.ctas?.googleStore.isNullOrBlank())
        assertTrue(hvn.campaign?.ctas?.appleStore.isNullOrBlank())
        assertTrue(pog.images.screenshots.all { it.startsWith("drawable:") })
        assertTrue(hvn.images.screenshots.all { it.startsWith("drawable:") })
        assertFalse(json.contains("picsum.photos"))
        assertFalse(json.contains("Fortnite"))
    }
}
