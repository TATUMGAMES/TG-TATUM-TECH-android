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
package com.tatumgames.tatumtech.android.analytics

import org.json.JSONException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * Pure helpers for analytics payloads (no Firebase dependency — unit-testable).
 */
object AnalyticsSanitizer {

    /**
     * Maps a Navigation Compose route to a stable screen_name without package names.
     * Strips path arguments (e.g. `game_details_screen/abc` → `game_details_screen`).
     */
    fun screenNameFromRoute(route: String?): String {
        val raw = route?.substringBefore('?')?.trim().orEmpty()
        if (raw.isEmpty()) return "unknown"
        val base = raw.substringBefore('/')
        return base.ifBlank { "unknown" }
            .lowercase()
            .replace('-', '_')
    }

    /**
     * Sanitizes a URL to a path template without query strings or invite codes.
     */
    fun sanitizeEndpoint(url: String?): String {
        if (url.isNullOrBlank()) return "unknown"
        return try {
            val uri = java.net.URI(url)
            val path = uri.path?.trim().orEmpty().ifBlank { "/" }
            // Discord invite paths: /api/v9/invites/{code} → /api/v9/invites
            val sanitized = path
                .replace(Regex("/invites/[^/]+"), "/invites")
                .replace(Regex("/[0-9a-fA-F-]{8,}"), "/{id}")
            sanitized.ifBlank { "/" }
        } catch (_: Exception) {
            "unknown"
        }
    }

    fun classifyThrowable(t: Throwable): String = when (t) {
        is SocketTimeoutException -> ApiErrorTypes.TIMEOUT
        is ConnectException -> ApiErrorTypes.CONNECTION
        is IOException -> ApiErrorTypes.IO
        is JSONException -> ApiErrorTypes.PARSE
        else -> ApiErrorTypes.UNKNOWN
    }
}
