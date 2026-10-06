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

        assertEquals(5, apps.size)
        assertEquals(apps.size, apps.map { it.appId }.toSet().size)
        assertEquals(
            setOf(
                "Price of Glory",
                "Heroes Vs Villains: Nemesis",
                "BANJAX",
                "Tales of Encenia",
                "Saints Art Puzzle"
            ),
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

    @Test
    fun gamesJson_banjaxAndTalesOfEnceniaLinksAndMedia() {
        val apps = loadApps()

        val banjax = apps.first { it.title == "BANJAX" }
        assertEquals("MugginsVR", banjax.companyName)
        assertTrue(GameMediaResolver.isComingSoon(banjax))
        assertEquals("https://mugginsvr.com/", banjax.website)
        assertEquals("drawable:banjax_logo_icon", banjax.campaign?.images?.appLogo)
        assertEquals(6, banjax.images.screenshots.size)
        val banjaxVideo = banjax.videos.promotional.single()
        assertEquals("https://www.youtube.com/watch?v=8Nv-PpXhG00", banjaxVideo.url)
        assertEquals("https://img.youtube.com/vi/8Nv-PpXhG00/hqdefault.jpg", banjaxVideo.thumbnailUrl)
        assertTrue(banjax.videos.others.isEmpty())
        assertEquals(listOf(banjaxVideo.url), banjax.campaign?.videoUrls)
        val banjaxCtas = banjax.campaign!!.ctas
        assertEquals("https://store.steampowered.com/app/4565160/BANJAX/", banjaxCtas.steamStore)
        assertEquals("https://www.meta.com/experiences/banjax/32964033859910934/", banjaxCtas.other)
        assertEquals("https://mugginsvr.com/", banjaxCtas.website)
        val banjaxSocial = banjax.campaign!!.socialMedia
        assertEquals("https://www.linkedin.com/in/barliesque/", banjaxSocial.linkedin)
        assertEquals("https://www.instagram.com/mugginsvr", banjaxSocial.instagram)
        assertEquals("https://www.youtube.com/@muggins-vr", banjaxSocial.youtube)

        val toe = apps.first { it.title == "Tales of Encenia" }
        assertEquals("Grey State Development", toe.companyName)
        assertTrue(GameMediaResolver.isComingSoon(toe))
        assertEquals("drawable:talesofencenia_logo_icon", toe.campaign?.images?.appLogo)
        assertEquals(7, toe.images.screenshots.size)
        assertEquals(
            "https://www.youtube.com/watch?v=3f76jiOZET8",
            toe.videos.promotional.single().url
        )
        assertFalse(toe.videos.promotional.single().thumbnailUrl.isNullOrBlank())
        assertEquals("https://talesofencenia.com/", toe.campaign?.ctas?.website)
        val toeSocial = toe.campaign!!.socialMedia
        assertEquals("https://www.youtube.com/@TalesOfEncenia", toeSocial.youtube)
        assertEquals("https://www.facebook.com/TalesOfEncenia", toeSocial.facebook)
        assertEquals("https://www.instagram.com/talesofencenia/", toeSocial.instagram)
        assertEquals("https://www.tiktok.com/@talesofencenia", toeSocial.tiktok)
    }

    @Test
    fun gamesJson_saintsArtPuzzleLinksAndMedia() {
        val apps = loadApps()
        val game = apps.first { it.title == "Saints Art Puzzle" }

        assertEquals("game_0005", game.appId)
        assertEquals("Art of Devotion Games", game.companyName)
        assertTrue(GameMediaResolver.isAvailable(game))
        assertEquals("drawable:saint_art_puzzle_logo", game.campaign?.images?.appLogo)
        assertEquals((1..6).map { "drawable:saint_art_puzzle_ss_0$it" }, game.images.screenshots)
        assertEquals(game.images.screenshots, game.campaign?.screenshotUrls)
        assertTrue(game.videos.promotional.isEmpty() && game.videos.others.isEmpty())
        assertTrue(game.campaign!!.videoUrls.isEmpty())
        assertEquals("https://www.artofdevotiongames.com/", game.website)
        val ctas = game.campaign!!.ctas
        assertEquals(
            "https://play.google.com/store/apps/details?id=com.artofdevotiongames.saintsartpuzzle",
            ctas.googleStore
        )
        assertEquals("https://apps.apple.com/us/app/saints-art-puzzle/id6759738917", ctas.appleStore)
        assertEquals("https://www.artofdevotiongames.com/", ctas.website)
        assertEquals("https://linktr.ee/artofdevotiongames", ctas.other)
        val social = game.campaign!!.socialMedia
        assertEquals("https://www.instagram.com/artofdevotiongames/", social.instagram)
        assertEquals("https://www.tiktok.com/@art.of.devotion8", social.tiktok)
        assertEquals(
            listOf(social.facebook, social.x, social.linkedin, social.youtube, social.discord, social.twitch),
            List(6) { null }
        )
        // Catalog order: appId sequence with descending featured priority and popularity.
        assertEquals(game, apps.last())
        assertTrue(apps.zipWithNext().all { (a, b) -> a.featuredPriority > b.featuredPriority })
        assertTrue(apps.zipWithNext().all { (a, b) -> a.popularityScore > b.popularityScore })
    }

    @Test
    fun gamesJson_localMediaReferencesExist() {
        val json = File("src/main/assets/games.json").readText()
        val refs = Regex("\"drawable:([a-z0-9_]+)\"").findAll(json).toList()
        assertTrue(refs.isNotEmpty())
        val drawables = File("src/main/res/drawable").listFiles().orEmpty()
        refs.forEach { match ->
            val name = match.groupValues[1]
            assertTrue(
                "Missing drawable resource: $name",
                drawables.any { it.nameWithoutExtension == name }
            )
        }
    }

    @Test
    fun gamesJson_videosAreRemoteLinksWithThumbnails() {
        val json = File("src/main/assets/games.json").readText()
        assertFalse(json.contains("\"raw:"))

        loadApps().flatMap { it.videos.promotional + it.videos.others }.forEach { video ->
            assertTrue("Video URL must be https: ${video.url}", video.url.startsWith("https://"))
            assertFalse("Missing thumbnail for ${video.url}", video.thumbnailUrl.isNullOrBlank())
        }
    }

    private fun loadApps() = Gson().fromJson(
        File("src/main/assets/games.json").readText(),
        GamesCatalogResponse::class.java
    ).data.apps
}
