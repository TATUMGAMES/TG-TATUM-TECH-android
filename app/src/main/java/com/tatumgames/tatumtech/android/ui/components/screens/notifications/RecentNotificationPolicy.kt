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
package com.tatumgames.tatumtech.android.ui.components.screens.notifications

import com.tatumgames.tatumtech.android.database.entity.RecentNotificationEntity
import com.tatumgames.tatumtech.android.enums.NotificationType

/**
 * Pure helpers for recent-notification identity, retention, and destination resolution.
 */
object RecentNotificationPolicy {
    /** Read notifications older than this are removed from Recent Notifications. */
    const val READ_RETENTION_MS: Long = 14L * 24L * 60L * 60L * 1000L

    fun codingChallengeDailyId(dayKey: String): String = "coding_challenge:daily:$dayKey"

    fun eventId(eventId: Long): String = "event:$eventId"

    fun careerSpotlightId(dayKey: String): String = "career:spotlight:$dayKey"

    fun communitySpotlightId(dayKey: String): String = "community:spotlight:$dayKey"

    fun gameSpotlightId(dayKey: String): String = "game:spotlight:$dayKey"

    /**
     * Keeps unread items and read items newer than [nowMillis] - [READ_RETENTION_MS].
     */
    fun shouldRetain(entity: RecentNotificationEntity, nowMillis: Long): Boolean {
        val readAt = entity.readAtMillis ?: return true
        return nowMillis - readAt <= READ_RETENTION_MS
    }

    fun destinationRoute(entity: RecentNotificationEntity): String {
        val type = NotificationType.fromStorage(entity.type)
        return entity.destinationRoute.ifBlank {
            type?.destinationRoute.orEmpty()
        }
    }
}
