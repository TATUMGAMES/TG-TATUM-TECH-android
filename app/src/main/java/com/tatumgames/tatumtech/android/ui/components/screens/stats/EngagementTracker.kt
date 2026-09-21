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
package com.tatumgames.tatumtech.android.ui.components.screens.stats

import android.content.Context
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.repository.EngagementCounterDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.enums.TimelineType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Records user engagement actions used by the achievement system.
 * Does not write Timeline rows (avoids timeline spam).
 */
object EngagementTracker {
    suspend fun record(context: Context, trackingKey: String) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            EngagementCounterDatabaseRepository(db.engagementCounterDao())
                .increment(trackingKey)
        }
    }

    suspend fun buildProgressCounts(context: Context): AchievementProgressCounts {
        return withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val engagement = EngagementCounterDatabaseRepository(db.engagementCounterDao())
                .getAllCounts()
                .toMutableMap()
            val timeline = TimelineDatabaseRepository(db.timelineDao()).getAllTimelineEvents()
            engagement[AchievementTrackingKeys.CHALLENGE_COMPLETION] =
                timeline.count { it.type == TimelineType.CHALLENGE_COMPLETION.typeValue }
            engagement[AchievementTrackingKeys.QR_SCAN] =
                timeline.count { it.type == TimelineType.QR_SCAN.typeValue }
            engagement[AchievementTrackingKeys.CONNECTION_MADE] =
                timeline.count {
                    it.type == TimelineType.CONNECTION_MADE.typeValue ||
                            it.type == TimelineType.FRIEND_ADD.typeValue
                }
            engagement[AchievementTrackingKeys.DONATION] =
                timeline.count { it.type == TimelineType.DONATION.typeValue }
            AchievementProgressCounts(engagement)
        }
    }
}
