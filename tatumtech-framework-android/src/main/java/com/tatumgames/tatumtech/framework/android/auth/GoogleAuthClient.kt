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

import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.CREDENTIAL_MANAGER_EXCEPTION
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.CREDENTIAL_MANAGER_FAILED
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.FIREBASE_INIT_ERROR
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.GOOGLE_AUTH_INITIALIZATION_FAILED
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.LEGACY_RESULT_ERROR
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.LEGACY_SIGN_IN_ERROR
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.TAG
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.UNKNOWN_ERROR
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.UNKNOWN_FIREBASE_ERROR
import com.tatumgames.tatumtech.framework.android.auth.configuration.GoogleAuthConfiguration
import com.tatumgames.tatumtech.framework.android.auth.models.GoogleUser
import com.tatumgames.tatumtech.framework.android.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object GoogleAuthClientMessages {
    internal const val TAG = "GoogleAuthClient"

    internal const val CREDENTIAL_MANAGER_FAILED =
        "CredentialManager failed, falling back to legacy"
    internal const val GOOGLE_AUTH_INITIALIZATION_FAILED =
        "Failed to initialize Google authentication"
    internal const val UNKNOWN_ERROR = "Unknown error"
    internal const val LEGACY_SIGN_IN_ERROR = "Legacy sign-in error"
    internal const val LEGACY_RESULT_ERROR = "Failed to handle legacy result"
    internal const val CREDENTIAL_MANAGER_EXCEPTION = "CredentialManager failed"
    internal const val UNKNOWN_FIREBASE_ERROR = "Unknown Firebase authentication error"
    internal const val FIREBASE_INIT_ERROR = "Failed to initialize Firebase"
}

/**
 * Main client for Google Authentication operations.
 * Contains all sign-in and result-handling logic, using configuration objects for parameters.
 */
object GoogleAuthClient {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    /**
     * Initiates Google Sign-In with the provided configuration.
     * This is the main entry point for Google authentication.
     *
     * @param configuration The configuration object containing all required parameters
     */
    fun signIn(
        configuration: GoogleAuthConfiguration
    ) {
        try {
            // Ensure Firebase is initialized
            try {
                FirebaseInitializer.initialize(configuration.context)
            } catch (e: Exception) {
                configuration.callback.onGoogleAuthFailure(
                    GoogleAuthError.FirebaseError("${FIREBASE_INIT_ERROR}: ${e.message}")
                )
                return
            }

            coroutineScope.launch {
                try {
                    // Try CredentialManager first (modern approach)
                    attemptCredentialManagerSignIn(configuration)
                } catch (e: Exception) {
                    Logger.e(TAG, CREDENTIAL_MANAGER_FAILED, e)
                    // Fallback to legacy GoogleSignInClient
                    attemptLegacySignIn(configuration)
                }
            }

        } catch (e: Exception) {
            Logger.e(TAG, GOOGLE_AUTH_INITIALIZATION_FAILED, e)
            configuration.callback.onGoogleAuthFailure(
                GoogleAuthError.InitializationError(e.message ?: UNKNOWN_ERROR)
            )
        }
    }

    /**
     * Handles the result from legacy Google Sign-In.
     * This should be called from the UI layer when the legacy launcher returns a result.
     *
     * @param configuration The configuration object containing all required parameters
     * @param data The intent data from the legacy launcher result
     */
    fun handleLegacyResult(
        configuration: GoogleAuthConfiguration,
        data: android.content.Intent?
    ) {
        try {
            if (data == null) {
                configuration.callback.onGoogleAuthFailure(GoogleAuthError.NoDataReceived)
                return
            }

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account =
                    task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                val idToken = account?.idToken

                if (idToken != null) {
                    authenticateWithFirebase(configuration, idToken)
                } else {
                    configuration.callback.onGoogleAuthFailure(GoogleAuthError.IdTokenNull)
                }
            } catch (e: Exception) {
                Logger.e(TAG, LEGACY_SIGN_IN_ERROR, e)
                configuration.callback.onGoogleAuthFailure(GoogleAuthError.GoogleSignInFailed)
            }
        } catch (e: Exception) {
            Logger.e(TAG, LEGACY_RESULT_ERROR, e)
            configuration.callback.onGoogleAuthFailure(
                GoogleAuthError.LegacyResultError(e.message ?: UNKNOWN_ERROR)
            )
        }
    }

    /**
     * Attempts authentication using CredentialManager (modern approach).
     */
    private suspend fun attemptCredentialManagerSignIn(
        configuration: GoogleAuthConfiguration
    ) {
        val credentialManager = CredentialManager.create(configuration.context)

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(configuration.webClientId)
                    .build()
            ).build()

        try {
            val result = credentialManager.getCredential(configuration.activity, request)
            val credential =
                result.credential as? com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
            val idToken = credential?.idToken

            if (idToken.isNullOrEmpty()) {
                configuration.callback.onGoogleAuthFailure(GoogleAuthError.NoIdToken)
                return
            }

            authenticateWithFirebase(configuration, idToken)

        } catch (e: NoCredentialException) {
            // No credential available, fallback to legacy
            throw e
        } catch (e: Exception) {
            Logger.e(TAG, CREDENTIAL_MANAGER_EXCEPTION, e)
            configuration.callback.onGoogleAuthFailure(
                GoogleAuthError.CredentialManagerError(
                    e.localizedMessage ?: CREDENTIAL_MANAGER_EXCEPTION
                )
            )
        }
    }

    /**
     * Attempts authentication using legacy GoogleSignInClient.
     */
    private fun attemptLegacySignIn(
        configuration: GoogleAuthConfiguration
    ) {
        val legacyLauncher = configuration.legacyLauncher
        if (legacyLauncher == null) {
            configuration.callback.onGoogleAuthFailure(GoogleAuthError.LegacyLauncherNotConfigured)
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(configuration.webClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(configuration.context, gso)
        legacyLauncher.launch(googleSignInClient.signInIntent)
    }

    /**
     * Authenticates with Firebase using the provided ID token.
     */
    private fun authenticateWithFirebase(
        configuration: GoogleAuthConfiguration,
        idToken: String
    ) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    val user = GoogleUser(
                        email = firebaseUser?.email,
                        uid = firebaseUser?.uid,
                        photoUrl = firebaseUser?.photoUrl?.toString(),
                        isAnonymous = firebaseUser?.isAnonymous ?: false,
                        displayName = firebaseUser?.displayName
                    )
                    configuration.callback.onGoogleAuthSuccess(user)
                } else {
                    val errorMessage =
                        task.exception?.message ?: UNKNOWN_FIREBASE_ERROR
                    configuration.callback.onGoogleAuthFailure(
                        GoogleAuthError.FirebaseError(
                            errorMessage
                        )
                    )
                }
            }
    }
}
