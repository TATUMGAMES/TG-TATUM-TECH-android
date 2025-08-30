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

// Legacy Event model for backward compatibility
data class Event(
    val id: Long,
    val name: String,
    val host: String,
    val date: String,
    val durationHours: Int,
    val location: String,
    val isRegistrationOpen: Boolean,
    val attendees: List<Attendee>,
    val featuredImage: String
)

// New API Event model
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
