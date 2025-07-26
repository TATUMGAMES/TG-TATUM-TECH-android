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
import androidx.room.Update
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_CODING_CHALLENGE
import com.tatumgames.tatumtech.android.database.entity.CodingChallengeEntity

@Dao
interface CodingChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(codingChallengeEntity: CodingChallengeEntity): Long

    @Update
    suspend fun update(codingChallengeEntity: CodingChallengeEntity)

    @Delete
    suspend fun delete(codingChallengeEntity: CodingChallengeEntity)

    @Query("SELECT * FROM $TABLE_CODING_CHALLENGE ORDER BY timestamp DESC")
    suspend fun getAllChallengeAnswers(): List<CodingChallengeEntity>

    @Query("SELECT * FROM $TABLE_CODING_CHALLENGE  WHERE questionId = :questionId LIMIT 1")
    suspend fun getChallengeAnswerByQuestionId(questionId: String): CodingChallengeEntity?
} 
