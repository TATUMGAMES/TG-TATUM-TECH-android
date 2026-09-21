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

import com.tatumgames.tatumtech.android.database.dao.ConnectionDao
import com.tatumgames.tatumtech.android.database.entity.ConnectionEntity

/**
 * Result of attempting to save a scanned contact card as a connection.
 */
sealed class SaveConnectionResult {
    data class Created(val connection: ConnectionEntity) : SaveConnectionResult()
    data class Updated(val connection: ConnectionEntity) : SaveConnectionResult()
}

class ConnectionDatabaseRepository(
    private val connectionDao: ConnectionDao
) {
    suspend fun getConnection(ownerUserId: Long, connectedCardId: String): ConnectionEntity? =
        connectionDao.getConnection(ownerUserId, connectedCardId)

    suspend fun countForOwner(ownerUserId: Long): Int =
        connectionDao.countForOwner(ownerUserId)

    /**
     * Inserts a new connection or refreshes the snapshot when the same card is scanned again.
     * Returns whether this was a brand-new connection (for achievements/timeline).
     */
    suspend fun saveOrUpdate(connection: ConnectionEntity): SaveConnectionResult {
        val existing =
            connectionDao.getConnection(connection.ownerUserId, connection.connectedCardId)
        return if (existing == null) {
            val id = connectionDao.insert(connection)
            SaveConnectionResult.Created(connection.copy(id = id))
        } else {
            val updated = connection.copy(id = existing.id, connectedAt = existing.connectedAt)
            connectionDao.update(updated)
            SaveConnectionResult.Updated(updated)
        }
    }
}
