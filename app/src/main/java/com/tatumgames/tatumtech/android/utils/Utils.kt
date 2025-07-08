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

import android.os.Build
import android.util.Patterns
import androidx.compose.ui.graphics.Color
import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.framework.android.logger.Logger
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Utils {

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

    fun parseDate(dateString: String): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val zonedDateTime = ZonedDateTime.parse(dateString)
                zonedDateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a"))
            } else {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                val date = format.parse(dateString)
                val displayFormat = SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
                displayFormat.format(date ?: Date())
            }
        } catch (e: Exception) {
            Logger.e(TAG, e.message)
            dateString
        }
    }

    fun drawableToUri(drawableId: Int): String {
        return "android.resource://com.tatumgames.tatumtech.android/$drawableId"
    }

    fun generateColorFromString(input: String): Color {
        val colors = listOf(
            Color(0xFFE57373), // Red
            Color(0xFF81C784), // Green
            Color(0xFF64B5F6), // Blue
            Color(0xFFFFB74D), // Orange
            Color(0xFFBA68C8), // Purple
            Color(0xFF4DB6AC), // Teal
            Color(0xFFFF8A65), // Deep Orange
            Color(0xFF9575CD), // Deep Purple
            Color(0xFF4FC3F7), // Light Blue
            Color(0xFF81C784), // Light Green
            Color(0xFFFFD54F), // Yellow
            Color(0xFFA1887F)  // Brown
        )
        val hash = input.hashCode()
        val index = kotlin.math.abs(hash) % colors.size
        return colors[index]
    }
}
