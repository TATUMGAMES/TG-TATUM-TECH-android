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
package com.tatumgames.tatumtech.android.ui.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamesCatalogSectionsTest {

    @Test
    fun sections_useSubscriptionReleaseAndGameplayFields() {
        val enterprise = sampleGame(
            id = "e1",
            subscription = "Enterprise",
            releaseStatus = "Released",
            gameplayType = "Non-Casual"
        )
        val startup = sampleGame(
            id = "s1",
            subscription = "Startup",
            releaseStatus = "Released",
            gameplayType = "Casual"
        )
        val freeCasual = sampleGame(
            id = "fc1",
            subscription = "Free",
            releaseStatus = "Released",
            gameplayType = "Casual"
        )
        val freeCore = sampleGame(
            id = "fn1",
            subscription = "Free",
            releaseStatus = "Released",
            gameplayType = "Non-Casual"
        )
        val inDev = sampleGame(
            id = "d1",
            subscription = "Free",
            releaseStatus = "Coming Soon",
            gameplayType = "Casual"
        )
        val games = listOf(enterprise, startup, freeCasual, freeCore, inDev)

        assertEquals(listOf(enterprise), GamesCatalogSections.justTooFun(games))
        assertEquals(listOf(startup), GamesCatalogSections.tatumGamesFavorites(games))
        assertEquals(listOf(inDev), GamesCatalogSections.appsInDevelopment(games))
        assertEquals(listOf(freeCasual, inDev), GamesCatalogSections.casualGamer(games))
        assertEquals(listOf(freeCore), GamesCatalogSections.coreGamer(games))
    }

    @Test
    fun sections_areEmptyWhenFieldsDoNotMatch() {
        val unknown = sampleGame(
            id = "u1",
            subscription = "Partner",
            releaseStatus = "Released",
            gameplayType = null
        )
        assertTrue(GamesCatalogSections.justTooFun(listOf(unknown)).isEmpty())
        assertTrue(GamesCatalogSections.casualGamer(listOf(unknown)).isEmpty())
        assertTrue(GamesCatalogSections.appsInDevelopment(listOf(unknown)).isEmpty())
    }

    private fun sampleGame(
        id: String,
        subscription: String,
        releaseStatus: String,
        gameplayType: String?
    ): GameModel {
        return GameModel(
            appId = id,
            appName = id,
            companyName = "Test",
            title = id,
            shortDesc = "desc",
            fullDesc = "full",
            androidPackageName = null,
            iOSBundleId = null,
            website = null,
            appCategory = "Action",
            appStore = "Android",
            userSubscriptionType = subscription,
            releaseStatus = releaseStatus,
            images = GameImages(featureGraphics = emptyList(), screenshots = emptyList()),
            videos = GameVideos(promotional = emptyList(), others = emptyList()),
            gameplayType = gameplayType
        )
    }
}
