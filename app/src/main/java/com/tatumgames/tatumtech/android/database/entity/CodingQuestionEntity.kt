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
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_CODING_QUESTIONS

/**
 * Entity representing a coding challenge question in the database.
 * 
 * This entity stores the questions imported from JSON files for the coding challenges.
 *
 * @property id Primary key for the question entity.
 * @property questionId Unique identifier for the question from JSON.
 * @property createdAt Timestamp when the question was created.
 * @property language Programming language of the question.
 * @property level Difficulty level of the question.
 * @property question The question text.
 * @property options List of answer options as JSON string.
 * @property correctAnswer The correct answer.
 * @property explanation Explanation of the correct answer.
 * @property platform Target platform for the question.
 */
@Entity(tableName = TABLE_CODING_QUESTIONS)
data class CodingQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    val questionId: String,
    val createdAt: String,
    val language: String,
    val level: String,
    val question: String,
    val options: String, // JSON string of options
    val correctAnswer: String,
    val explanation: String,
    val platform: String
)

