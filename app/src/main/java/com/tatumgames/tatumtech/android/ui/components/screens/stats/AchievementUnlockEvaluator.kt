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

import android.content.Context
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.models.Achievement

object AchievementIconResolver {
    fun resolve(context: Context, iconName: String?): Int {
        if (iconName.isNullOrBlank()) return R.drawable.badge_first_step
        val id = context.resources.getIdentifier(iconName, "drawable", context.packageName)
        return if (id != 0) id else R.drawable.badge_first_step
    }
}

data class AchievementProgressCounts(
    val counts: Map<String, Int>
) {
    fun count(key: String): Int = counts[key] ?: 0
}

object AchievementUnlockEvaluator {
    fun isUnlocked(achievement: Achievement, progress: AchievementProgressCounts): Boolean {
        val key = achievement.trackingKey ?: return false
        return progress.count(key) >= achievement.threshold.coerceAtLeast(0)
    }

    fun withUnlockState(
        achievements: List<Achievement>,
        progress: AchievementProgressCounts
    ): List<Achievement> = achievements.map { ach ->
        ach.copy(isUnlocked = isUnlocked(ach, progress))
    }
}
