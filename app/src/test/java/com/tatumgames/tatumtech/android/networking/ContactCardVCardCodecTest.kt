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
import com.tatumgames.tatumtech.android.ui.components.screens.networking.models.ContactCardVCardCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactCardVCardCodecTest {

    private fun fullCard() = ContactCardEntity(
        cardId = "card-uuid-abc",
        ownerUserId = 1L,
        name = "Alex Rivera",
        jobTitle = "Game Designer",
        company = "Indie Studio",
        description = "Building community games.",
        email = "alex@studio.example",
        phone = "+1-555-0100",
        alternateEmail = "alex.alt@example.com",
        website = "https://example.com",
        linkedin = "https://www.linkedin.com/in/example",
        twitter = "https://x.com/example",
        customLink = "https://portfolio.example",
        calendly = "https://calendly.com/example"
    )

    @Test
    fun encode_minimalCard_omitsEmptyProperties() {
        val card = ContactCardEntity(
            cardId = "card-min",
            ownerUserId = 1L,
            name = "Sam Lee"
        )
        val vcard = ContactCardVCardCodec.encode(card)!!
        assertTrue(vcard.contains("BEGIN:VCARD"))
        assertTrue(vcard.contains("VERSION:3.0"))
        assertTrue(vcard.contains("FN:Sam Lee"))
        assertTrue(vcard.contains("UID:card-min"))
        assertFalse(vcard.contains("EMAIL:"))
        assertFalse(vcard.contains("TEL:"))
        assertFalse(vcard.contains("URL:"))
        assertFalse(vcard.contains("NOTE:"))
        assertTrue(vcard.endsWith("\r\n"))
    }

    @Test
    fun encode_usesProfileNamePartsForN() {
        val card = ContactCardEntity(
            cardId = "card-n",
            ownerUserId = 1L,
            name = "Alex Rivera"
        )
        val vcard = ContactCardVCardCodec.encode(card, firstName = "Alex", lastName = "Rivera")!!
        assertTrue(vcard.contains("N:Rivera;Alex;;;"))
    }

    @Test
    fun encode_escapesSpecialCharacters() {
        val card = ContactCardEntity(
            cardId = "card-esc",
            ownerUserId = 1L,
            name = "O'Brien, Jr.",
            company = "Games; Labs",
            description = "Line1\nLine2"
        )
        val vcard = ContactCardVCardCodec.encode(card)!!
        assertTrue(vcard.contains("FN:O'Brien\\, Jr."))
        assertTrue(vcard.contains("ORG:Games\\; Labs"))
        assertTrue(vcard.contains("NOTE:Line1\\nLine2"))
    }

    @Test
    fun encode_truncatesLongDescription() {
        val longDesc = "A".repeat(400)
        val card = ContactCardEntity(
            cardId = "card-note",
            ownerUserId = 1L,
            name = "Pat",
            description = longDesc
        )
        val vcard = ContactCardVCardCodec.encode(card)!!
        val payload = (ContactCardVCardCodec.parse(vcard) as ContactCardQrParseResult.Success).payload
        assertNotNull(payload.description)
        assertTrue(payload.description!!.length <= 280)
        assertTrue(payload.description!!.endsWith("…"))
    }

    @Test
    fun encode_returnsNullWithoutDisplayName() {
        val card = ContactCardEntity(
            cardId = "card-empty",
            ownerUserId = 1L,
            name = "   "
        )
        assertNull(ContactCardVCardCodec.encode(card))
    }

    @Test
    fun encodeParse_roundTripsFullCard() {
        val card = fullCard()
        val vcard = ContactCardVCardCodec.encode(
            card,
            firstName = "Alex",
            lastName = "Rivera"
        )!!
        val parsed = ContactCardVCardCodec.parse(vcard)
        assertTrue(parsed is ContactCardQrParseResult.Success)
        val payload = (parsed as ContactCardQrParseResult.Success).payload
        assertEquals("card-uuid-abc", payload.cardId)
        assertEquals("Alex Rivera", payload.name)
        assertEquals("Game Designer", payload.jobTitle)
        assertEquals("Indie Studio", payload.company)
        assertEquals("Building community games.", payload.description)
        assertEquals("alex@studio.example", payload.email)
        assertEquals("+1-555-0100", payload.phone)
        assertEquals("alex.alt@example.com", payload.alternateEmail)
        assertEquals("https://example.com", payload.website)
        assertEquals("https://www.linkedin.com/in/example", payload.linkedin)
        assertEquals("https://x.com/example", payload.twitter)
        assertEquals("https://portfolio.example", payload.customLink)
        assertEquals("https://calendly.com/example", payload.calendly)
        assertEquals(0L, payload.userId)
        assertNull(payload.anonymousId)
    }

    @Test
    fun encode_unicodeNameSurvivesRoundTrip() {
        val card = ContactCardEntity(
            cardId = "card-jp",
            ownerUserId = 1L,
            name = "田中 太郎"
        )
        val vcard = ContactCardVCardCodec.encode(card, firstName = "太郎", lastName = "田中")!!
        val payload = (ContactCardVCardCodec.parse(vcard) as ContactCardQrParseResult.Success).payload
        assertEquals("田中 太郎", payload.name)
    }

    @Test
    fun parseIncoming_routesVCardAndLegacyJson() {
        val card = ContactCardEntity(cardId = "c1", ownerUserId = 1L, name = "A")
        val vcard = ContactCardVCardCodec.encode(card)!!
        assertTrue(ContactCardQrCodec.parseIncoming(vcard) is ContactCardQrParseResult.Success)

        val legacy = ContactCardQrCodec.encode(
            ContactCardQrCodec.fromCard(card, anonymousId = "anon-1")
        )
        assertTrue(ContactCardQrCodec.parseIncoming(legacy) is ContactCardQrParseResult.Success)
    }

    @Test
    fun parseIncoming_rejectsGarbage() {
        assertEquals(ContactCardQrParseResult.Invalid, ContactCardQrCodec.parseIncoming(""))
        assertEquals(ContactCardQrParseResult.Invalid, ContactCardQrCodec.parseIncoming("hello"))
        assertEquals(ContactCardQrParseResult.Invalid, ContactCardQrCodec.parseIncoming("{not-json"))
        assertEquals(
            ContactCardQrParseResult.Invalid,
            ContactCardQrCodec.parseIncoming("BEGIN:VCARD\r\nVERSION:3.0\r\nEND:VCARD\r\n")
        )
    }

    @Test
    fun toConnection_nullsUnknownUserIdFromVCard() {
        val card = ContactCardEntity(cardId = "c-dup", ownerUserId = 1L, name = "Dup")
        val vcard = ContactCardVCardCodec.encode(card)!!
        val payload = (ContactCardVCardCodec.parse(vcard) as ContactCardQrParseResult.Success).payload
        val connection = ContactCardQrCodec.toConnection(ownerUserId = 9L, payload = payload)
        assertEquals("c-dup", connection.connectedCardId)
        assertNull(connection.connectedUserId)
    }

    @Test
    fun isOwnCard_matchesUidToLocalCardId() {
        val user = UserEntity(
            id = 1L,
            anonymousId = "anon-local",
            firstName = "Alex",
            lastName = "Rivera",
            name = "anon-local",
            email = null
        )
        val card = ContactCardEntity(cardId = "local-card", ownerUserId = 1L, name = "Alex Rivera")
        val vcard = ContactCardVCardCodec.encode(card, "Alex", "Rivera")!!
        val payload = (ContactCardVCardCodec.parse(vcard) as ContactCardQrParseResult.Success).payload
        assertTrue(ContactCardQrCodec.isOwnCard(payload, user, "local-card"))
        assertFalse(ContactCardQrCodec.isOwnCard(payload, user, "other-card"))
    }

    @Test
    fun syntheticCardId_isStableWithoutUid() {
        val raw = """
            BEGIN:VCARD
            VERSION:3.0
            FN:Stable Person
            EMAIL;TYPE=INTERNET:stable@example.com
            TEL;TYPE=CELL:555-0000
            END:VCARD
        """.trimIndent().replace("\n", "\r\n") + "\r\n"
        val a = (ContactCardVCardCodec.parse(raw) as ContactCardQrParseResult.Success).payload
        val b = (ContactCardVCardCodec.parse(raw) as ContactCardQrParseResult.Success).payload
        assertEquals(a.cardId, b.cardId)
        assertTrue(a.cardId.startsWith("vcard:"))
    }

    @Test
    fun escapeUnescape_roundTrip() {
        val original = "a\\b;c,d\ne"
        assertEquals(original, ContactCardVCardCodec.unescape(ContactCardVCardCodec.escape(original)))
    }

    @Test
    fun foldLines_breaksLongContent() {
        val long = "NOTE:" + "あ".repeat(80)
        val folded = ContactCardVCardCodec.foldLines(listOf(long))
        assertTrue(folded.size > 1)
        assertTrue(folded.drop(1).all { it.startsWith(" ") })
    }

    @Test
    fun encode_producesScannableDenseCard() {
        val vcard = ContactCardVCardCodec.encode(fullCard(), "Alex", "Rivera")
        assertNotNull(vcard)
        // Keep under a practical QR payload budget for ECC-M phone scans.
        assertTrue(vcard!!.toByteArray(Charsets.UTF_8).size < 1200)
    }
}
