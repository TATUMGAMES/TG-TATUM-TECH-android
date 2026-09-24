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
package com.tatumgames.tatumtech.android.ui.components.screens.notifications

import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.entity.RecentNotificationEntity
import com.tatumgames.tatumtech.android.enums.NotificationType
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentNotificationPolicyTest {

    @Test
    fun codingChallengeDailyId_isStableAndContentKeyed() {
        assertEquals(
            "coding_challenge:daily:2026-09-22",
            RecentNotificationPolicy.codingChallengeDailyId("2026-09-22")
        )
    }

    @Test
    fun eventId_usesNumericContentId() {
        assertEquals("event:42", RecentNotificationPolicy.eventId(42))
    }

    @Test
    fun shouldRetain_keepsUnread() {
        val entity = sample(readAtMillis = null, createdAt = 0L)
        assertTrue(RecentNotificationPolicy.shouldRetain(entity, nowMillis = 1_000_000L))
    }

    @Test
    fun shouldRetain_keepsRecentRead() {
        val now = 1_000_000_000L
        val entity = sample(readAtMillis = now - dayMs(3), createdAt = now - dayMs(4))
        assertTrue(RecentNotificationPolicy.shouldRetain(entity, now))
    }

    @Test
    fun shouldRetain_dropsOldRead() {
        val now = 1_000_000_000L
        val entity = sample(readAtMillis = now - dayMs(15), createdAt = now - dayMs(20))
        assertFalse(RecentNotificationPolicy.shouldRetain(entity, now))
    }

    @Test
    fun notificationType_mapsToExpectedRoutes() {
        assertEquals(
            NavRoutes.CODING_CHALLENGES_SCREEN,
            NotificationType.CODING_CHALLENGE.destinationRoute
        )
        assertEquals(NavRoutes.UPCOMING_EVENTS_SCREEN, NotificationType.EVENT.destinationRoute)
        assertEquals(NavRoutes.CAREER_SCREEN, NotificationType.CAREER.destinationRoute)
        assertEquals(NavRoutes.COMMUNITY_SCREEN, NotificationType.COMMUNITY.destinationRoute)
        assertEquals(NavRoutes.GAMES_SCREEN, NotificationType.GAME.destinationRoute)
    }

    @Test
    fun destinationRoute_prefersPersistedRoute() {
        val entity = sample(readAtMillis = null, createdAt = 0L).copy(
            destinationRoute = NavRoutes.LEET_CODE_CHALLENGES_SCREEN
        )
        assertEquals(
            NavRoutes.LEET_CODE_CHALLENGES_SCREEN,
            RecentNotificationPolicy.destinationRoute(entity)
        )
    }

    private fun dayMs(days: Int): Long = days * 24L * 60L * 60L * 1000L

    private fun sample(readAtMillis: Long?, createdAt: Long) = RecentNotificationEntity(
        id = "coding_challenge:daily:2026-09-22",
        type = NotificationType.CODING_CHALLENGE.name,
        title = "Coding Challenge",
        description = "New coding challenge available",
        iconResId = R.drawable.notif_coding_challenge,
        createdAtMillis = createdAt,
        readAtMillis = readAtMillis,
        destinationRoute = NotificationType.CODING_CHALLENGE.destinationRoute
    )
}
