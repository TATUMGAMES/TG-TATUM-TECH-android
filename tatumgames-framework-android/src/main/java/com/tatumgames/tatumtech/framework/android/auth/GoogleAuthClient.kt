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

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.ACCOUNT_REAUTH_FAILED
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.CANCELLED
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.CLEAR_CREDENTIAL_STATE_FAILED
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.CREDENTIAL_MANAGER_EXCEPTION
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.DELETE_FIREBASE_USER_FAILED
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.FIREBASE_INIT_ERROR
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.FIREBASE_SIGN_IN_FAILED
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.GOOGLE_AUTH_INITIALIZATION_FAILED
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.ID_TOKEN_PARSING_FAILED
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.NO_CREDENTIAL
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.TAG
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.UNKNOWN_ERROR
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClientMessages.UNKNOWN_FIREBASE_ERROR
import com.tatumgames.tatumtech.framework.android.auth.configuration.GoogleAuthConfiguration
import com.tatumgames.tatumtech.framework.android.auth.models.GoogleUser
import com.tatumgames.tatumtech.framework.android.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object GoogleAuthClientMessages {
    internal const val TAG = "GoogleAuthClient"

    internal const val GOOGLE_AUTH_INITIALIZATION_FAILED =
        "Failed to initialize Google authentication"
    internal const val UNKNOWN_ERROR = "Unknown error"
    internal const val CREDENTIAL_MANAGER_EXCEPTION = "CredentialManager failed"
    internal const val NO_CREDENTIAL =
        "No Google credential. If Google accounts exist on the device, check that this " +
            "package name + signing certificate SHA-1 is registered as an Android OAuth client " +
            "in the same Google Cloud project as the web client ID"
    internal const val CANCELLED =
        "Google sign-in cancelled. If an account was selected, Google may have rejected this " +
            "app's package name + signing certificate (look for an OAuth2 registration error " +
            "from the Auth tag)"
    internal const val ACCOUNT_REAUTH_FAILED = "Account reauth failed"
    internal const val ID_TOKEN_PARSING_FAILED = "Failed to parse Google ID token credential"
    internal const val FIREBASE_SIGN_IN_FAILED = "Firebase sign-in with Google credential failed"
    internal const val UNKNOWN_FIREBASE_ERROR = "Unknown Firebase authentication error"
    internal const val FIREBASE_INIT_ERROR = "Failed to initialize Firebase"
    internal const val DELETE_FIREBASE_USER_FAILED = "Failed to delete Firebase user"
    internal const val CLEAR_CREDENTIAL_STATE_FAILED = "Failed to clear credential state"
}

/**
 * Sign in with Google through AndroidX Credential Manager.
 *
 * Flow: Credential Manager shows Google's account sheet, returns a Google ID token for
 * [GoogleAuthConfiguration.webClientId] (the OAuth **web** client ID), the token signs in to
 * Firebase Auth, and the callback receives a [GoogleUser] carrying the same ID token so the app
 * can exchange it with its own backend.
 */
object GoogleAuthClient {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Starts Sign in with Google. Results arrive on the main thread through
     * [GoogleAuthConfiguration.callback]; a dismissed sheet reports [GoogleAuthError.Cancelled].
     */
    fun signIn(
        configuration: GoogleAuthConfiguration
    ) {
        try {
            FirebaseInitializer.initialize(configuration.context)
        } catch (e: Exception) {
            Logger.e(TAG, FIREBASE_INIT_ERROR, e)
            configuration.callback.onGoogleAuthFailure(GoogleAuthError.FirebaseInitializationFailed)
            return
        }

        coroutineScope.launch {
            try {
                val idToken = requestGoogleIdToken(configuration) ?: return@launch
                authenticateWithFirebase(configuration, idToken)
            } catch (e: Exception) {
                Logger.e(TAG, GOOGLE_AUTH_INITIALIZATION_FAILED, e)
                configuration.callback.onGoogleAuthFailure(
                    GoogleAuthError.InitializationError(e.message ?: UNKNOWN_ERROR)
                )
            }
        }
    }

    /**
     * Shows the Google account sheet and returns the ID token, or reports the failure through
     * the callback and returns null.
     */
    private suspend fun requestGoogleIdToken(configuration: GoogleAuthConfiguration): String? {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetSignInWithGoogleOption.Builder(configuration.webClientId).build()
            )
            .build()

        val credential = try {
            CredentialManager.create(configuration.activity)
                .getCredential(configuration.activity, request)
                .credential
        } catch (e: GetCredentialException) {
            configuration.callback.onGoogleAuthFailure(credentialFailure(e))
            return null
        }

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            Logger.e(TAG, "$CREDENTIAL_MANAGER_EXCEPTION: unexpected credential ${credential.type}")
            configuration.callback.onGoogleAuthFailure(
                GoogleAuthError.UnexpectedCredentialType(credential.type)
            )
            return null
        }

        val idToken = try {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } catch (e: GoogleIdTokenParsingException) {
            Logger.e(TAG, ID_TOKEN_PARSING_FAILED, e)
            configuration.callback.onGoogleAuthFailure(GoogleAuthError.InvalidCredential)
            return null
        }

        if (idToken.isBlank()) {
            configuration.callback.onGoogleAuthFailure(GoogleAuthError.NoIdToken)
            return null
        }
        return idToken
    }

    private fun credentialFailure(e: GetCredentialException): GoogleAuthError = when (e) {
        is GetCredentialCancellationException -> {
            Logger.w(TAG, "$CANCELLED: ${e.errorMessage}")
            // Google reports a rejected account (e.g. unregistered package/SHA-1) as a
            // cancellation after the user picked an account; only the message tells them apart.
            if (e.errorMessage?.contains(ACCOUNT_REAUTH_FAILED, ignoreCase = true) == true) {
                GoogleAuthError.CredentialManagerError(ACCOUNT_REAUTH_FAILED)
            } else {
                GoogleAuthError.Cancelled
            }
        }
        is NoCredentialException -> {
            Logger.e(TAG, NO_CREDENTIAL, e)
            GoogleAuthError.NoCredentialAvailable
        }
        is GetCredentialProviderConfigurationException, is GetCredentialUnsupportedException -> {
            Logger.e(TAG, CREDENTIAL_MANAGER_EXCEPTION, e)
            GoogleAuthError.ProviderUnavailable
        }
        else -> {
            Logger.e(TAG, "$CREDENTIAL_MANAGER_EXCEPTION (${e.type})", e)
            GoogleAuthError.CredentialManagerError(e.type)
        }
    }

    /**
     * Whether a Firebase user from a previous Google sign-in is still signed in on this device.
     * Firebase keeps that session until sign-out, account deletion, or revocation.
     */
    fun hasSignedInUser(context: Context): Boolean = try {
        FirebaseInitializer.initialize(context.applicationContext)
        FirebaseAuth.getInstance().currentUser != null
    } catch (e: Exception) {
        Logger.e(TAG, FIREBASE_INIT_ERROR, e)
        false
    }

    /**
     * Permanently removes the signed-in identity from this device.
     *
     * Deletes the Firebase Auth user when one is signed in, signs out of Firebase, and clears
     * Credential Manager state so the next sign-in shows the account picker again. Each step is
     * best-effort: a failure in one step (e.g. Firebase requiring a recent login) never
     * prevents the remaining steps.
     *
     * @param context Any context; the application context is used internally.
     * @return `true` when the remote Firebase user was deleted (or none was signed in).
     */
    suspend fun deleteAccount(context: Context): Boolean {
        val appContext = context.applicationContext
        var remoteDeleted = true

        try {
            FirebaseInitializer.initialize(appContext)
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            if (user != null) {
                remoteDeleted = awaitTask(user.delete())
                if (!remoteDeleted) {
                    Logger.e(TAG, DELETE_FIREBASE_USER_FAILED)
                }
            }
            auth.signOut()
        } catch (e: Exception) {
            remoteDeleted = false
            Logger.e(TAG, DELETE_FIREBASE_USER_FAILED, e)
        }

        try {
            CredentialManager.create(appContext)
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Logger.e(TAG, CLEAR_CREDENTIAL_STATE_FAILED, e)
        }

        return remoteDeleted
    }

    private suspend fun awaitTask(task: Task<*>): Boolean =
        suspendCancellableCoroutine { continuation ->
            task.addOnCompleteListener { completed ->
                if (continuation.isActive) {
                    continuation.resume(completed.isSuccessful)
                }
            }
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
                        displayName = firebaseUser?.displayName,
                        idToken = idToken
                    )
                    configuration.callback.onGoogleAuthSuccess(user)
                } else {
                    val exception = task.exception
                    if (exception != null) {
                        Logger.e(TAG, FIREBASE_SIGN_IN_FAILED, exception)
                    } else {
                        Logger.e(TAG, FIREBASE_SIGN_IN_FAILED)
                    }
                    configuration.callback.onGoogleAuthFailure(
                        if (exception is FirebaseNetworkException) {
                            GoogleAuthError.Network
                        } else {
                            GoogleAuthError.FirebaseError(exception?.message ?: UNKNOWN_FIREBASE_ERROR)
                        }
                    )
                }
            }
    }
}
