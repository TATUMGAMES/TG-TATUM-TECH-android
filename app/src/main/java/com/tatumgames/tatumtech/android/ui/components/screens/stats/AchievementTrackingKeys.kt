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
package com.tatumgames.tatumtech.android.ui.components.screens.stats

/**
 * Engagement / activity keys used by achievement JSON [trackingKey] fields.
 * UI screens record actions; unlock evaluation lives outside composables.
 */
object AchievementTrackingKeys {
    const val CHALLENGE_COMPLETION = "CHALLENGE_COMPLETION"
    const val QR_SCAN = "QR_SCAN"
    const val CONNECTION_MADE = "CONNECTION_MADE"
    const val DONATION = "DONATION"
    const val GAME_DETAILS_VIEWED = "GAME_DETAILS_VIEWED"
    const val JOB_APPLY_CLICKED = "JOB_APPLY_CLICKED"
}
