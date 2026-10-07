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
import com.tatumgames.tatumtech.android.api.TatumTechApiProvider
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClient
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Ends the signed-in session on this device without touching the account or its data.
 *
 * Cleared on success: the Tatum Tech API session (signed out server-side first) and the signed-in
 * Google/Firebase identity, both of which [com.tatumgames.tatumtech.android.activity.AuthActivity]
 * treats as an existing session. Room data and the account itself are kept.
 */
object SignOutManager {

    /**
     * Runs to completion even if the calling screen leaves composition.
     *
     * @return `null` once signed out; the API failure otherwise, in which case nothing is cleared.
     */
    suspend fun signOut(context: Context): ApiError? = withContext(NonCancellable + Dispatchers.IO) {
        val failure = TatumTechApiProvider.getSessionManager().signOutOrFail()
        if (failure == null) {
            GoogleAuthClient.signOut(context.applicationContext)
        }
        failure
    }
}
