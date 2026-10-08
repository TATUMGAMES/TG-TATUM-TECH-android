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
package com.tatumgames.tatumtech.android.ui.components.common

import androidx.annotation.StringRes
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.api.error.ApiErrorClassifier
import com.tatumgames.tatumtech.android.api.error.ApiErrorKind
import com.tatumgames.tatumtech.android.api.error.ClassifiedApiError
import com.tatumgames.tatumtech.android.api.error.TatumTechServerCode
import com.tatumgames.tatumtech.framework.android.http.response.ApiError

/**
 * What the user was doing when an API call failed. Request-specific failures (validation,
 * conflicts, rejected credentials) are titled after it; service and connectivity failures use
 * [R.string.error_title_generic].
 */
enum class ApiOperation(@StringRes val failureTitle: Int) {
    SIGN_IN(R.string.error_title_sign_in),
    SIGN_UP(R.string.error_title_sign_up),
    FORGOT_PASSWORD(R.string.error_title_forgot_password),
    LOAD_CONTENT(R.string.error_title_load_content),
    SIGN_OUT(R.string.error_title_sign_out),
    UPDATE_PROFILE(R.string.error_title_update_profile)
}

/**
 * User-facing description of a failed API call. Never contains technical details: [serverMessage]
 * is only set for server text that passed
 * [com.tatumgames.tatumtech.android.api.error.SafeServerMessage].
 *
 * @param message Localized copy; shown when [serverMessage] is `null`.
 * @param canRetry Repeating the same request may succeed, so a "Try Again" action makes sense.
 */
data class ApiErrorPresentation(
    @StringRes val title: Int,
    @StringRes val message: Int,
    val serverMessage: String?,
    val canRetry: Boolean
)

/**
 * The single mapping from API failures to dialog and error-state copy. Screens pass the error
 * and their [ApiOperation]; they never interpret status codes themselves.
 */
fun presentApiError(error: ApiError, operation: ApiOperation): ApiErrorPresentation =
    presentApiError(ApiErrorClassifier.classify(error), operation)

internal fun presentApiError(error: ClassifiedApiError, operation: ApiOperation): ApiErrorPresentation {
    val knownMessage = error.knownServerCode?.let(::messageFor)
    val requestSpecific = knownMessage != null || error.kind in REQUEST_SPECIFIC_KINDS ||
        (error.kind == ApiErrorKind.NOT_FOUND && error.serverCode != null)
    return ApiErrorPresentation(
        title = if (requestSpecific) operation.failureTitle else R.string.error_title_generic,
        message = knownMessage ?: messageFor(error, operation),
        serverMessage = error.userMessage.takeIf { knownMessage == null },
        canRetry = error.kind.isTransient
    )
}

private val REQUEST_SPECIFIC_KINDS = setOf(
    ApiErrorKind.BAD_REQUEST,
    ApiErrorKind.UNAUTHORIZED,
    ApiErrorKind.FORBIDDEN,
    ApiErrorKind.CONFLICT
)

@StringRes
private fun messageFor(code: TatumTechServerCode): Int? = when (code) {
    TatumTechServerCode.USER_ALREADY_EXISTS -> R.string.error_message_account_exists
    TatumTechServerCode.INVALID_EMAIL_FORMAT -> R.string.error_message_invalid_email
    TatumTechServerCode.INVALID_PASSWORD_FORMAT -> R.string.error_password_minimum_six_characters
    TatumTechServerCode.PASSWORDS_DO_NOT_MATCH -> R.string.error_passwords_do_not_match
    TatumTechServerCode.WRONG_EMAIL_OR_PASSWORD -> R.string.error_message_wrong_email_or_password
    TatumTechServerCode.REFRESH_TOKEN_DOES_NOT_EXIST,
    TatumTechServerCode.UNAUTHORIZED -> R.string.error_message_session_expired
    TatumTechServerCode.EVENT_NOT_FOUND -> R.string.error_message_not_found
}

@StringRes
private fun messageFor(error: ClassifiedApiError, operation: ApiOperation): Int = when (error.kind) {
    ApiErrorKind.NETWORK_UNAVAILABLE -> R.string.error_message_network
    ApiErrorKind.TIMEOUT -> R.string.error_message_timeout
    ApiErrorKind.BAD_REQUEST -> R.string.error_message_bad_request
    ApiErrorKind.UNAUTHORIZED -> when (operation) {
        ApiOperation.SIGN_IN, ApiOperation.SIGN_UP -> R.string.error_message_credentials_rejected
        else -> R.string.error_message_session_expired
    }
    ApiErrorKind.FORBIDDEN -> R.string.error_message_forbidden
    // Without a server code, a 404 means the endpoint itself is missing: a service problem.
    ApiErrorKind.NOT_FOUND ->
        if (error.serverCode != null) R.string.error_message_not_found else R.string.error_message_service
    ApiErrorKind.CONFLICT -> R.string.error_message_conflict
    ApiErrorKind.RATE_LIMITED -> R.string.error_message_rate_limited
    ApiErrorKind.SERVER_ERROR -> R.string.error_message_server
    ApiErrorKind.INVALID_RESPONSE, ApiErrorKind.UNKNOWN -> R.string.error_message_service
}
