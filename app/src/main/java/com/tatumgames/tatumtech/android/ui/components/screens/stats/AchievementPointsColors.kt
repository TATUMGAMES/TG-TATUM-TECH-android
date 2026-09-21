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

import androidx.compose.ui.graphics.Color
import com.tatumgames.tatumtech.android.ui.theme.FallDeepOrange500
import com.tatumgames.tatumtech.android.ui.theme.Grey400
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.ui.theme.Purple700
import com.tatumgames.tatumtech.android.ui.theme.SuccessGreen

/**
 * Deterministic badge background for achievement point totals.
 * Text on the badge should remain white for contrast.
 */
object AchievementPointsColors {
    fun badgeColor(points: Int, unlocked: Boolean): Color {
        if (!unlocked) return Grey400.copy(alpha = 0.45f)
        return when {
            points >= 100 -> Purple700
            points >= 25 -> FallDeepOrange500
            points >= 15 -> Purple500
            else -> SuccessGreen
        }
    }
}
