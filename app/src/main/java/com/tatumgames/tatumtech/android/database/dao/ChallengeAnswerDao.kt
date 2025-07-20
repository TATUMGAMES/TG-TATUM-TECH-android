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
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_CHALLENGE_ANSWERS
import com.tatumgames.tatumtech.android.database.entity.ChallengeAnswerEntity

@Dao
interface ChallengeAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challengeAnswer: ChallengeAnswerEntity): Long

    @Update
    suspend fun update(challengeAnswer: ChallengeAnswerEntity)

    @Delete
    suspend fun delete(challengeAnswer: ChallengeAnswerEntity)

    @Query("SELECT * FROM $TABLE_CHALLENGE_ANSWERS ORDER BY timestamp DESC")
    suspend fun getAllChallengeAnswers(): List<ChallengeAnswerEntity>

    @Query("SELECT * FROM $TABLE_CHALLENGE_ANSWERS  WHERE questionId = :questionId LIMIT 1")
    suspend fun getChallengeAnswerByQuestionId(questionId: String): ChallengeAnswerEntity?
} 
