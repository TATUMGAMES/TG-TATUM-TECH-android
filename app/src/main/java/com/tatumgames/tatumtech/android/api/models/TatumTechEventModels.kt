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
package com.tatumgames.tatumtech.android.api.models

/**
 * Event as returned by the API. Ids are strings; numeric JSON ids are read as their text.
 *
 * @param date ISO-8601 start time.
 */
data class TatumTechEvent(
    val id: String = "",
    val name: String? = null,
    val host: String? = null,
    val date: String? = null,
    val durationHours: Int? = null,
    val location: String? = null,
    val featuredImage: String? = null,
    val lumaUrl: String? = null,
    val virtualSpeakers: List<TatumTechSpeaker> = emptyList()
)

data class TatumTechSpeaker(
    val id: String = "",
    val name: String? = null,
    val companyName: String? = null,
    val profileImage: String? = null,
    val description: String? = null,
    val speakingTopic: String? = null,
    val speakingSchedule: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val timeZone: String? = null,
    val meetUrl: String? = null,
    val sortOrder: Int = 0
)

/** `data` of `upcomingEvents`. */
data class TatumTechEventsData(
    val events: List<TatumTechEvent> = emptyList()
)

/** `data` of `events/{eventId}`. */
data class TatumTechEventData(
    val event: TatumTechEvent? = null
)

/** `data` of `events/{eventId}/speakers`. */
data class TatumTechSpeakersData(
    val speakers: List<TatumTechSpeaker> = emptyList()
)
