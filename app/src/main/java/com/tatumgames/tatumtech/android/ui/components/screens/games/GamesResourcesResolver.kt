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
package com.tatumgames.tatumtech.android.ui.components.screens.games

import com.tatumgames.tatumtech.android.ui.models.GamesResourceCategory
import com.tatumgames.tatumtech.android.ui.models.GamesResourceEntry
import com.tatumgames.tatumtech.android.ui.models.Partner

/**
 * Resolves Games → Resources category partner IDs against the Partners catalog.
 * Missing partners are omitted (no crash).
 */
object GamesResourcesResolver {
    fun resolve(
        categories: List<GamesResourceCategory>,
        partners: List<Partner>
    ): List<GamesResourceEntry> {
        val byId = partners.associateBy { it.id }
        return categories.flatMap { category ->
            category.partnerIds.mapNotNull { partnerId ->
                val partner = byId[partnerId] ?: return@mapNotNull null
                GamesResourceEntry(category = category.category, partner = partner)
            }
        }
    }

    fun groupByCategory(entries: List<GamesResourceEntry>): List<Pair<String, List<GamesResourceEntry>>> {
        if (entries.isEmpty()) return emptyList()
        val order = entries.map { it.category }.distinct()
        return order.map { category ->
            category to entries.filter { it.category == category }
        }.filter { it.second.isNotEmpty() }
    }
}
