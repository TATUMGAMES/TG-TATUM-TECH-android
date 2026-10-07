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
import java.net.SocketTimeoutException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiErrorLoggerTest {

    private val metadata = ResponseMetadata(
        HttpMethod.POST,
        "https://tg-api-new-stage.uc.r.appspot.com/tatum-tech/signup",
        "tatum-tech/signup",
        42
    )

    @Test
    fun `envelope error lists method, path, both statuses, code, and environment`() {
        val error = ApiError.Http(
            HttpStatusCode.fromCode(406),
            listOf(ErrorItem(code = "PASSWORDS_DO_NOT_MATCH")),
            """{"status":{"statusCode":406,"statusMessage":"PASSWORDS_DO_NOT_MATCH"}}""",
            metadata,
            responseStatusCode = 200
        )

        val log = ApiErrorLogger.describe(error, "STAGE")

        assertTrue(log, log.contains("Environment: STAGE"))
        assertTrue(log, log.contains("Method: POST"))
        assertTrue(log, log.contains("Endpoint: /tatum-tech/signup"))
        assertTrue(log, log.contains("HTTP Status: 200"))
        assertTrue(log, log.contains("API Status: 406"))
        assertTrue(log, log.contains("Server Code: PASSWORDS_DO_NOT_MATCH"))
        assertTrue(log, log.contains("Error Type: BAD_REQUEST"))
        assertTrue(log, log.contains("Request ID: not provided by the server"))
    }

    @Test
    fun `credentials in response bodies are masked`() {
        val body = """{"status":{"statusCode":201},"data":{"email":"a@b.test","password":"hunter22",
            |"confirmPassword":"hunter22","accessToken":"eyJaccess","refreshToken":"eyJrefresh",
            |"idToken":"google-id","nested":[{"clientSecret":"s3"}]}}""".trimMargin()
        val error = ApiError.Serialization(IllegalStateException("missing field"), HttpStatusCode.fromCode(201), body, metadata)

        val log = ApiErrorLogger.describe(error, "STAGE")

        listOf("hunter22", "eyJaccess", "eyJrefresh", "google-id", "s3\"").forEach {
            assertFalse("leaked $it in $log", log.contains(it))
        }
        assertTrue(log, log.contains(ApiErrorLogger.REDACTED))
        assertTrue(log, log.contains("a@b.test"))
    }

    @Test
    fun `html pages are summarized instead of dumped`() {
        val html = "<html><head><title>404 Page Not Found</title></head><body>${"x".repeat(5_000)}</body></html>"

        val summary = ApiErrorLogger.summarizeBody(html)

        assertTrue(summary, summary.startsWith("HTML page \"404 Page Not Found\""))
        assertTrue(summary, summary.length < 100)
    }

    @Test
    fun `long bodies are truncated`() {
        val summary = ApiErrorLogger.summarizeBody("e".repeat(ApiErrorLogger.MAX_BODY_CHARS * 3))
        assertTrue(summary.length < ApiErrorLogger.MAX_BODY_CHARS + 40)
    }

    @Test
    fun `transport failures name the exception and have no status`() {
        val log = ApiErrorLogger.describe(ApiError.Network(SocketTimeoutException("timeout"), metadata), "STAGE")

        assertTrue(log, log.contains("HTTP Status: none (no response)"))
        assertTrue(log, log.contains("Error Type: TIMEOUT"))
        assertTrue(log, log.contains("Exception: java.net.SocketTimeoutException: timeout"))
    }

    @Test
    fun `sensitive keys`() {
        listOf("password", "confirmPassword", "accessToken", "refresh_token", "idToken", "apiKey", "Authorization", "clientSecret")
            .forEach { assertTrue(it, ApiErrorLogger.isSensitiveKey(it)) }
        listOf("email", "deviceId", "statusMessage").forEach { assertFalse(it, ApiErrorLogger.isSensitiveKey(it)) }
    }
}
