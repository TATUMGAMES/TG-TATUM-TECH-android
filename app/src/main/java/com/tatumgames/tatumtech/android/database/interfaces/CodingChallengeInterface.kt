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

import com.tatumgames.tatumtech.android.database.entity.CodingChallengeEntity

/**
 * Interface defining coding challenge-related database operations.
 * 
 * Provides a contract for coding challenge answer management operations including
 * creation, updates, retrieval, and deletion of coding challenge answers.
 */
interface CodingChallengeInterface {

    /**
     * Insert a coding challenge answer into the database.
     * 
     * @param codingChallengeEntity The coding challenge entity to insert.
     * @return The ID of the inserted entity.
     */
    suspend fun insert(codingChallengeEntity: CodingChallengeEntity): Long

    /**
     * Update an existing coding challenge answer.
     * 
     * @param codingChallengeEntity The coding challenge entity to update.
     */
    suspend fun update(codingChallengeEntity: CodingChallengeEntity)

    /**
     * Delete a coding challenge answer from the database.
     * 
     * @param codingChallengeEntity The coding challenge entity to delete.
     */
    suspend fun delete(codingChallengeEntity: CodingChallengeEntity)

    /**
     * Get all coding challenge answers from the database.
     * 
     * @return List of all coding challenge answers.
     */
    suspend fun getAllChallengeAnswers(): List<CodingChallengeEntity>

    /**
     * Get a coding challenge answer by question ID.
     * 
     * @param questionId The question ID to search for.
     * @return The coding challenge entity if found, null otherwise.
     */
    suspend fun getChallengeAnswerByQuestionId(questionId: String): CodingChallengeEntity?
} 
