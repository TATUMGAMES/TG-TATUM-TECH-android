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
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UpcomingEventsJsonValidationTest {

    @Test
    fun upcomingEventsJson_parsesLumaAndSpeakers() {
        val json = File("src/main/assets/upcoming_events.json").readText()
        val type = object : TypeToken<List<Event>>() {}.type
        val events: List<Event> = Gson().fromJson(json, type)

        assertEquals(1, events.size)
        val event = events.first()
        assertEquals("https://luma.com/14agrzue", event.lumaUrl)
        assertTrue(event.registerEnabled)
        assertTrue(event.hasVirtualSpeakers)

        val speakers = event.speakersInOrder()
        assertEquals(3, speakers.size)
        assertEquals(
            listOf("jeff-bogensberger", "reginald-owens-ii", "tanmay-patil"),
            speakers.map { it.id }
        )
        assertEquals("speaker_jeff_bogensberger", speakers[0].profileImage)
        assertEquals("speaker_reginald_owens", speakers[1].profileImage)
        assertNull(speakers[2].profileImage)
        assertTrue(speakers.all { it.joinEnabled })
        assertEquals("https://meet.google.com/ggu-rnvc-rpm", speakers[0].meetUrl)
        assertFalse(json.contains("Registered"))
    }

    @Test
    fun eventWithoutLuma_disablesRegister() {
        val event = Event(
            id = 2,
            name = "No RSVP",
            host = "Tatum Games",
            date = "2027-01-01T00:00:00Z",
            durationHours = 1,
            location = "South LA",
            featuredImage = "tatum_tech_placeholder_flyer_01",
            lumaUrl = null
        )
        assertFalse(event.registerEnabled)
    }

    @Test
    fun speakerWithoutMeet_disablesJoin() {
        val speaker =
            com.tatumgames.tatumtech.android.ui.components.screens.events.models.VirtualSpeaker(
                id = "x",
                name = "Test",
                speakingTopic = "Topic",
                meetUrl = null
            )
        assertFalse(speaker.joinEnabled)
    }
}
