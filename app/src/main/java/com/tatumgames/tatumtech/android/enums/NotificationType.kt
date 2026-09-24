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
package com.tatumgames.tatumtech.android.enums

import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes

/**
 * Structured notification categories with a default navigation destination.
 * Destination resolution stays centralized here — not in UI string comparisons.
 */
enum class NotificationType(val destinationRoute: String) {
    CODING_CHALLENGE(NavRoutes.CODING_CHALLENGES_SCREEN),
    EVENT(NavRoutes.UPCOMING_EVENTS_SCREEN),
    CAREER(NavRoutes.CAREER_SCREEN),
    COMMUNITY(NavRoutes.COMMUNITY_SCREEN),
    GAME(NavRoutes.GAMES_SCREEN);

    companion object {
        fun fromStorage(value: String): NotificationType? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
