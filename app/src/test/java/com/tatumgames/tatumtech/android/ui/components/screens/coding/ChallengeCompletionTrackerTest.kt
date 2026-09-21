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
package com.tatumgames.tatumtech.android.ui.components.screens.coding

import com.tatumgames.tatumtech.android.database.entity.TimelineEntity
import com.tatumgames.tatumtech.android.enums.TimelineFilter
import com.tatumgames.tatumtech.android.enums.TimelineType
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.timeline.TimelineFilterWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChallengeCompletionTrackerTest {

    @Test
    fun relatedId_isStableForSameSession() {
        val a = ChallengeCompletionTracker.relatedId("route|Kotlin|Beginner", "[\"q1\",\"q2\"]")
        val b = ChallengeCompletionTracker.relatedId("route|Kotlin|Beginner", "[\"q1\",\"q2\"]")
        assertEquals(a, b)
    }

    @Test
    fun relatedId_differsWhenQuestionSetDiffers() {
        val a = ChallengeCompletionTracker.relatedId("route|Kotlin|Beginner", "[\"q1\",\"q2\"]")
        val b = ChallengeCompletionTracker.relatedId("route|Kotlin|Beginner", "[\"q3\",\"q4\"]")
        assertNotEquals(a, b)
    }

    @Test
    fun buildDescription_includesLanguageAndLevel() {
        assertEquals(
            "Completed Kotlin Beginner Challenge",
            ChallengeCompletionTracker.buildDescription("Kotlin", "Beginner")
        )
    }

    @Test
    fun categoryFromDescription_parsesKnownFormats() {
        assertEquals(
            "Kotlin",
            ChallengeCompletionTracker.categoryFromDescription(
                "Completed Kotlin Beginner Challenge"
            )
        )
        assertEquals(
            "AI/LLM",
            ChallengeCompletionTracker.categoryFromDescription(
                "Completed AI/LLM Intermediate Challenge"
            )
        )
        assertEquals(
            "Mock Interview",
            ChallengeCompletionTracker.categoryFromDescription(
                "Completed Mock Interview Advanced Challenge"
            )
        )
        assertNull(
            ChallengeCompletionTracker.categoryFromDescription("Visited donation page")
        )
    }

    @Test
    fun categoryCounts_groupsCompletionsAndLeavesZeros() {
        val events = listOf(
            completion("Completed Kotlin Beginner Challenge", 1L),
            completion("Completed Kotlin Intermediate Challenge", 2L),
            completion("Completed Kotlin Advanced Challenge", 3L),
            completion("Completed AI/LLM Beginner Challenge", 4L)
        )
        val counts = ChallengeCompletionTracker.categoryCounts(events)
        assertEquals(3, counts["Kotlin"])
        assertEquals(1, counts["AI/LLM"])
        assertEquals(0, counts["Java"])
        assertEquals(4, events.size)
        assertEquals(4, counts.values.sum())
    }

    @Test
    fun trackLabel_fallsBackToRouteWhenLanguageBlank() {
        assertEquals(
            "LeetCode",
            ChallengeCompletionTracker.trackLabel("", NavRoutes.LEET_CODE_CHALLENGES_SCREEN)
        )
        assertEquals(
            "AI/LLM",
            ChallengeCompletionTracker.trackLabel("", NavRoutes.AI_LLM_CHALLENGES_SCREEN)
        )
        assertEquals(
            "Kotlin",
            ChallengeCompletionTracker.trackLabel("Kotlin", NavRoutes.CODING_CHALLENGES_SCREEN)
        )
    }

    @Test
    fun timelineFilterWindow_usesRollingWindows() {
        val now = 1_700_000_000_000L
        assertEquals(
            now - 24 * 60 * 60 * 1000L,
            TimelineFilterWindow.fromTimestamp(TimelineFilter.TODAY, now)
        )
        assertEquals(
            now - 7 * 24 * 60 * 60 * 1000L,
            TimelineFilterWindow.fromTimestamp(TimelineFilter.WEEK, now)
        )
        assertEquals(
            now - 30 * 24 * 60 * 60 * 1000L,
            TimelineFilterWindow.fromTimestamp(TimelineFilter.MONTH, now)
        )
    }

    private fun completion(description: String, relatedId: Long) = TimelineEntity(
        type = TimelineType.CHALLENGE_COMPLETION.typeValue,
        description = description,
        relatedId = relatedId,
        timestamp = relatedId
    )
}
