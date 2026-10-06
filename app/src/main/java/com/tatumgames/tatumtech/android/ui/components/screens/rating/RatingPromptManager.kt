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
package com.tatumgames.tatumtech.android.ui.components.screens.rating

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.tatumgames.tatumtech.android.BuildConfig
import com.tatumgames.tatumtech.android.constants.Constants
import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.EngagementCounterEntity
import com.tatumgames.tatumtech.android.database.repository.EngagementCounterDatabaseRepository
import com.tatumgames.tatumtech.framework.android.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Keys stored in the `engagement_counters` table for the rating prompt.
 * Not achievement tracking keys, so they never surface in achievement progress.
 */
object RatingPromptKeys {
    const val APP_OPEN_COUNT = "APP_OPEN_COUNT"

    /** 1 once the user has been sent to the store listing; never reset afterwards. */
    const val HAS_BEEN_SENT_TO_APP_STORE_FOR_RATING = "HAS_BEEN_SENT_TO_APP_STORE_FOR_RATING"
}

/**
 * Decides when to show the "Enjoying The Tatum Tech App?" rating screen and persists its state.
 */
object RatingPromptManager {

    const val APP_OPEN_PROMPT_INTERVAL = 20
    const val MIN_STORE_RATING = 4
    const val MAX_RATING = 5

    /** True on every [APP_OPEN_PROMPT_INTERVAL]th open while the user has not been sent to the store. */
    fun shouldPromptForAppOpen(openCount: Int, hasBeenSentToStore: Boolean): Boolean =
        !hasBeenSentToStore && openCount > 0 && openCount % APP_OPEN_PROMPT_INTERVAL == 0

    /** True only for the answer that moves today's count from below [limit] to [limit] or above. */
    fun reachedLimitThisAnswer(countBefore: Int, countAfter: Int, limit: Int): Boolean =
        countBefore < limit && countAfter >= limit

    fun shouldSendToStore(rating: Int): Boolean = rating >= MIN_STORE_RATING

    /**
     * Records one app open and reports whether the rating screen should be shown for it.
     * Call once per real launch of the main experience, never on recomposition or recreation.
     */
    suspend fun recordAppOpen(context: Context): Boolean = withContext(Dispatchers.IO) {
        val repository = repository(context)
        val count = repository.increment(RatingPromptKeys.APP_OPEN_COUNT)
        shouldPromptForAppOpen(count, hasBeenSentToStore(repository))
    }

    suspend fun isEligible(context: Context): Boolean = withContext(Dispatchers.IO) {
        !hasBeenSentToStore(repository(context))
    }

    suspend fun markSentToAppStore(context: Context) = withContext(Dispatchers.IO) {
        AppDatabase.getInstance(context).engagementCounterDao().upsert(
            EngagementCounterEntity(
                type = RatingPromptKeys.HAS_BEEN_SENT_TO_APP_STORE_FOR_RATING,
                count = 1
            )
        )
    }

    /** Opens the Play Store app on the listing, falling back to the web listing. */
    fun openAppStoreListing(context: Context) {
        val packageName = BuildConfig.STORE_PACKAGE_NAME
        val storeIntent = Intent(
            Intent.ACTION_VIEW,
            "${Constants.URI_PLAY_STORE_DETAILS}$packageName".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(storeIntent)
        } catch (e: ActivityNotFoundException) {
            Logger.d(TAG, "Play Store app unavailable, opening web listing: ${e.message}")
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "${Constants.URL_PLAY_STORE_DETAILS}$packageName".toUri()
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e: ActivityNotFoundException) {
                Logger.e(TAG, "No activity available to open the store listing: ${e.message}")
            }
        }
    }

    private suspend fun hasBeenSentToStore(repository: EngagementCounterDatabaseRepository) =
        repository.getCount(RatingPromptKeys.HAS_BEEN_SENT_TO_APP_STORE_FOR_RATING) > 0

    private fun repository(context: Context) =
        EngagementCounterDatabaseRepository(AppDatabase.getInstance(context).engagementCounterDao())
}
