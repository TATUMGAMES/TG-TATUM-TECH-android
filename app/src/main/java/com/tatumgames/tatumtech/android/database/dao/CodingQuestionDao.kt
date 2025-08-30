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
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_CODING_QUESTIONS
import com.tatumgames.tatumtech.android.database.entity.CodingQuestionEntity

/**
 * Data Access Object for CodingQuestionEntity operations.
 * 
 * Provides methods to interact with the coding_questions table in the database.
 */
@Dao
interface CodingQuestionDao {
    
    /**
     * Insert a new question or replace if exists.
     * 
     * @param question The question entity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: CodingQuestionEntity): Long
    
    /**
     * Insert multiple questions.
     * 
     * @param questions List of question entities to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<CodingQuestionEntity>)
    
    /**
     * Get all questions from the database.
     * 
     * @return List of all questions.
     */
    @Query("SELECT * FROM $TABLE_CODING_QUESTIONS ORDER BY language, level, questionId")
    suspend fun getAllQuestions(): List<CodingQuestionEntity>
    
    /**
     * Get questions by language and level.
     * 
     * @param language Programming language.
     * @param level Difficulty level.
     * @return List of questions matching the criteria.
     */
    @Query("SELECT * FROM $TABLE_CODING_QUESTIONS WHERE language = :language AND level = :level ORDER BY questionId")
    suspend fun getQuestionsByLanguageAndLevel(language: String, level: String): List<CodingQuestionEntity>
    
    /**
     * Get questions by language.
     * 
     * @param language Programming language.
     * @return List of questions for the specified language.
     */
    @Query("SELECT * FROM $TABLE_CODING_QUESTIONS WHERE language = :language ORDER BY level, questionId")
    suspend fun getQuestionsByLanguage(language: String): List<CodingQuestionEntity>
    
    /**
     * Get the total count of questions in the database.
     * 
     * @return Number of questions.
     */
    @Query("SELECT COUNT(*) FROM $TABLE_CODING_QUESTIONS")
    suspend fun getQuestionCount(): Int
    
    /**
     * Check if a question exists by questionId.
     * 
     * @param questionId The question ID to check.
     * @return True if the question exists, false otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM $TABLE_CODING_QUESTIONS WHERE questionId = :questionId)")
    suspend fun questionExists(questionId: String): Boolean
}



