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
import com.tatumgames.tatumtech.android.database.entity.EngagementCounterEntity

@Dao
interface EngagementCounterDao {
    @Query("SELECT * FROM engagement_counters WHERE type = :type LIMIT 1")
    suspend fun get(type: String): EngagementCounterEntity?

    @Query("SELECT count FROM engagement_counters WHERE type = :type LIMIT 1")
    suspend fun getCount(type: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: EngagementCounterEntity)

    @Query("SELECT * FROM engagement_counters")
    suspend fun getAll(): List<EngagementCounterEntity>
}
