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
package com.tatumgames.tatumtech.android.ui.utils

import android.content.Context
import com.tatumgames.tatumtech.android.ui.models.GameModel

/**
 * Resolves image references from content (API responses or bundled JSON) into data for
 * Coil / AsyncImage. Every content image (games, events, speakers, partners) goes through
 * [resolve], so screens never need to know where an image comes from.
 *
 * Supports:
 * - URIs with a scheme (`https://…`, `http://…`, `android.resource://…`) → passed through unchanged
 * - `drawable:name` or a bare `name` → local drawable resource id
 */
object GameMediaResolver {

    private const val DRAWABLE_PREFIX = "drawable:"
    private val URI_SCHEME = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://")

    /**
     * @return The URI string or drawable id for Coil, or `null` when the reference is blank or names
     * a drawable that doesn't exist, so the caller's placeholder/fallback is shown.
     */
    fun resolve(context: Context, ref: String?): Any? =
        resolve(ref) { name -> context.resources.getIdentifier(name, "drawable", context.packageName) }

    internal fun resolve(ref: String?, drawableId: (String) -> Int): Any? {
        val value = ref?.trim().orEmpty()
        if (value.isEmpty()) return null
        if (URI_SCHEME.containsMatchIn(value)) return value
        val name = value.removePrefix(DRAWABLE_PREFIX)
        if (name.isEmpty()) return null
        return drawableId(name).takeIf { it != 0 }
    }

    fun isComingSoon(game: GameModel): Boolean =
        game.releaseStatus.equals("Coming Soon", ignoreCase = true)

    fun isAvailable(game: GameModel): Boolean =
        game.releaseStatus.equals("Released", ignoreCase = true)
}
