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
package com.tatumgames.tatumtech.framework.android.http.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommonClientConfigurationTest {

    @Test
    fun builder_defaultsAreEmpty() {
        val config = CommonClientConfiguration.Builder().build()

        assertNull(config.apiKey)
        assertNull(config.jwtAccessToken)
        assertNull(config.baseUrl)
        assertNull(config.connectTimeout)
        assertNull(config.refreshToken)
        assertNull(config.tokenExpiration)
        assertFalse(config.debugMode)
    }

    @Test
    fun builder_setsEveryValue() {
        val config = CommonClientConfiguration.Builder()
            .setApiKey("key-123")
            .setJwtAccessToken("abc.def.ghi")
            .setBaseUrl("https://api.example.com")
            .setHttpClientTimeout(20_000L)
            .setRefreshToken("refresh-1")
            .setTokenExpiration(1_700_000_000_000L)
            .setDebugMode(true)
            .build()

        assertEquals("key-123", config.apiKey)
        assertEquals("Bearer abc.def.ghi", config.jwtAccessToken)
        assertEquals("https://api.example.com", config.baseUrl)
        assertEquals(20_000L, config.connectTimeout)
        assertEquals("refresh-1", config.refreshToken)
        assertEquals(1_700_000_000_000L, config.tokenExpiration)
        assertTrue(config.debugMode)
    }

    @Test
    fun builder_treatsBlankStringsAsAbsent() {
        val config = CommonClientConfiguration.Builder()
            .setApiKey("   ")
            .setJwtAccessToken("")
            .setBaseUrl(" ")
            .setRefreshToken("\t")
            .build()

        assertNull(config.apiKey)
        assertNull(config.jwtAccessToken)
        assertNull(config.baseUrl)
        assertNull(config.refreshToken)
    }

    @Test
    fun builder_trimsValues() {
        val config = CommonClientConfiguration.Builder()
            .setApiKey("  key  ")
            .setRefreshToken(" refresh ")
            .build()

        assertEquals("key", config.apiKey)
        assertEquals("refresh", config.refreshToken)
    }

    @Test
    fun baseUrl_trailingSlashesAreRemoved() {
        val config = CommonClientConfiguration.Builder()
            .setBaseUrl("https://api.example.com/v1///")
            .build()

        assertEquals("https://api.example.com/v1", config.baseUrl)
    }

    @Test
    fun baseUrl_httpIsAccepted() {
        val config = CommonClientConfiguration.Builder().setBaseUrl("http://10.0.2.2:8080").build()

        assertEquals("http://10.0.2.2:8080", config.baseUrl)
    }

    @Test(expected = IllegalArgumentException::class)
    fun baseUrl_missingSchemeIsRejected() {
        CommonClientConfiguration.Builder().setBaseUrl("api.example.com")
    }

    @Test(expected = IllegalArgumentException::class)
    fun baseUrl_nonHttpSchemeIsRejected() {
        CommonClientConfiguration.Builder().setBaseUrl("ftp://api.example.com")
    }

    @Test(expected = IllegalArgumentException::class)
    fun baseUrl_queryIsRejected() {
        CommonClientConfiguration.Builder().setBaseUrl("https://api.example.com?env=stage")
    }

    @Test
    fun timeout_nullRestoresDefault() {
        val config = CommonClientConfiguration.Builder()
            .setHttpClientTimeout(5_000L)
            .setHttpClientTimeout(null)
            .build()

        assertNull(config.connectTimeout)
    }

    @Test(expected = IllegalArgumentException::class)
    fun timeout_zeroIsRejected() {
        CommonClientConfiguration.Builder().setHttpClientTimeout(0L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun timeout_negativeIsRejected() {
        CommonClientConfiguration.Builder().setHttpClientTimeout(-1L)
    }

    @Test
    fun from_copiesConfigurationForRebuilding() {
        val original = CommonClientConfiguration.Builder()
            .setApiKey("key")
            .setBaseUrl("https://api.example.com")
            .setHttpClientTimeout(9_000L)
            .setDebugMode(true)
            .build()

        val updated = CommonClientConfiguration.Builder()
            .from(original)
            .setJwtAccessToken("new-token")
            .build()

        assertEquals("key", updated.apiKey)
        assertEquals("https://api.example.com", updated.baseUrl)
        assertEquals(9_000L, updated.connectTimeout)
        assertTrue(updated.debugMode)
        assertEquals("Bearer new-token", updated.jwtAccessToken)
    }

    @Test
    fun toString_redactsSecrets() {
        val text = CommonClientConfiguration.Builder()
            .setApiKey("secret-key")
            .setJwtAccessToken("secret-token")
            .setRefreshToken("secret-refresh")
            .setBaseUrl("https://api.example.com")
            .build()
            .toString()

        assertFalse(text.contains("secret"))
        assertTrue(text.contains("https://api.example.com"))
    }

    @Test
    fun subclassBuilder_keepsItsOwnTypeThroughChaining() {
        val config: AppConfiguration = AppConfiguration.Builder()
            .setBaseUrl("https://app.example.com")
            .setRegion("eu")
            .setApiKey("k")
            .build()

        assertEquals("eu", config.region)
        assertEquals("https://app.example.com", config.baseUrl)
        assertEquals("k", config.apiKey)
    }

    private class AppConfiguration private constructor(
        values: Values,
        val region: String
    ) : CommonClientConfiguration(values) {

        class Builder : ConfigurationBuilder<Builder, AppConfiguration>() {
            private var region = "us"

            fun setRegion(region: String): Builder = apply { this.region = region }

            override fun self(): Builder = this

            override fun build(): AppConfiguration = AppConfiguration(values(), region)
        }
    }
}
