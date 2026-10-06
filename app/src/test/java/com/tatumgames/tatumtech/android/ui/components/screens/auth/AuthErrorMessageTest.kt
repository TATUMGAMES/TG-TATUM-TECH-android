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
package com.tatumgames.tatumtech.android.ui.components.screens.auth

import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthError
import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ErrorItem
import com.tatumgames.tatumtech.framework.android.http.response.HttpStatusCode
import com.tatumgames.tatumtech.framework.android.http.response.ResponseMetadata
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthErrorMessageTest {

    private val metadata = ResponseMetadata(HttpMethod.POST, "https://example.test/auth", "/auth", 10)

    private fun http(vararg errors: ErrorItem) =
        ApiError.Http(HttpStatusCode.fromCode(401), errors.toList(), null, metadata)

    private fun message(error: ApiError) =
        authErrorMessage(error, networkMessage = NETWORK, genericMessage = GENERIC)

    @Test
    fun http_showsFirstServerMessageTrimmed() {
        assertEquals(
            "Invalid email or password.",
            message(http(ErrorItem(code = "x"), ErrorItem(message = "  Invalid email or password.  ")))
        )
    }

    @Test
    fun http_withoutUsableMessage_fallsBackToGeneric() {
        assertEquals(GENERIC, message(http()))
        assertEquals(GENERIC, message(http(ErrorItem(message = "   "))))
    }

    @Test
    fun http_longMessage_isCapped() {
        val result = message(http(ErrorItem(message = "a".repeat(MAX_AUTH_ERROR_MESSAGE_LENGTH + 200))))
        assertEquals(MAX_AUTH_ERROR_MESSAGE_LENGTH + 1, result.length)
        assertTrue(result.endsWith("…"))
    }

    @Test
    fun network_usesNetworkCopy() {
        assertEquals(NETWORK, message(ApiError.Network(IOException("timeout at 10.0.0.1"), metadata)))
    }

    @Test
    fun serializationAndUnexpected_useGenericCopy() {
        val status = HttpStatusCode.fromCode(200)
        assertEquals(GENERIC, message(ApiError.Serialization(IllegalStateException("bad json"), status, "{}", metadata)))
        assertEquals(GENERIC, message(ApiError.Unexpected(IllegalArgumentException("no base url"), null)))
    }

    @Test
    fun googleCancellation_showsNothing() {
        assertNull(googleAuthErrorMessageRes(GoogleAuthError.Cancelled))
    }

    @Test
    fun googleFailures_mapToFriendlyCopy() {
        assertEquals(R.string.error_network_unavailable, googleAuthErrorMessageRes(GoogleAuthError.Network))
        assertEquals(
            R.string.google_sign_in_unavailable,
            googleAuthErrorMessageRes(GoogleAuthError.NoCredentialAvailable)
        )
        assertEquals(
            R.string.google_sign_in_unavailable,
            googleAuthErrorMessageRes(GoogleAuthError.ProviderUnavailable)
        )
        listOf(
            GoogleAuthError.InvalidCredential,
            GoogleAuthError.NoIdToken,
            GoogleAuthError.UnexpectedCredentialType("password"),
            GoogleAuthError.CredentialManagerError("TYPE_UNKNOWN"),
            GoogleAuthError.FirebaseError("INVALID_IDP_RESPONSE"),
            GoogleAuthError.FirebaseInitializationFailed
        ).forEach { assertEquals(R.string.google_sign_in_failed, googleAuthErrorMessageRes(it)) }
    }

    private companion object {
        const val NETWORK = "network"
        const val GENERIC = "generic"
    }
}
