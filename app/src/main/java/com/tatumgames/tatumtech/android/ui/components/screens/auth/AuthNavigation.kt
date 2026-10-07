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
import com.tatumgames.tatumtech.android.activity.AuthActivity
import com.tatumgames.tatumtech.android.activity.MainActivity
import com.tatumgames.tatumtech.android.reminders.MeetingReminderDestination
import com.tatumgames.tatumtech.android.ui.components.common.StandardAlertDialog
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthError

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
 * Returns to the auth flow after the session ended and removes the main flow from the back stack.
 */
fun openAuthScreen(context: Context) {
    val intent = Intent(context, AuthActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    context.startActivity(intent)
}

/**
 * Explains why Google sign-in failed in the standard dialog, titled like other sign-in errors.
 * Not shown when the user simply dismissed Google's account sheet.
 */
@Composable
fun GoogleAuthErrorDialog(error: GoogleAuthError, onDismiss: () -> Unit) {
    val messageRes = googleAuthErrorMessageRes(error) ?: return
    StandardAlertDialog(
        title = stringResource(R.string.error_title_sign_in),
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
    GoogleAuthError.Network -> R.string.error_message_network
    GoogleAuthError.NoCredentialAvailable,
    GoogleAuthError.ProviderUnavailable -> R.string.google_sign_in_unavailable
    else -> R.string.google_sign_in_failed
}
