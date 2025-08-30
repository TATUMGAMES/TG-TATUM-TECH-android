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

import com.tatumgames.tatumtech.android.database.entity.UserEntity

/**
 * Interface defining user-related database operations.
 * 
 * Provides a contract for user management operations including
 * creation, updates, and retrieval of user information.
 */
interface UserInterface {
    
    /**
     * Insert or update a user in the database.
     * 
     * @param user The user entity to save.
     */
    suspend fun insertUser(user: UserEntity)
    
    /**
     * Update an existing user in the database.
     * 
     * @param user The user entity to update.
     */
    suspend fun updateUser(user: UserEntity)
    
    /**
     * Get the current user from the database.
     * 
     * @return The current user entity or null if not found.
     */
    suspend fun getCurrentUser(): UserEntity?
    
    /**
     * Check if a user exists in the database.
     * 
     * @return True if a user exists, false otherwise.
     */
    suspend fun userExists(): Boolean
}
