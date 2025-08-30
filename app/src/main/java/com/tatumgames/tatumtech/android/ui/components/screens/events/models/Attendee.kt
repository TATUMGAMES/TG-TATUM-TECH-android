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

import com.tatumgames.tatumtech.android.enums.ProfileImage

data class Attendee(
    val id: Long,
    val name: String,
    val profileImage: ProfileImage
)

// New models for API responses
data class EventAttendeeResponse(
    val eventId: String,
    val attendees: List<AttendeeUser>
)

data class AttendeeUser(
    val user: AttendeeUserDetails
)

data class AttendeeUserDetails(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val userImages: AttendeeUserImages
)

data class AttendeeUserImages(
    val profileImage: String
) 
