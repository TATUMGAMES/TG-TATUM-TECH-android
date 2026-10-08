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
package com.tatumgames.tatumtech.android.ui.components.screens.main

import android.content.Context
import com.tatumgames.tatumtech.android.analytics.AnalyticsService
import com.tatumgames.tatumtech.android.analytics.ProfileFields
import com.tatumgames.tatumtech.android.api.TatumTechApiProvider
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.repository.UserDatabaseRepository
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Saves the Profile screen: the names go to the Tatum Tech API (`updateUserProfile`), then the
 * names and email are stored in the local user. The API has no email field, so email stays local.
 */
object ProfileUpdateManager {

    /**
     * Runs to completion even if the calling screen leaves composition. `null` values are cleared
     * locally and left out of the API request.
     *
     * @return `null` once saved; the API failure otherwise, in which case nothing is stored.
     */
    suspend fun save(context: Context, firstName: String?, lastName: String?, email: String?): ApiError? =
        withContext(NonCancellable + Dispatchers.IO) {
            TatumTechApiProvider.getSessionManager().updateUserProfile(firstName, lastName)
                ?.let { return@withContext it }

            val userRepository = UserDatabaseRepository(AppDatabase.getInstance(context).userDao())
            val currentUser = userRepository.getCurrentUser() ?: return@withContext null
            val changedFields = buildList {
                if (currentUser.firstName != firstName) add(ProfileFields.FIRST_NAME)
                if (currentUser.lastName != lastName) add(ProfileFields.LAST_NAME)
                if (currentUser.email != email) add(ProfileFields.EMAIL)
            }
            userRepository.updateUser(currentUser.copy(firstName = firstName, lastName = lastName, email = email))
            changedFields.forEach(AnalyticsService::updateProfile)
            null
        }
}
