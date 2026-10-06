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
package com.tatumgames.tatumtech.framework.android.http.logging

import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequest
import com.tatumgames.tatumtech.framework.android.http.executor.HttpResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class PrettyHttpTrafficLoggerTest {

    private val logged = mutableListOf<Pair<String, String>>()
    private val logger = PrettyHttpTrafficLogger(sink = { tag, message -> logged += tag to message })

    private fun output(tag: String) = logged.filter { it.first == tag }.joinToString("\n") { it.second }

    private val signIn = HttpRequest(
        method = HttpMethod.POST,
        url = "https://api.example.com/auth/signin",
        headers = mapOf("Accept" to "application/json", "x-api-key" to "key-1", "Authorization" to "Bearer t"),
        body = """{"email":"a@b.com","password":"hunter2","deviceId":"d1"}"""
    )

    @Test
    fun request_isLoggedUnderRqWithPrettyJsonAndMaskedCredentials() {
        logger.logRequest(signIn)

        val text = output("RQ")
        assertTrue(text.startsWith("--> POST https://api.example.com/auth/signin"))
        assertTrue(text.contains("Accept: application/json"))
        assertTrue(text.contains("x-api-key: ${PrettyHttpTrafficLogger.REDACTED}"))
        assertTrue(text.contains("Authorization: ${PrettyHttpTrafficLogger.REDACTED}"))
        assertTrue(text.contains("{\n  \"email\": \"a@b.com\",\n  \"password\": \"${PrettyHttpTrafficLogger.REDACTED}\""))
        assertFalse(text.contains("hunter2") || text.contains("key-1") || text.contains("Bearer t"))
        assertTrue(text.endsWith("--> END POST"))
        assertTrue(logged.none { it.first == "RS" })
    }

    @Test
    fun response_isLoggedUnderRsWithStatusDurationAndNestedTokensMasked() {
        val response = HttpResponse(
            statusCode = 200,
            url = "https://api.example.com/auth/signin",
            headers = mapOf("content-type" to "application/json"),
            body = """{"data":{"accessToken":"a1","refreshToken":"r1","tokenType":"Bearer","user":{"id":7,"bio":null}}}"""
        )

        logger.logResponse(signIn, response, durationMs = 42)

        val text = output("RS")
        assertTrue(text.startsWith("<-- 200 OK POST https://api.example.com/auth/signin (42 ms)"))
        assertTrue(text.contains("\"accessToken\": \"${PrettyHttpTrafficLogger.REDACTED}\""))
        assertTrue(text.contains("\"refreshToken\": \"${PrettyHttpTrafficLogger.REDACTED}\""))
        assertTrue(text.contains("\"tokenType\": \"Bearer\""))
        assertTrue(text.contains("\"bio\": null"))
        assertFalse(text.contains("\"a1\"") || text.contains("\"r1\""))
    }

    @Test
    fun getWithQuery_showsTheReadableUrl() {
        logger.logRequest(
            HttpRequest(HttpMethod.GET, "https://api.example.com/partners", queryParameters = mapOf("category" to "Game Studios"))
        )

        assertTrue(output("RQ").startsWith("--> GET https://api.example.com/partners?category=Game%20Studios\n"))
    }

    @Test
    fun nonJsonAndEmptyBodies_areLoggedAsIs() {
        val request = HttpRequest(HttpMethod.GET, "https://api.example.com/x")

        logger.logResponse(request, HttpResponse(502, "https://api.example.com/x", body = "Bad gateway"), 5)
        logger.logResponse(request, HttpResponse(204, "https://api.example.com/x", body = ""), 5)
        logger.logResponse(request, HttpResponse(200, "https://api.example.com/x", body = "{broken"), 5)

        val text = output("RS")
        assertTrue(text.contains("Bad gateway"))
        assertTrue(text.contains("(empty body)"))
        assertTrue(text.contains("{broken"))
    }

    @Test
    fun failure_isLoggedUnderRs() {
        logger.logFailure(signIn, IOException("timeout"), durationMs = 15_000)

        assertEquals(
            "<-- FAILED POST https://api.example.com/auth/signin (15000 ms): IOException: timeout",
            output("RS")
        )
    }

    @Test
    fun largeBodies_areChunkedForLogcatAndTruncated() {
        val items = (1..2_000).joinToString(",", "[", "]") { """{"id":$it,"name":"Item $it"}""" }
        val small = PrettyHttpTrafficLogger(maxBodyChars = 20_000, sink = { tag, message -> logged += tag to message })

        small.logResponse(signIn, HttpResponse(200, signIn.url, body = items), 1)

        assertTrue(logged.size > 1)
        assertTrue(logged.all { it.first == "RS" && it.second.length <= PrettyHttpTrafficLogger.MAX_CHUNK_CHARS })
        val text = output("RS")
        assertTrue(text.contains("more characters)"))
        assertTrue(text.endsWith("<-- END POST"))
    }

    @Test
    fun defaultSensitiveKeys() {
        listOf("password", "confirmPassword", "refreshToken", "googleIdToken", "verifyToken", "clientSecret", "apiKey")
            .forEach { assertTrue(it, PrettyHttpTrafficLogger.isDefaultSensitiveJsonKey(it)) }
        listOf("email", "tokenType", "deviceId", "tokenExpiration")
            .forEach { assertFalse(it, PrettyHttpTrafficLogger.isDefaultSensitiveJsonKey(it)) }
    }
}
