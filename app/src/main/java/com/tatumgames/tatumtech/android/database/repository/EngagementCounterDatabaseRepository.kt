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

import com.tatumgames.tatumtech.android.database.dao.EngagementCounterDao
import com.tatumgames.tatumtech.android.database.entity.EngagementCounterEntity

class EngagementCounterDatabaseRepository(
    private val dao: EngagementCounterDao
) {
    suspend fun getCount(type: String): Int = dao.getCount(type) ?: 0

    suspend fun increment(type: String): Int {
        val next = getCount(type) + 1
        dao.upsert(EngagementCounterEntity(type = type, count = next))
        return next
    }

    suspend fun getAllCounts(): Map<String, Int> =
        dao.getAll().associate { it.type to it.count }
}
