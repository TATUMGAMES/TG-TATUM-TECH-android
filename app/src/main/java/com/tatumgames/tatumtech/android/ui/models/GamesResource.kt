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
package com.tatumgames.tatumtech.android.ui.models

/**
 * Games → Resources category mapping: references existing [Partner] ids only.
 * Partner name/logo/description/website come from the Partners dataset.
 */
data class GamesResourceCategory(
    val category: String,
    val partnerIds: List<String> = emptyList()
)

/**
 * Resolved row for UI: category header grouping + partner payload.
 */
data class GamesResourceEntry(
    val category: String,
    val partner: Partner
) {
    /** Prefer product brand (e.g. MIKROS) when present. */
    val displayName: String
        get() = partner.productName?.takeIf { it.isNotBlank() } ?: partner.name
}
