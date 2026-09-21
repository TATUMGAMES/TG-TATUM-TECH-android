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
package com.tatumgames.tatumtech.android.ui.components.screens.partners

import com.tatumgames.tatumtech.android.ui.models.Partner

/**
 * Category filter vocabulary for the Partners screen.
 */
object PartnerCategoryFilters {

    const val FILTER_ALL = "All"

    /** Full category values stored in JSON. */
    val jsonCategories: List<String> = listOf(
        "Community Partners",
        "Corporate Partners",
        "Education Partners",
        "Game Studio Partners",
        "Government Partners",
        "Technology Partners"
    )

    /** Chip order: All + concise UI labels. */
    data class Chip(val filterKey: String, val jsonCategory: String?)

    val chips: List<Chip> = listOf(
        Chip(FILTER_ALL, null),
        Chip("Community", "Community Partners"),
        Chip("Corporate", "Corporate Partners"),
        Chip("Education", "Education Partners"),
        Chip("Game Studios", "Game Studio Partners"),
        Chip("Government", "Government Partners"),
        Chip("Technology", "Technology Partners")
    )

    fun filter(partners: List<Partner>, selectedChipKey: String): List<Partner> {
        val chip = chips.find { it.filterKey == selectedChipKey } ?: chips.first()
        val filtered = if (chip.jsonCategory == null) {
            partners
        } else {
            partners.filter { it.category == chip.jsonCategory }
        }
        return filtered.sortedWith(
            compareByDescending<Partner> { it.featured }.thenBy { it.name.lowercase() }
        )
    }

    fun shortLabelForCategory(category: String): String =
        chips.find { it.jsonCategory == category }?.filterKey ?: category
}
