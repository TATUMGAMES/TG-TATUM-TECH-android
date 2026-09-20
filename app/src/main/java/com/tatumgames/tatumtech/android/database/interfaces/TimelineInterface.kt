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

import com.tatumgames.tatumtech.android.database.entity.TimelineEntity

/**
 * Interface defining timeline-related database operations.
 * 
 * Provides a contract for timeline event management operations including
 * creation and retrieval of timeline events.
 */
interface TimelineInterface {

    /**
     * Insert a timeline event into the database.
     * 
     * @param timelineEntity The timeline entity to insert.
     */
    suspend fun insertTimelineEvent(timelineEntity: TimelineEntity)

    /**
     * Get all timeline events from the database.
     * 
     * @return List of all timeline events.
     */
    suspend fun getAllTimelineEvents(): List<TimelineEntity>

    /**
     * Get timeline events from a specific timestamp onwards.
     * 
     * @param fromTimestamp The timestamp to get events from (inclusive).
     * @return List of timeline events from the timestamp.
     */
    suspend fun getTimelineEventsFrom(fromTimestamp: Long): List<TimelineEntity>
} 
