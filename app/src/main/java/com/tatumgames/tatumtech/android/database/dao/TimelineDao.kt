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
package com.tatumgames.tatumtech.android.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_TIMELINE
import com.tatumgames.tatumtech.android.database.entity.TimelineEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TimelineEntity operations.
 * 
 * Provides methods to interact with the timeline table in the database.
 */
@Dao
interface TimelineDao {

    /**
     * Insert a timeline event or replace if exists.
     * 
     * @param timelineEntity The timeline entity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEvent(timelineEntity: TimelineEntity)

    /**
     * Get all timeline events from the database.
     * 
     * @return List of all timeline events, ordered by timestamp descending.
     */
    @Query("SELECT * FROM $TABLE_TIMELINE ORDER BY timestamp DESC")
    suspend fun getAllTimelineEvents(): List<TimelineEntity>

    /**
     * Get timeline events from a specific timestamp onwards.
     *
     * @param fromTimestamp The timestamp to get events from (inclusive).
     * @return List of timeline events from the timestamp, ordered by timestamp descending.
     */
    @Query("SELECT * FROM $TABLE_TIMELINE WHERE timestamp >= :fromTimestamp ORDER BY timestamp DESC")
    suspend fun getTimelineEventsFrom(fromTimestamp: Long): List<TimelineEntity>

    @Query(
        "SELECT * FROM $TABLE_TIMELINE WHERE type = :type AND relatedId = :relatedId LIMIT 1"
    )
    suspend fun getByTypeAndRelatedId(type: String, relatedId: Long): TimelineEntity?

    @Query("SELECT * FROM $TABLE_TIMELINE WHERE timestamp >= :fromTimestamp ORDER BY timestamp DESC")
    fun observeTimelineEventsFrom(fromTimestamp: Long): Flow<List<TimelineEntity>>
} 
