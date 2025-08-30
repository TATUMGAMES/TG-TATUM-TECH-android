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
package com.tatumgames.tatumtech.android.database.repository

import com.tatumgames.tatumtech.android.database.dao.CodingQuestionDao
import com.tatumgames.tatumtech.android.database.entity.CodingQuestionEntity
import com.tatumgames.tatumtech.android.database.interfaces.CodingQuestionInterface

/**
 * Repository for coding question database operations.
 * 
 * Provides a clean interface for accessing coding question data from the database.
 */
class CodingQuestionDatabaseRepository(
    private val dao: CodingQuestionDao
) : CodingQuestionInterface {

    override suspend fun insertQuestion(question: CodingQuestionEntity): Long =
        dao.insertQuestion(question)

    override suspend fun insertQuestions(questions: List<CodingQuestionEntity>) =
        dao.insertQuestions(questions)

    override suspend fun getAllQuestions(): List<CodingQuestionEntity> =
        dao.getAllQuestions()

    override suspend fun getQuestionsByLanguageAndLevel(language: String, level: String): List<CodingQuestionEntity> =
        dao.getQuestionsByLanguageAndLevel(language, level)

    override suspend fun getQuestionsByLanguage(language: String): List<CodingQuestionEntity> =
        dao.getQuestionsByLanguage(language)

    override suspend fun getQuestionCount(): Int =
        dao.getQuestionCount()

    override suspend fun questionExists(questionId: String): Boolean =
        dao.questionExists(questionId)
}



