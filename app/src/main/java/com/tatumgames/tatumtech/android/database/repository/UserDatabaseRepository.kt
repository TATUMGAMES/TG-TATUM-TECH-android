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

import com.tatumgames.tatumtech.android.database.dao.UserDao
import com.tatumgames.tatumtech.android.database.entity.UserEntity
import com.tatumgames.tatumtech.android.database.interfaces.UserInterface

/**
 * Repository implementation for user database operations.
 * 
 * Provides concrete implementation of UserInterface for managing
 * user data in the local database.
 *
 * @property userDao The data access object for user operations.
 */
class UserDatabaseRepository(
    private val userDao: UserDao
) : UserInterface {
    
    /**
     * Insert or update a user in the database.
     * 
     * @param user The user entity to save.
     */
    override suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }
    
    /**
     * Update an existing user in the database.
     * 
     * @param user The user entity to update.
     */
    override suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }
    
    /**
     * Get the current user from the database.
     * 
     * @return The current user entity or null if not found.
     */
    override suspend fun getCurrentUser(): UserEntity? {
        return userDao.getCurrentUser()
    }
    
    /**
     * Check if a user exists in the database.
     * 
     * @return True if a user exists, false otherwise.
     */
    override suspend fun userExists(): Boolean {
        return userDao.userExists() > 0
    }
}
