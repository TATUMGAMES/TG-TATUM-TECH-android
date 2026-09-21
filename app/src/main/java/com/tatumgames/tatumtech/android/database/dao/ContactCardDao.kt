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
import com.tatumgames.tatumtech.android.database.entity.ContactCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactCardDao {
    @Query("SELECT * FROM contact_cards WHERE ownerUserId = :ownerUserId LIMIT 1")
    suspend fun getByOwnerUserId(ownerUserId: Long): ContactCardEntity?

    @Query("SELECT * FROM contact_cards WHERE ownerUserId = :ownerUserId LIMIT 1")
    fun observeByOwnerUserId(ownerUserId: Long): Flow<ContactCardEntity?>

    @Query("SELECT * FROM contact_cards WHERE cardId = :cardId LIMIT 1")
    suspend fun getByCardId(cardId: String): ContactCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(card: ContactCardEntity)
}
