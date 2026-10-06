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
package com.tatumgames.tatumtech.framework.android.http.client

import java.net.URLEncoder

internal object UrlResolver {

    /**
     * Joins [baseUrl] and [path] with exactly one slash. Absolute `http(s)` paths are returned
     * unchanged so a client can call another host when it must.
     *
     * @throws IllegalStateException when [path] is relative and no base URL is configured.
     */
    fun resolve(baseUrl: String?, path: String): String {
        val trimmedPath = path.trim()
        if (isAbsolute(trimmedPath)) return trimmedPath
        val base = baseUrl?.trimEnd('/')
            ?: throw IllegalStateException("No base URL configured for relative path '$path'")
        val relative = trimmedPath.trimStart('/')
        return if (relative.isEmpty()) base else "$base/$relative"
    }

    /** Percent-encodes one path segment, including `/`, `?`, and spaces (as `%20`). */
    fun encodePathSegment(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")

    private fun isAbsolute(path: String): Boolean =
        path.startsWith("http://", ignoreCase = true) || path.startsWith("https://", ignoreCase = true)
}
