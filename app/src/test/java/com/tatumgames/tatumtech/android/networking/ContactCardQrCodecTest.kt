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
package com.tatumgames.tatumtech.android.networking

import com.tatumgames.tatumtech.android.database.entity.ContactCardEntity
import com.tatumgames.tatumtech.android.database.entity.UserEntity
import com.tatumgames.tatumtech.android.ui.components.screens.networking.models.ContactCardQrCodec
import com.tatumgames.tatumtech.android.ui.components.screens.networking.models.ContactCardQrParseResult
import com.tatumgames.tatumtech.android.ui.components.screens.networking.models.ContactCardQrPayload
import com.tatumgames.tatumtech.android.ui.components.screens.stats.AchievementPointsColors
import com.tatumgames.tatumtech.android.ui.theme.FallDeepOrange500
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.ui.theme.SuccessGreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactCardQrCodecTest {

    @Test
    fun encodeAndParse_roundTripsValidPayload() {
        val card = ContactCardEntity(
            cardId = "card-123",
            ownerUserId = 1L,
            name = "Kevin Lim",
            jobTitle = "Engineer",
            company = "Tatum Games",
            email = "kevin@example.com",
            phone = "+1-555-0100",
            website = "https://example.com"
        )
        val encoded = ContactCardQrCodec.encode(
            ContactCardQrCodec.fromCard(card, anonymousId = "anon-kevin")
        )
        val parsed = ContactCardQrCodec.parse(encoded)

        assertTrue(parsed is ContactCardQrParseResult.Success)
        val payload = (parsed as ContactCardQrParseResult.Success).payload
        assertEquals("card-123", payload.cardId)
        assertEquals("Kevin Lim", payload.name)
        assertEquals("Engineer", payload.jobTitle)
        assertEquals("+1-555-0100", payload.phone)
        assertEquals("anon-kevin", payload.anonymousId)
        assertEquals(ContactCardQrPayload.TYPE, payload.type)
        assertEquals(ContactCardQrPayload.CURRENT_VERSION, payload.version)
    }

    @Test
    fun parse_rejectsMalformedJson() {
        assertEquals(ContactCardQrParseResult.Invalid, ContactCardQrCodec.parse("{not-json"))
        assertEquals(ContactCardQrParseResult.Invalid, ContactCardQrCodec.parse(""))
    }

    @Test
    fun parse_rejectsWrongType() {
        val raw = """{"type":"other","version":1,"cardId":"c1","userId":1,"name":"A"}"""
        assertEquals(ContactCardQrParseResult.Invalid, ContactCardQrCodec.parse(raw))
    }

    @Test
    fun parse_rejectsUnsupportedVersion() {
        val raw =
            """{"type":"tatum_tech_contact","version":99,"cardId":"c1","userId":1,"name":"A"}"""
        val result = ContactCardQrCodec.parse(raw)
        assertTrue(result is ContactCardQrParseResult.UnsupportedVersion)
        assertEquals(99, (result as ContactCardQrParseResult.UnsupportedVersion).version)
    }

    @Test
    fun blankToNull_trimsEmpty() {
        assertEquals(null, ContactCardQrCodec.blankToNull("   "))
        assertEquals("ok", ContactCardQrCodec.blankToNull(" ok "))
    }

    @Test
    fun toConnection_mapsStableIdsAndPhone() {
        val payload = ContactCardQrPayload(
            cardId = "card-abc",
            userId = 42L,
            anonymousId = "anon-david",
            name = "David Ashe",
            email = "david@example.com",
            phone = "555-1212"
        )
        val connection = ContactCardQrCodec.toConnection(ownerUserId = 1L, payload = payload)
        assertEquals(1L, connection.ownerUserId)
        assertEquals("card-abc", connection.connectedCardId)
        assertEquals(42L, connection.connectedUserId)
        assertEquals("David Ashe", connection.name)
        assertEquals("555-1212", connection.phone)
    }

    @Test
    fun isOwnCard_usesAnonymousIdNotLocalRoomUserId() {
        val currentUser = UserEntity(
            id = 1L,
            anonymousId = "anon-local",
            firstName = null,
            lastName = null,
            name = "Local",
            email = null
        )
        val otherPayload = ContactCardQrPayload(
            cardId = "other-card",
            userId = 1L, // same Room id as local — must NOT count as own
            anonymousId = "anon-other",
            name = "Other Person"
        )
        assertFalse(ContactCardQrCodec.isOwnCard(otherPayload, currentUser, "local-card"))

        val ownPayload = ContactCardQrPayload(
            cardId = "local-card",
            userId = 1L,
            anonymousId = "anon-local",
            name = "Local"
        )
        assertTrue(ContactCardQrCodec.isOwnCard(ownPayload, currentUser, "local-card"))
    }

    @Test
    fun isOwnCard_fallsBackToCardIdWhenAnonymousMissing() {
        val currentUser = UserEntity(
            id = 1L,
            anonymousId = "anon-local",
            firstName = null,
            lastName = null,
            name = "Local",
            email = null
        )
        val legacyOther = ContactCardQrPayload(
            cardId = "other-card",
            userId = 1L,
            anonymousId = null,
            name = "Other"
        )
        assertFalse(ContactCardQrCodec.isOwnCard(legacyOther, currentUser, "local-card"))

        val legacyOwn = ContactCardQrPayload(
            cardId = "local-card",
            userId = 1L,
            anonymousId = null,
            name = "Local"
        )
        assertTrue(ContactCardQrCodec.isOwnCard(legacyOwn, currentUser, "local-card"))
    }

    @Test
    fun achievementPointsColors_areDeterministicByTier() {
        assertEquals(SuccessGreen, AchievementPointsColors.badgeColor(10, unlocked = true))
        assertEquals(Purple500, AchievementPointsColors.badgeColor(15, unlocked = true))
        assertEquals(FallDeepOrange500, AchievementPointsColors.badgeColor(25, unlocked = true))
    }
}
