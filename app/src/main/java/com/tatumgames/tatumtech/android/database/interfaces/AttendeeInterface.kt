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
package com.tatumgames.tatumtech.android.database.interfaces

import com.tatumgames.tatumtech.android.database.entity.AttendeeEntity

/**
 * Interface defining attendee-related database operations.
 * 
 * Provides a contract for attendee management operations including
 * creation, retrieval, friend management, and deletion of attendees.
 */
interface AttendeeInterface {

    /**
     * Get all attendees for a specific event.
     * 
     * @param eventId The ID of the event to get attendees for.
     * @return List of attendees for the event.
     */
    suspend fun getAttendeesForEvent(eventId: Long): List<AttendeeEntity>

    /**
     * Insert multiple attendees into the database.
     * 
     * @param attendees List of attendee entities to insert.
     */
    suspend fun insertAttendees(attendees: List<AttendeeEntity>)

    /**
     * Insert a single attendee into the database.
     * 
     * @param attendee The attendee entity to insert.
     */
    suspend fun insertAttendee(attendee: AttendeeEntity)

    /**
     * Remove an attendee from the database.
     * 
     * @param attendeeId The ID of the attendee to remove.
     */
    suspend fun removeAttendee(attendeeId: Long)

    /**
     * Mark an attendee as a friend.
     * 
     * @param attendeeId The ID of the attendee to mark as friend.
     */
    suspend fun addFriend(attendeeId: Long)

    /**
     * Remove friend status from an attendee.
     * 
     * @param attendeeId The ID of the attendee to remove friend status from.
     */
    suspend fun removeFriend(attendeeId: Long)

    /**
     * Get all attendees marked as friends.
     * 
     * @return List of all friend attendees.
     */
    suspend fun getAllFriends(): List<AttendeeEntity>
} 
