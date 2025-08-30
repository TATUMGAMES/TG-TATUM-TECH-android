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
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_ATTENDEES
import com.tatumgames.tatumtech.android.database.entity.AttendeeEntity

@Dao
interface AttendeeDao {
    @Query("SELECT * FROM $TABLE_ATTENDEES WHERE eventId = :eventId ORDER BY name ASC")
    suspend fun getAttendeesForEvent(eventId: Long): List<AttendeeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendees(attendees: List<AttendeeEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendee(attendee: AttendeeEntity)
    
    @Delete
    suspend fun removeAttendee(attendee: AttendeeEntity)
    
    @Query("DELETE FROM $TABLE_ATTENDEES WHERE id = :attendeeId")
    suspend fun removeAttendee(attendeeId: Long)

    @Query("UPDATE $TABLE_ATTENDEES SET isFriend = 1 WHERE id = :attendeeId")
    suspend fun addFriend(attendeeId: Long)

    @Query("UPDATE $TABLE_ATTENDEES SET isFriend = 0 WHERE id = :attendeeId")
    suspend fun removeFriend(attendeeId: Long)

    @Query("SELECT * FROM $TABLE_ATTENDEES WHERE isFriend = 1 ORDER BY name ASC")
    suspend fun getAllFriends(): List<AttendeeEntity>
} 
