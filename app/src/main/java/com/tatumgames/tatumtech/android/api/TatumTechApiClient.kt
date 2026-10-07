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
package com.tatumgames.tatumtech.android.api

import com.google.gson.Gson
import com.tatumgames.tatumtech.android.api.error.ApiErrorClassifier
import com.tatumgames.tatumtech.android.api.error.ApiErrorLogger
import com.tatumgames.tatumtech.android.api.models.TatumTechAuthSession
import com.tatumgames.tatumtech.android.api.models.TatumTechEmailSignInRequest
import com.tatumgames.tatumtech.android.api.models.TatumTechEvent
import com.tatumgames.tatumtech.android.api.models.TatumTechEventData
import com.tatumgames.tatumtech.android.api.models.TatumTechEventsData
import com.tatumgames.tatumtech.android.api.models.TatumTechForgotPasswordRequest
import com.tatumgames.tatumtech.android.api.models.TatumTechGoogleSignInRequest
import com.tatumgames.tatumtech.android.api.models.TatumTechPartner
import com.tatumgames.tatumtech.android.api.models.TatumTechPartnerCategory
import com.tatumgames.tatumtech.android.api.models.TatumTechPartnerData
import com.tatumgames.tatumtech.android.api.models.TatumTechPartnersData
import com.tatumgames.tatumtech.android.api.models.TatumTechRefreshTokenRequest
import com.tatumgames.tatumtech.android.api.models.TatumTechResetPasswordRequest
import com.tatumgames.tatumtech.android.api.models.TatumTechResponse
import com.tatumgames.tatumtech.android.api.models.TatumTechSignUpRequest
import com.tatumgames.tatumtech.android.api.models.TatumTechSpeaker
import com.tatumgames.tatumtech.android.api.models.TatumTechSpeakersData
import com.tatumgames.tatumtech.android.api.models.TatumTechUpdateUserProfileRequest
import com.tatumgames.tatumtech.framework.android.http.analytics.AnalyticsClient
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpErrorEvent
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpErrorType
import com.tatumgames.tatumtech.framework.android.http.client.BaseApiClient
import com.tatumgames.tatumtech.framework.android.http.config.BearerToken
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.executor.OkHttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ApiResponse
import com.tatumgames.tatumtech.framework.android.http.response.EmptyStateInfo
import com.tatumgames.tatumtech.framework.android.http.response.ErrorItem
import com.tatumgames.tatumtech.framework.android.http.response.HttpStatusCode

/**
 * Paths of the Tatum Tech API, relative to the configured base URL.
 */
internal object TatumTechEndpoints {
    const val SIGN_IN = "tatum-tech/signin"
    const val SIGN_UP = "tatum-tech/signup"
    const val REFRESH_TOKEN = "tatum-tech/refreshToken"
    const val FORGOT_PASSWORD = "tatum-tech/forgotPassword"
    const val RESET_PASSWORD = "tatum-tech/resetPassword"
    const val SIGN_OUT = "tatum-tech/signout"
    const val UPDATE_USER_PROFILE = "tatum-tech/updateUserProfile"
    const val UPCOMING_EVENTS = "tatum-tech/upcomingEvents"
    const val EVENTS = "tatum-tech/events"
    const val SPEAKERS = "speakers"
    const val PARTNERS = "tatum-tech/partners"

    const val QUERY_CATEGORY = "category"
}

/**
 * Typed access to the Tatum Tech API. Every call returns the unwrapped `data` of the response
 * envelope, or an [ApiError]; calls are main-safe and never throw except on cancellation.
 *
 * Obtain the shared instance from [TatumTechApiProvider].
 */
class TatumTechApiClient(
    configuration: TatumTechClientConfiguration,
    executor: HttpRequestExecutor = OkHttpRequestExecutor(configuration.connectTimeout),
    analyticsClient: AnalyticsClient? = null
) : BaseApiClient<TatumTechClientConfiguration>(
    configuration = configuration,
    executor = executor,
    analyticsClient = analyticsClient
) {

    // region Authentication
    suspend fun signIn(email: String, password: String, deviceId: String): ApiResponse<TatumTechAuthSession> =
        post<TatumTechResponse<TatumTechAuthSession>>(
            TatumTechEndpoints.SIGN_IN,
            body = TatumTechEmailSignInRequest(email, password, deviceId)
        ).unwrapData()

    suspend fun signInWithGoogle(googleIdToken: String, deviceId: String): ApiResponse<TatumTechAuthSession> =
        post<TatumTechResponse<TatumTechAuthSession>>(
            TatumTechEndpoints.SIGN_IN,
            body = TatumTechGoogleSignInRequest(googleIdToken, deviceId)
        ).unwrapData()

    suspend fun signUp(
        email: String,
        password: String,
        confirmPassword: String,
        deviceId: String
    ): ApiResponse<TatumTechAuthSession> =
        post<TatumTechResponse<TatumTechAuthSession>>(
            TatumTechEndpoints.SIGN_UP,
            body = TatumTechSignUpRequest(email, password, confirmPassword, deviceId)
        ).unwrapData()

    /**
     * @param previousAccessToken Sent as `Authorization: Bearer …` when provided.
     */
    suspend fun refreshToken(
        refreshToken: String,
        deviceId: String,
        previousAccessToken: String? = null
    ): ApiResponse<TatumTechAuthSession> {
        val headers = BearerToken.normalize(previousAccessToken)
            ?.let { mapOf(HEADER_AUTHORIZATION to it) }
            .orEmpty()
        return post<TatumTechResponse<TatumTechAuthSession>>(
            TatumTechEndpoints.REFRESH_TOKEN,
            body = TatumTechRefreshTokenRequest(refreshToken, deviceId),
            headers = headers
        ).unwrapData()
    }

    suspend fun forgotPassword(email: String): ApiResponse<EmptyStateInfo> =
        post<TatumTechResponse<Any>>(
            TatumTechEndpoints.FORGOT_PASSWORD,
            body = TatumTechForgotPasswordRequest(email)
        ).unwrapStatus()

    suspend fun resetPassword(
        verifyToken: String,
        password: String,
        confirmPassword: String? = null,
        email: String? = null
    ): ApiResponse<EmptyStateInfo> = post<TatumTechResponse<Any>>(
        TatumTechEndpoints.RESET_PASSWORD,
        body = TatumTechResetPasswordRequest(verifyToken, password, confirmPassword, email)
    ).unwrapStatus()

    /** Requires an access token in the configuration. */
    suspend fun signOut(): ApiResponse<EmptyStateInfo> =
        post<TatumTechResponse<Any>>(
            TatumTechEndpoints.SIGN_OUT,
            body = emptyMap<String, Any>(),
            authenticated = true
        ).unwrapStatus()

    /** Requires an access token in the configuration. `null` names are left unchanged. */
    suspend fun updateUserProfile(firstName: String?, lastName: String?): ApiResponse<EmptyStateInfo> =
        post<TatumTechResponse<Any>>(
            TatumTechEndpoints.UPDATE_USER_PROFILE,
            body = TatumTechUpdateUserProfileRequest(firstName, lastName),
            authenticated = true
        ).unwrapStatus()

    // region Events
    suspend fun getUpcomingEvents(): ApiResponse<List<TatumTechEvent>> =
        get<TatumTechResponse<TatumTechEventsData>>(TatumTechEndpoints.UPCOMING_EVENTS)
            .unwrapData()
            .map { it.events }

    suspend fun getEventDetails(eventId: String): ApiResponse<TatumTechEvent> =
        get<TatumTechResponse<TatumTechEventData>>(eventPath(eventId))
            .unwrapData()
            .requireField("event") { it.event }

    suspend fun getEventSpeakers(eventId: String): ApiResponse<List<TatumTechSpeaker>> =
        get<TatumTechResponse<TatumTechSpeakersData>>(
            "${eventPath(eventId)}/${TatumTechEndpoints.SPEAKERS}"
        ).unwrapData().map { it.speakers }

    // region Partners
    /** @param category Omit to list every partner. */
    suspend fun getPartners(category: TatumTechPartnerCategory? = null): ApiResponse<List<TatumTechPartner>> =
        get<TatumTechResponse<TatumTechPartnersData>>(
            TatumTechEndpoints.PARTNERS,
            queryParameters = mapOf(TatumTechEndpoints.QUERY_CATEGORY to category?.apiValue)
        ).unwrapData().map { it.partners }

    suspend fun getPartnerDetails(partnerId: String): ApiResponse<TatumTechPartner> =
        get<TatumTechResponse<TatumTechPartnerData>>(
            "${TatumTechEndpoints.PARTNERS}/${encodePathSegment(partnerId)}"
        ).unwrapData().requireField("partner") { it.partner }

    private fun eventPath(eventId: String) = "${TatumTechEndpoints.EVENTS}/${encodePathSegment(eventId)}"

    /** A 2xx envelope without `data` cannot satisfy a typed call, so it becomes a parse failure. */
    private fun <D> ApiResponse<TatumTechResponse<D>>.unwrapData(): ApiResponse<D> =
        checkStatus().requireField("data") { it.data }

    /** For calls whose `data` the app does not use; only the envelope status matters. */
    private fun ApiResponse<TatumTechResponse<Any>>.unwrapStatus(): ApiResponse<EmptyStateInfo> =
        when (val checked = checkStatus()) {
            is ApiResponse.Failure -> checked
            is ApiResponse.Success -> ApiResponse.Success(EmptyStateInfo(checked.statusCode), checked.statusCode, checked.metadata)
        }

    /**
     * The API answers HTTP 200 even for failures and reports the outcome in `status.statusCode`
     * (e.g. `406 PASSWORDS_DO_NOT_MATCH`), so a non-2xx envelope status becomes an
     * [ApiError.Http] carrying that status, exactly like a non-2xx status line. Every failure is
     * logged here, so all endpoints share one diagnostic path.
     */
    private fun <D> ApiResponse<TatumTechResponse<D>>.checkStatus(): ApiResponse<TatumTechResponse<D>> {
        val success = when (this) {
            is ApiResponse.Failure -> {
                ApiErrorLogger.log(error, configuration)
                return this
            }
            is ApiResponse.Success -> this
        }
        val status = success.data.status ?: return success
        if (status.statusCode == 0 || status.statusCode in 200..299) return success

        val reported = status.statusMessage?.trim()?.takeIf { it.isNotEmpty() }
        val item = if (reported == null || ApiErrorClassifier.isMachineCode(reported)) {
            ErrorItem(code = reported)
        } else {
            ErrorItem(message = reported)
        }
        val metadata = success.metadata
        val error = ApiError.Http(
            statusCode = HttpStatusCode.fromCode(status.statusCode),
            errors = listOf(item),
            rawBody = envelopeJson.toJson(mapOf("status" to status)),
            metadata = metadata,
            responseStatusCode = success.statusCode.code
        )
        try {
            analyticsClient?.logHttpError(
                HttpErrorEvent(metadata.method, metadata.path, status.statusCode, metadata.durationMs, HttpErrorType.HTTP)
            )
        } catch (e: Exception) {
            // Analytics must never change the outcome of a request.
        }
        ApiErrorLogger.log(error, configuration)
        return ApiResponse.Failure(error)
    }

    private fun <S, D> ApiResponse<S>.requireField(name: String, select: (S) -> D?): ApiResponse<D> =
        when (this) {
            is ApiResponse.Failure -> this
            is ApiResponse.Success -> select(data)
                ?.let { ApiResponse.Success(it, statusCode, metadata) }
                ?: ApiResponse.Failure(
                    ApiError.Serialization(
                        cause = IllegalStateException("Response is missing '$name'"),
                        statusCode = statusCode,
                        rawBody = null,
                        metadata = metadata
                    ).also { ApiErrorLogger.log(it, configuration) }
                )
        }

    private companion object {
        val envelopeJson = Gson()
    }
}
