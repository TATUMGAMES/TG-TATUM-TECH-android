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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_USERS

/**
 * Entity representing a user in the application.
 * 
 * This entity stores user information including anonymous ID, name components,
 * and email for proper attendee management and user identification.
 *
 * @property id Primary key for the user entity.
 * @property anonymousId Unique anonymous identifier for users without full names.
 * @property firstName User's first name.
 * @property lastName User's last name.
 * @property name Auto-combined full name (firstName + lastName).
 * @property email User's email address.
 */
@Entity(tableName = TABLE_USERS)
data class UserEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey val id: Long = 1, // Single user per app instance
    val anonymousId: String,
    val firstName: String?,
    val lastName: String?,
    val name: String,
    val email: String?
)
