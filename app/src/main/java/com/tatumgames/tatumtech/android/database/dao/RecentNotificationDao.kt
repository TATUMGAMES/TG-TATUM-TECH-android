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
import com.tatumgames.tatumtech.android.database.entity.RecentNotificationEntity

@Dao
interface RecentNotificationDao {
    @Query("SELECT * FROM recent_notifications ORDER BY createdAtMillis DESC")
    suspend fun getAll(): List<RecentNotificationEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: RecentNotificationEntity): Long

    @Query(
        "UPDATE recent_notifications SET readAtMillis = :readAtMillis " +
            "WHERE id = :id AND readAtMillis IS NULL"
    )
    suspend fun markReadIfUnread(id: String, readAtMillis: Long): Int

    @Query("DELETE FROM recent_notifications WHERE type = :type")
    suspend fun deleteByType(type: String)

    @Query(
        "DELETE FROM recent_notifications WHERE readAtMillis IS NOT NULL " +
            "AND readAtMillis < :olderThanMillis"
    )
    suspend fun deleteReadOlderThan(olderThanMillis: Long)

    @Query("SELECT * FROM recent_notifications WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RecentNotificationEntity?
}
