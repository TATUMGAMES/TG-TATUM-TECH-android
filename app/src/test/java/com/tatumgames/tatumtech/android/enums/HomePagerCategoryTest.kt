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

import org.junit.Assert.assertEquals
import org.junit.Test

class HomePagerCategoryTest {

    @Test
    fun toString_mapsKnownValues() {
        assertEquals(HomePagerCategory.EVENTS, HomePagerCategory.toString("Events"))
        assertEquals(HomePagerCategory.CODING, HomePagerCategory.toString("Coding"))
        assertEquals(HomePagerCategory.COMMUNITY, HomePagerCategory.toString("Community"))
        assertEquals(HomePagerCategory.CAREER, HomePagerCategory.toString("Career"))
        assertEquals(HomePagerCategory.GAMES, HomePagerCategory.toString("Games"))
    }

    @Test
    fun toString_fallsBackToEventsForUnknownValue() {
        assertEquals(HomePagerCategory.EVENTS, HomePagerCategory.toString("Unknown"))
        assertEquals(HomePagerCategory.EVENTS, HomePagerCategory.toString(""))
    }

    @Test
    fun entries_preserveExpectedStableValues() {
        assertEquals(
            listOf("Events", "Coding", "Community", "Career", "Games"),
            HomePagerCategory.entries.map { it.value }
        )
    }
}
