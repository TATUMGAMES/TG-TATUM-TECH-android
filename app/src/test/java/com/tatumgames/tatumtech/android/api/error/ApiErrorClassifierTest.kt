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
package com.tatumgames.tatumtech.android.api.error

import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ErrorItem
import com.tatumgames.tatumtech.framework.android.http.response.HttpStatusCode
import com.tatumgames.tatumtech.framework.android.http.response.ResponseMetadata
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiErrorClassifierTest {

    private val metadata = ResponseMetadata(HttpMethod.POST, "https://example.test/tatum-tech/signup", "tatum-tech/signup", 10)

    private fun http(status: Int, vararg errors: ErrorItem) =
        ApiError.Http(HttpStatusCode.fromCode(status), errors.toList(), null, metadata)

    private fun kindOf(error: ApiError) = ApiErrorClassifier.classify(error).kind

    @Test
    fun `http statuses map to their categories`() {
        assertEquals(ApiErrorKind.BAD_REQUEST, kindOf(http(400)))
        assertEquals(ApiErrorKind.UNAUTHORIZED, kindOf(http(401)))
        assertEquals(ApiErrorKind.FORBIDDEN, kindOf(http(403)))
        assertEquals(ApiErrorKind.NOT_FOUND, kindOf(http(404)))
        assertEquals(ApiErrorKind.CONFLICT, kindOf(http(409)))
        assertEquals(ApiErrorKind.RATE_LIMITED, kindOf(http(429)))
        assertEquals(ApiErrorKind.BAD_REQUEST, kindOf(http(422)))
        assertEquals(ApiErrorKind.TIMEOUT, kindOf(http(408)))
        listOf(500, 502, 503, 504).forEach { assertEquals(ApiErrorKind.SERVER_ERROR, kindOf(http(it))) }
    }

    @Test
    fun `transport failures distinguish timeouts from no connection`() {
        assertEquals(ApiErrorKind.TIMEOUT, kindOf(ApiError.Network(SocketTimeoutException("timeout"), metadata)))
        assertEquals(
            ApiErrorKind.NETWORK_UNAVAILABLE,
            kindOf(ApiError.Network(UnknownHostException("Unable to resolve host"), metadata))
        )
        assertEquals(ApiErrorKind.NETWORK_UNAVAILABLE, kindOf(ApiError.Network(IOException("reset"), metadata)))
    }

    @Test
    fun `malformed responses and unexpected failures`() {
        val malformed = ApiError.Serialization(IllegalStateException("bad json"), HttpStatusCode.fromCode(200), "<html>", metadata)
        assertEquals(ApiErrorKind.INVALID_RESPONSE, kindOf(malformed))
        assertEquals(ApiErrorKind.UNKNOWN, kindOf(ApiError.Unexpected(IllegalArgumentException("no base url"), null)))
    }

    @Test
    fun `known server codes override the status category`() {
        val exists = ApiErrorClassifier.classify(http(400, ErrorItem(code = "USER_ALREADY_EXISTS")))
        assertEquals(ApiErrorKind.CONFLICT, exists.kind)
        assertEquals(TatumTechServerCode.USER_ALREADY_EXISTS, exists.knownServerCode)
        assertNull(exists.userMessage)

        assertEquals(ApiErrorKind.BAD_REQUEST, kindOf(http(406, ErrorItem(message = "PASSWORDS_DO_NOT_MATCH"))))
        assertEquals(ApiErrorKind.UNAUTHORIZED, kindOf(http(414, ErrorItem(message = "WRONG_EMAIL_OR_PASSWORD"))))
        assertEquals(ApiErrorKind.UNAUTHORIZED, kindOf(http(419, ErrorItem(message = "REFRESH_TOKEN_DOES_NOT_EXIST"))))
    }

    @Test
    fun `unknown server codes fall back to the status`() {
        val classified = ApiErrorClassifier.classify(http(418, ErrorItem(code = "SOMETHING_NEW")))
        assertEquals(ApiErrorKind.BAD_REQUEST, classified.kind)
        assertEquals("SOMETHING_NEW", classified.serverCode)
        assertNull(classified.knownServerCode)
    }

    @Test
    fun `only safe server sentences become user messages`() {
        val safe = ApiErrorClassifier.classify(http(400, ErrorItem(message = "  Please choose a different email.  ")))
        assertEquals("Please choose a different email.", safe.userMessage)

        val technical = listOf(
            "java.lang.NullPointerException at com.example.Foo.bar(Foo.kt:12)",
            "SQLSTATE[23000]: Integrity constraint violation",
            "<html><body>Error</body></html>",
            "See https://internal.example.com/logs for details",
            "Connection refused to 10.0.0.12 on port 5432",
            "Undefined index: password in line 42",
            "a".repeat(SafeServerMessage.MAX_LENGTH + 1) + " b"
        )
        technical.forEach { message ->
            assertNull(message, ApiErrorClassifier.classify(http(500, ErrorItem(message = message))).userMessage)
        }
    }

    @Test
    fun `transient categories allow retry`() {
        listOf(ApiErrorKind.NETWORK_UNAVAILABLE, ApiErrorKind.TIMEOUT, ApiErrorKind.RATE_LIMITED, ApiErrorKind.SERVER_ERROR)
            .forEach { assertTrue(it.name, it.isTransient) }
        listOf(
            ApiErrorKind.BAD_REQUEST, ApiErrorKind.UNAUTHORIZED, ApiErrorKind.FORBIDDEN, ApiErrorKind.NOT_FOUND,
            ApiErrorKind.CONFLICT, ApiErrorKind.INVALID_RESPONSE, ApiErrorKind.UNKNOWN
        ).forEach { assertFalse(it.name, it.isTransient) }
    }

    @Test
    fun `machine codes are recognized`() {
        assertTrue(ApiErrorClassifier.isMachineCode("USER_ALREADY_EXISTS"))
        assertTrue(ApiErrorClassifier.isMachineCode("UNAUTHORIZED"))
        assertFalse(ApiErrorClassifier.isMachineCode("User already exists"))
        assertFalse(ApiErrorClassifier.isMachineCode("OK"))
    }
}
