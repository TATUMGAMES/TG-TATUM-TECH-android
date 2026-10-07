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
package com.tatumgames.tatumtech.android.api.session

import com.tatumgames.tatumtech.android.api.TatumTechApiClient
import com.tatumgames.tatumtech.android.api.TatumTechClientConfiguration
import com.tatumgames.tatumtech.android.api.models.TatumTechAuthSession
import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ApiResponse
import com.tatumgames.tatumtech.framework.android.http.response.EmptyStateInfo
import com.tatumgames.tatumtech.framework.android.http.response.HttpStatusCode
import com.tatumgames.tatumtech.framework.android.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

/**
 * Owns the Tatum Tech sign-in state: signs in, persists the session, keeps the shared
 * [TatumTechApiClient]'s tokens in sync, refreshes the access token, and signs out.
 *
 * Session lifetime: the access token is short-lived (the API issues 24-hour tokens) and is
 * refreshed with the refresh token shortly before it expires, so an active user stays signed in
 * indefinitely, like a Firebase/Google session. The server decides how long a refresh token
 * stays valid; once it rejects a refresh, the session is cleared and the next launch shows the
 * sign-in flow. Network failures never sign the user out.
 *
 * All token refreshing goes through [refreshIfNeeded]; authenticated calls use [authenticated],
 * which refreshes first and retries once after a 401.
 */
class TatumTechSessionManager(
    private val clientProvider: () -> TatumTechApiClient,
    private val store: TatumTechSessionStore,
    private val clock: () -> Long = System::currentTimeMillis,
    private val backgroundScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private val refreshMutex = Mutex()

    @Volatile
    private var session: TatumTechSession? = null

    val currentSession: TatumTechSession? get() = session

    val isSignedIn: Boolean get() = session != null

    /** Loads the stored session into memory and the client. Call once at startup. */
    fun restore() {
        val stored = store.load()
        session = stored
        applyToClient(stored)
    }

    // region Sign-in

    suspend fun signIn(email: String, password: String): ApiResponse<TatumTechSession> =
        start(TatumTechAuthMethod.EMAIL) { signIn(email, password, store.deviceId()) }

    suspend fun signUp(email: String, password: String, confirmPassword: String): ApiResponse<TatumTechSession> =
        start(TatumTechAuthMethod.EMAIL) { signUp(email, password, confirmPassword, store.deviceId()) }

    suspend fun signInWithGoogle(googleIdToken: String): ApiResponse<TatumTechSession> =
        start(TatumTechAuthMethod.GOOGLE) { signInWithGoogle(googleIdToken, store.deviceId()) }

    suspend fun forgotPassword(email: String): ApiResponse<EmptyStateInfo> =
        clientProvider().forgotPassword(email)

    private suspend fun start(
        method: TatumTechAuthMethod,
        call: suspend TatumTechApiClient.() -> ApiResponse<TatumTechAuthSession>
    ): ApiResponse<TatumTechSession> {
        val response = clientProvider().call()
        val issued = response.toSession(method, previous = null)
        if (issued is ApiResponse.Success) {
            refreshMutex.withLock { persist(issued.data) }?.let { return it }
        }
        return issued
    }

    // endregion

    // region Refresh

    /**
     * Refreshes the access token if it expires within [REFRESH_WINDOW_MS] (or [force] is set).
     * Concurrent callers share one refresh.
     */
    suspend fun refreshIfNeeded(force: Boolean = false): RefreshResult = refreshMutex.withLock {
        val current = session ?: return@withLock RefreshResult.NoSession
        if (!force && !isNearExpiry(current)) return@withLock RefreshResult.NotNeeded
        val refreshToken = current.refreshToken ?: return@withLock RefreshResult.NotNeeded

        val response = clientProvider().refreshToken(refreshToken, store.deviceId(), current.accessToken)
        when (val refreshed = response.toSession(current.authMethod, previous = current)) {
            is ApiResponse.Success -> persist(refreshed.data)
                ?.let { RefreshResult.Failed((it as ApiResponse.Failure).error) }
                ?: RefreshResult.Refreshed

            is ApiResponse.Failure -> if (isRejection(refreshed.error)) {
                Logger.w(TAG, SESSION_REJECTED)
                clearLocked()
                RefreshResult.SignedOut
            } else {
                RefreshResult.Failed(refreshed.error)
            }
        }
    }

    /** Starts [refreshIfNeeded] without waiting, e.g. at app launch. */
    fun refreshInBackground() {
        if (session == null) return
        backgroundScope.launch { refreshIfNeeded() }
    }

    /**
     * Runs an authenticated [call] with a fresh access token, refreshing and retrying once if the
     * server answers 401.
     */
    suspend fun <T> authenticated(call: suspend TatumTechApiClient.() -> ApiResponse<T>): ApiResponse<T> {
        refreshIfNeeded()
        val first = clientProvider().call()
        val unauthorized = (first as? ApiResponse.Failure)?.error.let {
            it is ApiError.Http && it.statusCode == HttpStatusCode.UNAUTHORIZED
        }
        if (!unauthorized || refreshIfNeeded(force = true) != RefreshResult.Refreshed) return first
        return clientProvider().call()
    }

    // endregion

    /**
     * Signs out of the Tatum Tech API (best effort, at most [SIGN_OUT_TIMEOUT_MS]) and always
     * clears the local session.
     */
    suspend fun signOut() {
        if (session != null) {
            val response = withTimeoutOrNull(SIGN_OUT_TIMEOUT_MS) { authenticated { signOut() } }
            if (response !is ApiResponse.Success) {
                Logger.w(TAG, "$SIGN_OUT_FAILED: ${(response as? ApiResponse.Failure)?.error?.message ?: "timed out"}")
            }
        }
        refreshMutex.withLock { clearLocked() }
    }

    private fun isNearExpiry(session: TatumTechSession): Boolean =
        session.expiresAtMillis?.let { it - clock() <= REFRESH_WINDOW_MS } ?: false

    /** The server no longer accepts this refresh token. */
    private fun isRejection(error: ApiError): Boolean =
        error is ApiError.Http && error.statusCode.code in REJECTION_CODES

    /** Saves and applies [newSession]; returns a failure if it could not be stored. */
    private fun persist(newSession: TatumTechSession): ApiResponse<TatumTechSession>? = try {
        store.save(newSession)
        session = newSession
        applyToClient(newSession)
        null
    } catch (e: IOException) {
        Logger.e(TAG, SESSION_NOT_SAVED, e)
        ApiResponse.Failure(ApiError.Unexpected(e, metadata = null))
    }

    private fun clearLocked() {
        store.clear()
        session = null
        applyToClient(null)
    }

    private fun applyToClient(session: TatumTechSession?) {
        val client = clientProvider()
        client.updateConfiguration(
            TatumTechClientConfiguration.Builder()
                .from(client.configuration)
                .setJwtAccessToken(session?.accessToken)
                .setRefreshToken(session?.refreshToken)
                .setTokenExpiration(session?.expiresAtMillis)
                .build()
        )
    }

    private fun ApiResponse<TatumTechAuthSession>.toSession(
        method: TatumTechAuthMethod,
        previous: TatumTechSession?
    ): ApiResponse<TatumTechSession> = when (this) {
        is ApiResponse.Failure -> this
        is ApiResponse.Success -> {
            val accessToken = data.accessToken
            if (accessToken.isNullOrBlank()) {
                ApiResponse.Failure(
                    ApiError.Serialization(IllegalStateException(MISSING_ACCESS_TOKEN), statusCode, null, metadata)
                )
            } else {
                ApiResponse.Success(
                    TatumTechSession(
                        accessToken = accessToken,
                        refreshToken = data.refreshToken ?: previous?.refreshToken,
                        expiresAtMillis = data.expiresIn?.let { clock() + it * 1_000 },
                        authMethod = method,
                        user = data.user ?: previous?.user
                    ),
                    statusCode,
                    metadata
                )
            }
        }
    }

    sealed class RefreshResult {
        data object NoSession : RefreshResult()
        data object NotNeeded : RefreshResult()
        data object Refreshed : RefreshResult()

        /** The server rejected the refresh token; the session was cleared. */
        data object SignedOut : RefreshResult()

        /** Temporary failure (e.g. offline); the session is kept. */
        data class Failed(val error: ApiError) : RefreshResult()
    }

    companion object {
        /** Refresh this long before expiry so requests never carry a just-expired token. */
        const val REFRESH_WINDOW_MS = 60 * 60 * 1_000L

        const val SIGN_OUT_TIMEOUT_MS = 10_000L

        /** 419 is the API's `REFRESH_TOKEN_DOES_NOT_EXIST`. */
        private val REJECTION_CODES = setOf(400, 401, 403, 419)

        private const val MISSING_ACCESS_TOKEN = "Auth response has no accessToken"
        private const val SESSION_REJECTED = "Tatum Tech refresh token rejected; signing out"
        private const val SESSION_NOT_SAVED = "Tatum Tech session could not be saved"
        private const val SIGN_OUT_FAILED = "Tatum Tech sign-out request failed"
    }
}
