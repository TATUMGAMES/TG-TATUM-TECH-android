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
import com.tatumgames.tatumtech.android.database.repository.QuizProgressDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.enums.TimelineType
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.coding.ChallengeCompletionTracker.relatedId

/**
 * Helpers for challenge-completion timeline records and Stats category grouping.
 *
 * A completion is one finished quiz session. Idempotency uses a stable [relatedId]
 * derived from the progress bucket id + ordered question ids for that session.
 */
object ChallengeCompletionTracker {

    private val DESCRIPTION_REGEX =
        Regex("""^Completed (.+) (Beginner|Intermediate|Advanced) Challenge$""")

    /**
     * Categories shown in Stats breakdown (order preserved).
     */
    val statsCategories: List<String> = listOf(
        "Kotlin",
        "Java",
        "JavaScript",
        "Python",
        "C#",
        "AI/LLM",
        "LeetCode",
        "Mock Interview"
    )

    /**
     * Resolves the category/language label used in timeline descriptions and Stats.
     */
    fun trackLabel(language: String, quizRoute: String): String {
        if (language.isNotBlank()) return language
        return when (quizRoute) {
            NavRoutes.AI_LLM_CHALLENGES_SCREEN -> "AI/LLM"
            NavRoutes.LEET_CODE_CHALLENGES_SCREEN -> "LeetCode"
            NavRoutes.MOCK_INTERVIEW_CHALLENGES_SCREEN -> "Mock Interview"
            else -> "Coding"
        }
    }

    fun buildDescription(languageOrTrack: String, level: String): String {
        val label = languageOrTrack.ifBlank { "Coding" }
        return "Completed $label $level Challenge"
    }

    /**
     * Stable Long key for [TimelineEntity.relatedId].
     */
    fun relatedId(progressId: String, questionIdsJson: String): Long {
        return (progressId + "|" + questionIdsJson).hashCode().toLong()
    }

    /**
     * Extracts the category/language label from a completion description, or null if not parseable.
     */
    fun categoryFromDescription(description: String): String? {
        val match = DESCRIPTION_REGEX.matchEntire(description.trim()) ?: return null
        return match.groupValues[1]
    }

    /**
     * Counts challenge completions per Stats category (unknown labels are ignored).
     */
    fun categoryCounts(completions: List<TimelineEntity>): Map<String, Int> {
        val counts = statsCategories.associateWith { 0 }.toMutableMap()
        for (event in completions) {
            val raw = categoryFromDescription(event.description) ?: continue
            val key = statsCategories.find { it.equals(raw, ignoreCase = true) } ?: continue
            counts[key] = (counts[key] ?: 0) + 1
        }
        return counts
    }

    /**
     * Inserts a [TimelineType.CHALLENGE_COMPLETION] event if one with the same relatedId
     * does not already exist.
     *
     * @return true if a new event was inserted.
     */
    suspend fun recordCompletionIfAbsent(
        timelineRepository: TimelineDatabaseRepository,
        progressId: String,
        questionIdsJson: String,
        language: String,
        quizRoute: String,
        level: String,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean {
        val related = relatedId(progressId, questionIdsJson)
        val existing = timelineRepository.getByTypeAndRelatedId(
            TimelineType.CHALLENGE_COMPLETION.typeValue,
            related
        )
        if (existing != null) return false

        timelineRepository.insertTimelineEvent(
            TimelineEntity(
                type = TimelineType.CHALLENGE_COMPLETION.typeValue,
                description = buildDescription(trackLabel(language, quizRoute), level),
                relatedId = related,
                timestamp = timestamp
            )
        )
        return true
    }

    /**
     * Idempotent backfill: any [QuizProgressEntity] marked completed gets a matching timeline
     * event. Safe because relatedId is derived from progress id + question set.
     *
     * Note: only one completed row exists per bucket at a time, so prior sessions in the same
     * bucket that were overwritten cannot be recovered from quiz progress alone.
     */
    suspend fun backfillFromCompletedProgress(
        quizProgressRepository: QuizProgressDatabaseRepository,
        timelineRepository: TimelineDatabaseRepository
    ): Int {
        var inserted = 0
        for (progress in quizProgressRepository.getAllCompleted()) {
            val didInsert = recordCompletionIfAbsent(
                timelineRepository = timelineRepository,
                progressId = progress.id,
                questionIdsJson = progress.questionIdsJson,
                language = progress.language,
                quizRoute = progress.quizRoute,
                level = progress.level,
                timestamp = progress.lastUpdated
            )
            if (didInsert) inserted++
        }
        return inserted
    }
}
