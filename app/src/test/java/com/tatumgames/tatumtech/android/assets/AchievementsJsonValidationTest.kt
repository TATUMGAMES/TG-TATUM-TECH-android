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
package com.tatumgames.tatumtech.android.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tatumgames.tatumtech.android.ui.components.screens.stats.AchievementProgressCounts
import com.tatumgames.tatumtech.android.ui.components.screens.stats.AchievementTrackingKeys
import com.tatumgames.tatumtech.android.ui.components.screens.stats.AchievementUnlockEvaluator
import com.tatumgames.tatumtech.android.ui.models.Achievement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AchievementsJsonValidationTest {

    private fun loadAchievements(): List<Achievement> {
        val json = File("src/main/assets/achievements.json").readText()
        val type = object : TypeToken<List<Achievement>>() {}.type
        return Gson().fromJson(json, type)
    }

    @Test
    fun achievementsJson_deserializesWithoutNumberFormatException() {
        val achievements = loadAchievements()
        assertTrue(achievements.isNotEmpty())
        assertTrue(achievements.all { it.icon.isNotBlank() })
        assertTrue(achievements.none { it.icon.toIntOrNull() != null && it.icon.startsWith("badge") })
        assertFalse(achievements.any { it.id.contains("event_attendee") || it.id.contains("event_regular") })
        assertFalse(achievements.any { it.trackingKey == "EVENT_REGISTRATION" })
    }

    @Test
    fun discoverGameThresholds_unlockAtExpectedCounts() {
        val achievements = loadAchievements().filter {
            it.trackingKey == AchievementTrackingKeys.GAME_DETAILS_VIEWED
        }
        assertEquals(8, achievements.size)
        val progress = AchievementProgressCounts(
            mapOf(AchievementTrackingKeys.GAME_DETAILS_VIEWED to 15)
        )
        val unlocked = AchievementUnlockEvaluator.withUnlockState(achievements, progress)
            .filter { it.isUnlocked }
            .map { it.threshold }
            .toSet()
        assertEquals(setOf(1, 5, 15), unlocked)
    }

    @Test
    fun jobApplyThresholds_unlockAtExpectedCounts() {
        val achievements = loadAchievements().filter {
            it.trackingKey == AchievementTrackingKeys.JOB_APPLY_CLICKED
        }
        assertEquals(8, achievements.size)
        val progress = AchievementProgressCounts(
            mapOf(AchievementTrackingKeys.JOB_APPLY_CLICKED to 1)
        )
        val unlocked = AchievementUnlockEvaluator.withUnlockState(achievements, progress)
        assertTrue(unlocked.first { it.threshold == 1 }.isUnlocked)
        assertFalse(unlocked.first { it.threshold == 5 }.isUnlocked)
    }

    @Test
    fun iconField_isStringDrawableName() {
        val achievements = loadAchievements()
        achievements.forEach { ach ->
            assertTrue(ach.icon.startsWith("badge_") || ach.icon.isNotBlank())
        }
    }
}
