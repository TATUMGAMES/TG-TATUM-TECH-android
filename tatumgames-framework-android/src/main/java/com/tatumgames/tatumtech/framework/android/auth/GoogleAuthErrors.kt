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
package com.tatumgames.tatumtech.framework.android.auth

/**
 * Why Google sign-in did not complete. [message] is for logs only; apps map the type to
 * user-facing copy.
 */
sealed class GoogleAuthError(val message: String) {
    /** The user dismissed the Google account sheet. Not an error worth surfacing. */
    object Cancelled : GoogleAuthError("Google sign-in cancelled by the user.")

    /**
     * Credential Manager found no usable Google credential. Besides "no account", Google also
     * reports this when the app's package name + signing certificate are not registered as an
     * Android OAuth client in the same project as the web client ID.
     */
    object NoCredentialAvailable : GoogleAuthError("No Google credential available.")

    /** Credential Manager or Google Play services is missing or misconfigured on the device. */
    object ProviderUnavailable : GoogleAuthError("Credential provider unavailable.")

    /** The returned credential was not a Google ID token credential. */
    data class UnexpectedCredentialType(val type: String) :
        GoogleAuthError("Unexpected credential type: $type")

    /** The Google ID token credential could not be parsed. */
    object InvalidCredential : GoogleAuthError("Invalid Google ID token credential.")

    object NoIdToken : GoogleAuthError("No ID token received.")
    object FirebaseInitializationFailed : GoogleAuthError("Failed to initialize Firebase.")
    object Network : GoogleAuthError("Network error during Google sign-in.")

    data class FirebaseError(val cause: String) : GoogleAuthError(cause)
    data class InitializationError(val cause: String) :
        GoogleAuthError("Failed to initialize authentication: $cause")

    data class CredentialManagerError(val cause: String) : GoogleAuthError(cause)
}
