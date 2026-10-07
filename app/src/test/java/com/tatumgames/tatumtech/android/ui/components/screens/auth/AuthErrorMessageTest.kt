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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthErrorMessageTest {

    @Test
    fun googleCancellation_showsNothing() {
        assertNull(googleAuthErrorMessageRes(GoogleAuthError.Cancelled))
    }

    @Test
    fun googleFailures_mapToFriendlyCopy() {
        assertEquals(R.string.error_message_network, googleAuthErrorMessageRes(GoogleAuthError.Network))
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
}
