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

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.activity.MainActivity
import com.tatumgames.tatumtech.android.reminders.MeetingReminderDestination
import com.tatumgames.tatumtech.android.ui.components.common.StandardAlertDialog
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthError
import com.tatumgames.tatumtech.framework.android.http.response.ApiError

/**
 * Server messages beyond this length are cut short so the dialog stays readable.
 */
internal const val MAX_AUTH_ERROR_MESSAGE_LENGTH = 500

/**
 * Enters the app's main flow and removes the auth flow from the back stack.
 *
 * @param reminderDestination Screen to open on arrival, when launched from a meeting reminder.
 */
fun openMainScreen(context: Context, reminderDestination: MeetingReminderDestination? = null) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    reminderDestination?.writeTo(intent)
    context.startActivity(intent)
}

/**
 * Explains why a Tatum Tech auth request failed. Only dismissed by pressing OK, so the
 * user can't miss it; back presses and outside taps are ignored.
 *
 * Shown from composition (not a [Context]), so it is tied to the hosting Activity's
 * window and is removed automatically if the screen leaves composition.
 */
@Composable
fun AuthErrorDialog(error: ApiError, onDismiss: () -> Unit) {
    StandardAlertDialog(
        title = stringResource(R.string.auth_error_title),
        description = authErrorMessage(
            error = error,
            networkMessage = stringResource(R.string.error_network_unavailable),
            genericMessage = stringResource(R.string.something_went_wrong)
        ),
        confirmButtonText = stringResource(R.string.ok),
        onConfirm = onDismiss
    )
}

/**
 * User-facing text for [error]. Server-provided HTTP messages are shown as-is (trimmed and
 * length-capped); anything else falls back to localized copy so no technical detail leaks.
 */
internal fun authErrorMessage(
    error: ApiError,
    networkMessage: String,
    genericMessage: String
): String = when (error) {
    is ApiError.Http -> error.errors
        .firstNotNullOfOrNull { it.message?.trim()?.takeIf(String::isNotEmpty) }
        ?.let(::capLength)
        ?: genericMessage
    is ApiError.Network -> networkMessage
    is ApiError.Serialization, is ApiError.Unexpected -> genericMessage
}

/**
 * Explains why Google sign-in failed, using the same dialog as other auth errors. Not shown
 * when the user simply dismissed Google's account sheet.
 */
@Composable
fun GoogleAuthErrorDialog(error: GoogleAuthError, onDismiss: () -> Unit) {
    val messageRes = googleAuthErrorMessageRes(error) ?: return
    StandardAlertDialog(
        title = stringResource(R.string.auth_error_title),
        description = stringResource(messageRes),
        confirmButtonText = stringResource(R.string.ok),
        onConfirm = onDismiss
    )
}

/**
 * User-facing copy for a Google sign-in failure, or null when nothing should be shown
 * (the user cancelled). Technical details stay in the logs.
 */
@StringRes
internal fun googleAuthErrorMessageRes(error: GoogleAuthError): Int? = when (error) {
    GoogleAuthError.Cancelled -> null
    GoogleAuthError.Network -> R.string.error_network_unavailable
    GoogleAuthError.NoCredentialAvailable,
    GoogleAuthError.ProviderUnavailable -> R.string.google_sign_in_unavailable
    else -> R.string.google_sign_in_failed
}

private fun capLength(message: String): String =
    if (message.length <= MAX_AUTH_ERROR_MESSAGE_LENGTH) {
        message
    } else {
        message.take(MAX_AUTH_ERROR_MESSAGE_LENGTH).trimEnd() + "…"
    }
