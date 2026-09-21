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

import com.tatumgames.tatumtech.android.ui.components.screens.coding.models.CodingChallenges

/**
 * Pure helpers for building a quiz session from a question pool.
 * Intentionally free of Android framework APIs so unit tests can run on the JVM.
 */
object QuizSessionBuilder {

    /**
     * Selects up to [count] unique questions by stable [CodingChallenges.id],
     * then shuffles answer options while preserving [CodingChallenges.correctAnswer].
     *
     * Does not fabricate duplicates when the pool is smaller than [count].
     */
    fun buildSession(
        pool: List<CodingChallenges>,
        count: Int
    ): List<CodingChallenges> {
        if (count <= 0) return emptyList()

        val unique = pool
            .filter { it.id.isNotBlank() }
            .distinctBy { it.id }

        return unique
            .shuffled()
            .mapNotNull { shuffleOptionsOrNull(it) }
            .take(count)
    }

    /**
     * Shuffles display options; correctness remains tied to [CodingChallenges.correctAnswer] text.
     * Returns null when the correct answer is missing from options.
     */
    fun shuffleOptionsOrNull(question: CodingChallenges): CodingChallenges? {
        val distinctOptions = question.options.distinct()
        if (question.correctAnswer !in distinctOptions) {
            return null
        }
        return question.copy(options = distinctOptions.shuffled())
    }

    /** Test/helper wrapper that assumes valid option data. */
    fun shuffleOptions(question: CodingChallenges): CodingChallenges {
        return shuffleOptionsOrNull(question)
            ?: error("Question ${question.id}: correctAnswer not present in options")
    }

    /**
     * Session score derived from answered questions only.
     */
    fun countCorrect(
        questions: List<CodingChallenges>,
        answersById: Map<String, String>
    ): Pair<Map<String, Boolean>, Int> {
        val results = questions.associate { q ->
            val chosen = answersById[q.id]
            q.id to (chosen != null && chosen == q.correctAnswer)
        }
        val correct = results.values.count { it }
        return results to correct
    }

    fun effectiveSessionSize(
        requestedSize: Int,
        todayAnswerCount: Int,
        dailyLimit: Int
    ): Int {
        val remaining = (dailyLimit - todayAnswerCount).coerceAtLeast(0)
        return minOf(requestedSize, remaining)
    }
}
