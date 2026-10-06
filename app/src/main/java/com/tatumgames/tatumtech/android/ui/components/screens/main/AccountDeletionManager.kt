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
import com.tatumgames.tatumtech.android.api.TatumTechApiProvider
import com.tatumgames.tatumtech.android.constants.Constants
import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.ui.components.screens.networking.ContactCardScanSession
import com.tatumgames.tatumtech.android.ui.components.screens.rating.RatingPromptKeys
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClient
import com.tatumgames.tatumtech.framework.android.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Deletes the Tatum Tech account and every piece of user-specific state on this device.
 *
 * Cleared: the Tatum Tech API session (signed out server-side when reachable), the signed-in
 * Google/Firebase identity, every Room table (user, demographics,
 * contact cards, connections, achievement counters, notifications, timeline, quiz progress and
 * answer history, event registrations, imported coding content — re-synced from assets on next
 * home entry), captured contact card photos, and in-memory scan state.
 *
 * Kept: whether this device was already sent to the store to rate the app, because that
 * rating belongs to the store account rather than the Tatum Tech account.
 */
object AccountDeletionManager {

    /** Runs to completion even if the calling screen leaves composition. */
    suspend fun deleteAccount(context: Context) = withContext(NonCancellable + Dispatchers.IO) {
        val appContext = context.applicationContext
        AnalyticsService.deleteAccount()

        TatumTechApiProvider.getSessionManager().signOut()

        val remoteDeleted = GoogleAuthClient.deleteAccount(appContext)
        if (!remoteDeleted) {
            Logger.e(TAG, "Remote account deletion failed; local data is still cleared")
        }

        clearLocalData(appContext)
    }

    private suspend fun clearLocalData(context: Context) {
        val database = AppDatabase.getInstance(context)
        val counters = database.engagementCounterDao()
        val sentToStore = counters.get(RatingPromptKeys.HAS_BEEN_SENT_TO_APP_STORE_FOR_RATING)

        database.clearAllTables()
        sentToStore?.let { counters.upsert(it) }

        File(context.cacheDir, Constants.CONTACT_CARD_IMAGES_DIR).deleteRecursively()
        ContactCardScanSession.clear()
    }
}
