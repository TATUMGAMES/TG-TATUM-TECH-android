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
 * Resolves catalog media references for Coil / AsyncImage.
 *
 * Supports:
 * - `drawable:name` → local drawable resource id
 * - http(s) URLs → passed through for remote loading
 */
object GameMediaResolver {

    private const val DRAWABLE_PREFIX = "drawable:"

    fun resolve(context: Context, ref: String?): Any? {
        if (ref.isNullOrBlank()) return null
        if (ref.startsWith(DRAWABLE_PREFIX)) {
            val name = ref.removePrefix(DRAWABLE_PREFIX)
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            return id.takeIf { it != 0 }
        }
        return ref
    }

    fun isComingSoon(game: GameModel): Boolean =
        game.releaseStatus.equals("Coming Soon", ignoreCase = true)

    fun isAvailable(game: GameModel): Boolean =
        game.releaseStatus.equals("Released", ignoreCase = true)
}
