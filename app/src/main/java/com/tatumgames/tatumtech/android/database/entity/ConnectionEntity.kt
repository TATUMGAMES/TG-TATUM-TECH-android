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
package com.tatumgames.tatumtech.android.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_CONNECTIONS

/**
 * One-directional networking connection: [ownerUserId] saved [connectedCardId]'s contact card.
 * Does not imply mutual friendship, chat, or approval.
 */
@Entity(
    tableName = TABLE_CONNECTIONS,
    indices = [Index(value = ["ownerUserId", "connectedCardId"], unique = true)]
)
data class ConnectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerUserId: Long,
    val connectedCardId: String,
    val connectedUserId: Long? = null,
    val connectedAt: Long = System.currentTimeMillis(),
    val name: String,
    val jobTitle: String? = null,
    val company: String? = null,
    val description: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val alternateEmail: String? = null,
    val website: String? = null,
    val linkedin: String? = null,
    val twitter: String? = null,
    val customLink: String? = null,
    val calendly: String? = null
)
