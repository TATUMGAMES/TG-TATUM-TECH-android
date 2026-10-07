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
package com.tatumgames.tatumtech.android.ui.components.common

import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ErrorItem
import com.tatumgames.tatumtech.framework.android.http.response.HttpStatusCode
import com.tatumgames.tatumtech.framework.android.http.response.ResponseMetadata
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiErrorPresentationTest {

    private val metadata = ResponseMetadata(HttpMethod.POST, "https://example.test/tatum-tech/signup", "tatum-tech/signup", 10)

    private fun http(status: Int, vararg errors: ErrorItem) =
        ApiError.Http(HttpStatusCode.fromCode(status), errors.toList(), null, metadata)

    private fun signUp(error: ApiError) = presentApiError(error, ApiOperation.SIGN_UP)

    @Test
    fun `existing account explains itself under the sign up title`() {
        val presentation = signUp(http(400, ErrorItem(code = "USER_ALREADY_EXISTS")))

        assertEquals(R.string.error_title_sign_up, presentation.title)
        assertEquals(R.string.error_message_account_exists, presentation.message)
        assertNull(presentation.serverMessage)
        assertFalse(presentation.canRetry)
    }

    @Test
    fun `validation codes use the app's own copy`() {
        assertEquals(R.string.error_message_invalid_email, signUp(http(405, ErrorItem(code = "INVALID_EMAIL_FORMAT"))).message)
        assertEquals(R.string.error_passwords_do_not_match, signUp(http(406, ErrorItem(code = "PASSWORDS_DO_NOT_MATCH"))).message)
        assertEquals(
            R.string.error_message_wrong_email_or_password,
            presentApiError(http(414, ErrorItem(code = "WRONG_EMAIL_OR_PASSWORD")), ApiOperation.SIGN_IN).message
        )
    }

    @Test
    fun `each status category has its own message`() {
        assertEquals(R.string.error_message_bad_request, signUp(http(400)).message)
        assertEquals(R.string.error_message_credentials_rejected, signUp(http(401)).message)
        assertEquals(
            R.string.error_message_session_expired,
            presentApiError(http(401), ApiOperation.LOAD_CONTENT).message
        )
        assertEquals(R.string.error_message_forbidden, signUp(http(403)).message)
        assertEquals(R.string.error_message_service, signUp(http(404)).message)
        assertEquals(R.string.error_message_conflict, signUp(http(409)).message)
        assertEquals(R.string.error_message_rate_limited, signUp(http(429)).message)
        assertEquals(R.string.error_message_server, signUp(http(503)).message)
        assertEquals(R.string.error_message_network, signUp(ApiError.Network(IOException("reset"), metadata)).message)
        assertEquals(R.string.error_message_timeout, signUp(ApiError.Network(SocketTimeoutException("timeout"), metadata)).message)
        assertEquals(
            R.string.error_message_service,
            signUp(ApiError.Unexpected(IllegalStateException("boom"), null)).message
        )
    }

    @Test
    fun `service and connectivity failures use the generic title`() {
        listOf(
            http(404),
            http(500),
            ApiError.Network(IOException("reset"), metadata),
            ApiError.Serialization(IllegalStateException("bad"), HttpStatusCode.fromCode(200), "<html>", metadata)
        ).forEach { assertEquals(R.string.error_title_generic, signUp(it).title) }

        listOf(http(400), http(401), http(403), http(409)).forEach {
            assertEquals(R.string.error_title_sign_up, signUp(it).title)
        }
    }

    @Test
    fun `only transient failures offer try again`() {
        assertTrue(signUp(ApiError.Network(IOException("reset"), metadata)).canRetry)
        assertTrue(signUp(ApiError.Network(SocketTimeoutException("timeout"), metadata)).canRetry)
        assertTrue(signUp(http(429)).canRetry)
        assertTrue(signUp(http(500)).canRetry)
        listOf(400, 401, 403, 404, 409).forEach { assertFalse("$it", signUp(http(it)).canRetry) }
    }

    @Test
    fun `raw exceptions and technical server text never reach the UI`() {
        val technical = listOf(
            http(500, ErrorItem(message = "java.lang.NullPointerException at com.example.Foo.bar(Foo.kt:12)")),
            http(500, ErrorItem(message = "<h1>A PHP Error was encountered</h1>")),
            ApiError.Serialization(IllegalStateException("Expected BEGIN_OBJECT at line 1"), HttpStatusCode.fromCode(200), "x", metadata),
            ApiError.Unexpected(IllegalArgumentException("No base URL configured"), null),
            ApiError.Network(IOException("failed to connect to /10.0.2.2 (port 443)"), metadata)
        )
        technical.forEach { assertNull(signUp(it).serverMessage) }
    }

    @Test
    fun `safe server sentences are passed through`() {
        val presentation = signUp(http(400, ErrorItem(message = "Sign ups are paused for maintenance.")))
        assertEquals("Sign ups are paused for maintenance.", presentation.serverMessage)
    }

    @Test
    fun `error titles are title case`() {
        val strings = File("src/main/res/values/strings.xml").readText()
        val titles = Regex("<string name=\"(error_title_[a-z_]+|try_again)\">(.*?)</string>").findAll(strings).toList()
        assertTrue(titles.isNotEmpty())
        val minorWords = setOf("a", "an", "and", "the", "to", "of", "in", "on", "for", "or", "your")
        titles.forEach { match ->
            val (name, text) = match.destructured
            text.split(' ').forEachIndexed { index, word ->
                val capitalized = word.first().isUpperCase()
                assertTrue("$name: '$text'", capitalized || (index > 0 && word.lowercase() in minorWords))
            }
        }
    }
}
