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
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_CODING_CHALLENGE


/**
 * Marks a class as an entity. This class will have a mapping SQLite table in the database.
 *
 * <p>Each entity must have at least 1 field annotated with PrimaryKey. You can also use
 * primaryKeys() attribute to define the primary key.</p>
 *
 * @property questionId Specific question ID for the challenge.
 * @property answerChosen The answer chosen by the user.
 * @property timestamp Marked timestamp for when challenges were answered.
 */
@Entity(tableName = TABLE_CODING_CHALLENGE)
data class CodingChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    val questionId: String,
    val answerChosen: String,
    val timestamp: Long
)
