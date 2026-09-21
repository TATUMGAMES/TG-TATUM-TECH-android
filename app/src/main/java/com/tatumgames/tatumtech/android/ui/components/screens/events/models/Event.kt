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
package com.tatumgames.tatumtech.android.ui.components.screens.events.models

/**
 * Upcoming event listing model (local JSON / future API response).
 *
 * [lumaUrl] opens external RSVP when present. Registration is not tracked in-app.
 */
data class Event(
    val id: Long,
    val name: String,
    val host: String,
    val date: String,
    val durationHours: Int,
    val location: String,
    val featuredImage: String,
    val lumaUrl: String? = null,
    val virtualSpeakers: List<VirtualSpeaker> = emptyList(),
    /** Legacy field retained for older UI that still references attendees. */
    val attendees: List<Attendee> = emptyList(),
    @Deprecated("Local registration toggle removed; RSVP is external via lumaUrl")
    val isRegistrationOpen: Boolean = true
) {
    val hasVirtualSpeakers: Boolean get() = virtualSpeakers.isNotEmpty()

    val registerEnabled: Boolean get() = !lumaUrl.isNullOrBlank()

    fun speakersInOrder(): List<VirtualSpeaker> =
        virtualSpeakers.sortedWith(
            compareBy<VirtualSpeaker> { it.sortOrder }.thenBy { it.startTime.orEmpty() }
        )
}

/**
 * Virtual speaker session attached to an [Event].
 */
data class VirtualSpeaker(
    val id: String,
    val name: String,
    val companyName: String? = null,
    val profileImage: String? = null,
    val description: String? = null,
    val speakingTopic: String,
    val speakingSchedule: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val timeZone: String? = null,
    val meetUrl: String? = null,
    val sortOrder: Int = 0
) {
    val joinEnabled: Boolean get() = !meetUrl.isNullOrBlank()
}

// New API Event model (future backend shapes; unused by current UI)
data class ApiEvent(
    val eventId: String,
    val name: String,
    val description: String,
    val startTime: String,
    val endTime: String,
    val location: EventLocation,
    val registration: EventRegistration,
    val isUserRegistered: Boolean,
    val attendees: List<EventAttendee>
)

data class EventLocation(
    val name: String,
    val address: String
)

data class EventRegistration(
    val isOpen: Boolean,
    val deadline: String
)

data class EventAttendee(
    val userId: String,
    val userDetailsUrl: String
)
