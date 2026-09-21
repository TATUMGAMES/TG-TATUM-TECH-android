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

import com.tatumgames.tatumtech.android.database.dao.CodingChallengeDao
import com.tatumgames.tatumtech.android.database.entity.CodingChallengeEntity
import com.tatumgames.tatumtech.android.database.interfaces.CodingChallengeInterface

class CodingChallengeDatabaseRepository(
    private val codingChallengeDao: CodingChallengeDao
) : CodingChallengeInterface {

    override suspend fun insert(codingChallengeEntity: CodingChallengeEntity): Long =
        codingChallengeDao.insert(codingChallengeEntity)

    override suspend fun update(codingChallengeEntity: CodingChallengeEntity) =
        codingChallengeDao.update(codingChallengeEntity)

    override suspend fun delete(codingChallengeEntity: CodingChallengeEntity) =
        codingChallengeDao.delete(codingChallengeEntity)

    override suspend fun getAllChallengeAnswers(): List<CodingChallengeEntity> =
        codingChallengeDao.getAllChallengeAnswers()

    override suspend fun getChallengeAnswerByQuestionId(questionId: String): CodingChallengeEntity? =
        codingChallengeDao.getChallengeAnswerByQuestionId(questionId)
}
