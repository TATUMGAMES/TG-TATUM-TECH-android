package com.tatumgames.tatumtech.android.ui.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameMediaResolverTest {

    private val lookups = mutableListOf<String>()
    private val drawables = mapOf(
        "partner_logo_betteryouth" to 1,
        "speaker_jeff_bogensberger" to 2,
        "tatum_tech_placeholder_flyer_01" to 3,
        "pog_logo" to 4
    )

    private fun resolve(ref: String?) = GameMediaResolver.resolve(ref) { name ->
        lookups += name
        drawables[name] ?: 0
    }

    private fun assertPassedThrough(url: String) {
        assertEquals(url, resolve(url))
        assertTrue("A URL must never be looked up as a drawable", lookups.isEmpty())
    }

    @Test
    fun `api partner logo png url is passed to the image loader unchanged`() {
        assertPassedThrough("https://storage.googleapis.com/tg-api-new-stage.appspot.com/uploads/tatum_tech_partners_logo/betteryouth.png")
    }

    @Test
    fun `api partner logo jpg url is passed to the image loader unchanged`() {
        assertPassedThrough("https://storage.googleapis.com/tg-api-new-stage.appspot.com/uploads/tatum_tech_partners_logo/gbi_logo.jpg")
    }

    @Test
    fun `api speaker profile image url is passed to the image loader unchanged`() {
        assertPassedThrough("https://storage.googleapis.com/tg-api-new-stage.appspot.com/uploads/tatum_tech_speakers/speaker.png")
    }

    @Test
    fun `other uri schemes are passed through unchanged`() {
        assertPassedThrough("http://example.com/flyer.jpg")
        assertPassedThrough("HTTPS://example.com/logo.png")
        assertPassedThrough("android.resource://com.tatumgames.tatumtech.android/drawable/flyer")
    }

    @Test
    fun `surrounding whitespace is trimmed from urls`() {
        assertEquals("https://example.com/a.png", resolve("  https://example.com/a.png \n"))
    }

    @Test
    fun `bundled names resolve to drawables with or without the drawable prefix`() {
        assertEquals(1, resolve("partner_logo_betteryouth"))
        assertEquals(2, resolve("speaker_jeff_bogensberger"))
        assertEquals(3, resolve("tatum_tech_placeholder_flyer_01"))
        assertEquals(4, resolve("drawable:pog_logo"))
        assertEquals(1, resolve("drawable:partner_logo_betteryouth"))
    }

    @Test
    fun `blank or unknown references resolve to nothing so the placeholder is shown`() {
        assertNull(resolve(null))
        assertNull(resolve(""))
        assertNull(resolve("   "))
        assertNull(resolve("drawable:"))
        assertNull(resolve("partner_logo_missing"))
        assertNull(resolve("drawable:speaker_missing"))
    }
}
