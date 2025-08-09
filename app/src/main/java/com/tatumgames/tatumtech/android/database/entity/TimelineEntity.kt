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
package com.tatumgames.tatumtech.android.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_TIMELINE

/**
 * Marks a class as an entity. This class will have a mapping SQLite table in the database.
 *
 * <p>Each entity must have at least 1 field annotated with PrimaryKey. You can also use
 * primaryKeys() attribute to define the primary key.</p>
 *
 * @property type Enumerated values e.g. `event_registration`, `challenge_completion`, etc.
 * @property description The description of the timeline event.
 * @property relatedId Enumerated values e.g. `eventId`, `challengeId`, `attendeeId`, etc.
 */
@Entity(tableName = TABLE_TIMELINE)
data class TimelineEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    val type: String,
    val description: String,
    val relatedId: Long?,
    val timestamp: Long
) 
