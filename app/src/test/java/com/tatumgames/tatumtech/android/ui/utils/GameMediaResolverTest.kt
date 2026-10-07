package com.tatumgames.tatumtech.android.ui.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameMediaResolverTest {

    private val lookups = mutableListOf<String>()
    private val drawables = mapOf("partner_logo_betteryouth" to 42)

    private fun resolve(logo: String?) = GameMediaResolver.resolvePartnerLogo(logo) { name ->
        lookups += name
        drawables[name] ?: 0
    }

    @Test
    fun `api png logo url is passed to the image loader unchanged`() {
        val url = "https://storage.googleapis.com/tg-api-new-stage.appspot.com/uploads/tatum_tech_partners_logo/betteryouth.png"

        assertEquals(url, resolve(url))
        assertTrue("A remote URL must never be looked up as a drawable", lookups.isEmpty())
    }

    @Test
    fun `api jpg logo url is passed to the image loader unchanged`() {
        val url = "https://storage.googleapis.com/tg-api-new-stage.appspot.com/uploads/tatum_tech_partners_logo/community_creative_media_commerce.jpg"

        assertEquals(url, resolve(url))
    }

    @Test
    fun `url scheme is matched case-insensitively`() {
        assertEquals("HTTPS://example.com/logo.png", resolve("HTTPS://example.com/logo.png"))
    }

    @Test
    fun `bundled logo names still resolve to drawables`() {
        assertEquals(42, resolve("partner_logo_betteryouth"))
        assertEquals(42, resolve("drawable:partner_logo_betteryouth"))
    }

    @Test
    fun `missing or unknown logos resolve to nothing so the placeholder is shown`() {
        assertNull(resolve(null))
        assertNull(resolve(""))
        assertNull(resolve("   "))
        assertNull(resolve("partner_logo_missing"))
    }
}
