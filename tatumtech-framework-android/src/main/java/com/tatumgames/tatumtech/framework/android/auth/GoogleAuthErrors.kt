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
 * Centralized error handling for Google Authentication operations.
 * Provides a clean, type-safe way to handle authentication errors.
 */
sealed class GoogleAuthError(val message: String) {
    object NoIdToken : GoogleAuthError("No ID token received.")
    object IdTokenNull : GoogleAuthError("ID token is null")
    object LegacyLauncherNotConfigured : GoogleAuthError("Legacy launcher not configured.")
    object NoDataReceived : GoogleAuthError("No data received from legacy sign-in")
    object GoogleSignInFailed : GoogleAuthError("Google sign-in failed.")
    object FirebaseInitializationFailed : GoogleAuthError("Failed to initialize Firebase.")

    data class FirebaseError(val cause: String) : GoogleAuthError(cause)
    data class InitializationError(val cause: String) :
        GoogleAuthError("Failed to initialize authentication: $cause")

    data class LegacyResultError(val cause: String) :
        GoogleAuthError("Failed to handle legacy result: $cause")

    data class CredentialManagerError(val cause: String) : GoogleAuthError(cause)
}
