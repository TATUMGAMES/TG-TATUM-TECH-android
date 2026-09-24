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
package com.tatumgames.tatumtech.android.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Patterns
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.android.ui.theme.StringHashPalette
import com.tatumgames.tatumtech.framework.android.logger.Logger
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.TimeZone

object Utils {

    /**
     * Determine whether you have been granted a particular permission.
     *
     * @param context Interface to global information about an application environment.
     * @param permissions The name of the permission being checked.
     * @return True if permissions are enabled, otherwise false.
     */
    fun hasPermissions(
        context: Context,
        vararg permissions: String
    ): Boolean {
        return permissions.all {
            it.isNotEmpty() && ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isPasswordValid(password: String): Boolean {
        val minLength = 6
        val hasNoSpaces = !password.contains(" ")
        val hasUppercase = password.any { it.isUpperCase() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return password.length >= minLength && hasNoSpaces && hasUppercase && hasSpecialChar
    }

    fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Generate an anonymous ID in the format "anon_######".
     * 
     * @return A unique anonymous identifier with 6-digit random number.
     */
    fun generateAnonymousId(): String {
        val random = Random()
        val randomNumber = random.nextInt(900000) + 100000 // Generates 100000-999999
        return "anon_$randomNumber"
    }

    fun parseDate(dateString: String): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val zonedDateTime = ZonedDateTime.parse(dateString)
                zonedDateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a"))
            } else {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                val date = format.parse(dateString)
                val displayFormat =
                    SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
                displayFormat.format(date ?: Date())
            }
        } catch (e: Exception) {
            Logger.e(TAG, e.message)
            dateString
        }
    }

    fun formatTimestamp(
        context: Context,
        timestamp: Long
    ): String {
        return try {
            val sdf = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            Logger.e(TAG, e.message)
            context.getString(R.string.to_be_determined)
        }
    }

    fun getNameInitials(
        context: Context,
        name: String
    ): String {
        val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return if (words.isEmpty()) {
            context.getString(R.string.anonymous_name_fallback)
        } else {
            words.take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        }
    }

    fun generateColorFromString(input: String): Color {
        val hash = input.hashCode()
        val index = kotlin.math.abs(hash) % StringHashPalette.size
        return StringHashPalette[index]
    }

    /**
     * Get user's display name or anonymous ID based on available information.
     * 
     * @param user The user entity to check.
     * @return First name if available, full name if both names are available, otherwise anonymous ID.
     */
    fun getUserNameOrAnonymous(user: com.tatumgames.tatumtech.android.database.entity.UserEntity?): String {
        return when {
            user != null && user.firstName != null && user.firstName.isNotBlank() &&
                    user.lastName != null && user.lastName.isNotBlank() -> {
                "${user.firstName} ${user.lastName}"
            }

            user != null && user.firstName != null && user.firstName.isNotBlank() -> {
                user.firstName
            }

            else -> {
                user?.anonymousId ?: generateAnonymousId()
            }
        }
    }

    /**
     * Opens an http(s) URL in an external browser / associated app.
     * Does nothing for blank URLs; shows [failureMessageRes] on failure.
     */
    fun openUrl(
        context: Context,
        url: String?,
        failureMessageRes: Int = R.string.open_url_failed
    ) {
        val trimmed = url?.trim().orEmpty()
        if (trimmed.isEmpty()) return
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, trimmed.toUri()))
        }.onFailure {
            Logger.e(TAG, it.message)
            Toast.makeText(context, context.getString(failureMessageRes), Toast.LENGTH_SHORT).show()
        }
    }
}
