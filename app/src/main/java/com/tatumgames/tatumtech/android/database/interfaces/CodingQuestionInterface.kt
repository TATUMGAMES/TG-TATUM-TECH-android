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
package com.tatumgames.tatumtech.android.database.interfaces

import com.tatumgames.tatumtech.android.database.entity.CodingQuestionEntity

/**
 * Interface defining coding question-related database operations.
 * 
 * Provides a contract for coding question management operations including
 * creation, retrieval, and validation of coding questions.
 */
interface CodingQuestionInterface {
    
    /**
     * Insert a single question into the database.
     * 
     * @param question The question entity to insert.
     * @return The ID of the inserted question.
     */
    suspend fun insertQuestion(question: CodingQuestionEntity): Long
    
    /**
     * Insert multiple questions into the database.
     * 
     * @param questions List of question entities to insert.
     */
    suspend fun insertQuestions(questions: List<CodingQuestionEntity>)
    
    /**
     * Get all questions from the database.
     * 
     * @return List of all questions.
     */
    suspend fun getAllQuestions(): List<CodingQuestionEntity>
    
    /**
     * Get questions by language and level.
     * 
     * @param language Programming language.
     * @param level Difficulty level.
     * @return List of questions matching the criteria.
     */
    suspend fun getQuestionsByLanguageAndLevel(language: String, level: String): List<CodingQuestionEntity>
    
    /**
     * Get questions by language.
     * 
     * @param language Programming language.
     * @return List of questions for the specified language.
     */
    suspend fun getQuestionsByLanguage(language: String): List<CodingQuestionEntity>
    
    /**
     * Get the total count of questions in the database.
     * 
     * @return Number of questions.
     */
    suspend fun getQuestionCount(): Int
    
    /**
     * Check if a question exists by questionId.
     * 
     * @param questionId The question ID to check.
     * @return True if the question exists, false otherwise.
     */
    suspend fun questionExists(questionId: String): Boolean
}



