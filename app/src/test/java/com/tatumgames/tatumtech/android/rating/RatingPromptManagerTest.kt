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
package com.tatumgames.tatumtech.android.rating

import com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels.CodingChallengesViewModel.Companion.DAILY_ANSWER_LIMIT
import com.tatumgames.tatumtech.android.ui.components.screens.rating.RatingPromptManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RatingPromptManagerTest {

    @Test
    fun appOpen_promptsOnEveryTwentiethOpen() {
        val prompted = (1..100).filter {
            RatingPromptManager.shouldPromptForAppOpen(it, hasBeenSentToStore = false)
        }
        assertEquals(listOf(20, 40, 60, 80, 100), prompted)
    }

    @Test
    fun appOpen_neverPromptsAfterBeingSentToStore() {
        val prompted = (1..100).filter {
            RatingPromptManager.shouldPromptForAppOpen(it, hasBeenSentToStore = true)
        }
        assertTrue(prompted.isEmpty())
    }

    @Test
    fun appOpen_zeroOpensNeverPrompts() {
        assertFalse(RatingPromptManager.shouldPromptForAppOpen(0, hasBeenSentToStore = false))
    }

    @Test
    fun dailyLimit_firesOnlyOnTransitionToMax() {
        assertTrue(
            RatingPromptManager.reachedLimitThisAnswer(
                DAILY_ANSWER_LIMIT - 1,
                DAILY_ANSWER_LIMIT,
                DAILY_ANSWER_LIMIT
            )
        )
    }

    @Test
    fun dailyLimit_doesNotFireWhenAlreadyAtMax() {
        assertFalse(
            RatingPromptManager.reachedLimitThisAnswer(
                DAILY_ANSWER_LIMIT,
                DAILY_ANSWER_LIMIT,
                DAILY_ANSWER_LIMIT
            )
        )
        assertFalse(
            RatingPromptManager.reachedLimitThisAnswer(
                DAILY_ANSWER_LIMIT,
                DAILY_ANSWER_LIMIT + 1,
                DAILY_ANSWER_LIMIT
            )
        )
    }

    @Test
    fun dailyLimit_doesNotFireBelowMax() {
        assertFalse(
            RatingPromptManager.reachedLimitThisAnswer(
                DAILY_ANSWER_LIMIT - 2,
                DAILY_ANSWER_LIMIT - 1,
                DAILY_ANSWER_LIMIT
            )
        )
    }

    @Test
    fun rating_onlyFourAndFiveStarsGoToStore() {
        assertEquals(
            listOf(false, false, false, true, true),
            (1..5).map { RatingPromptManager.shouldSendToStore(it) }
        )
    }
}
