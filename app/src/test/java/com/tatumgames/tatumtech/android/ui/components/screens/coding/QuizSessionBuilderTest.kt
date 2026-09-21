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

import com.tatumgames.tatumtech.android.ui.components.screens.coding.models.AnswerFeedback
import com.tatumgames.tatumtech.android.ui.components.screens.coding.models.CodingChallenges
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizSessionBuilderTest {

    @Test
    fun buildSession_containsUniqueIdsAndRequestedSize() {
        val pool = (1..20).map { sample("q$it", "A$it") }
        val session = QuizSessionBuilder.buildSession(pool, 10)
        assertEquals(10, session.size)
        assertEquals(10, session.map { it.id }.toSet().size)
    }

    @Test
    fun buildSession_doesNotFabricateDuplicatesWhenPoolIsSmall() {
        val pool = listOf(sample("a", "A"), sample("b", "B"), sample("c", "C"))
        val session = QuizSessionBuilder.buildSession(pool, 10)
        assertEquals(3, session.size)
        assertEquals(3, session.map { it.id }.toSet().size)
    }

    @Test
    fun shuffleOptions_preservesCorrectAnswer() {
        val q = sample(
            id = "s1",
            correct = "Correct",
            options = listOf("Correct", "Wrong1", "Wrong2", "Wrong3")
        )
        repeat(20) {
            val shuffled = QuizSessionBuilder.shuffleOptions(q)
            assertTrue(shuffled.correctAnswer in shuffled.options)
            assertEquals("Correct", shuffled.correctAnswer)
            assertEquals(4, shuffled.options.size)
        }
    }

    @Test
    fun countCorrect_matchesAnswerMap() {
        val questions = listOf(
            sample("1", "A"),
            sample("2", "B"),
            sample("3", "C")
        )
        val answers = mapOf("1" to "A", "2" to "X", "3" to "C")
        val (results, correct) = QuizSessionBuilder.countCorrect(questions, answers)
        assertEquals(2, correct)
        assertEquals(true, results["1"])
        assertEquals(false, results["2"])
        assertEquals(true, results["3"])
    }

    @Test
    fun effectiveSessionSize_respectsDailyRemaining() {
        assertEquals(10, QuizSessionBuilder.effectiveSessionSize(10, 0, 30))
        assertEquals(5, QuizSessionBuilder.effectiveSessionSize(10, 25, 30))
        assertEquals(0, QuizSessionBuilder.effectiveSessionSize(10, 30, 30))
    }

    @Test
    fun answerFeedback_isMutuallyExclusive() {
        val values = AnswerFeedback.entries.toSet()
        assertEquals(
            setOf(AnswerFeedback.NONE, AnswerFeedback.CORRECT, AnswerFeedback.INCORRECT),
            values
        )
        assertNotEquals(AnswerFeedback.CORRECT, AnswerFeedback.INCORRECT)
    }

    private fun sample(
        id: String,
        correct: String,
        options: List<String> = listOf(correct, "W1", "W2", "W3")
    ): CodingChallenges {
        return CodingChallenges(
            id = id,
            createdAt = "2026-01-01T00:00:00Z",
            language = "Kotlin",
            level = "Beginner",
            question = "Question $id?",
            options = options,
            correctAnswer = correct,
            explanation = "Because $correct",
            platform = "Mobile"
        )
    }
}
