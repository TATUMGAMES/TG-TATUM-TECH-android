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

import android.content.Context
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.dao.CodingQuestionDao
import com.tatumgames.tatumtech.android.database.dao.RecentNotificationDao
import com.tatumgames.tatumtech.android.database.entity.RecentNotificationEntity
import com.tatumgames.tatumtech.android.enums.NotificationType
import com.tatumgames.tatumtech.android.ui.components.screens.notifications.RecentNotificationPolicy
import com.tatumgames.tatumtech.android.ui.utils.JsonImporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Creates, deduplicates, retains, and marks recent notifications.
 * Generation is content-keyed (stable ids) — not regenerated from display text.
 */
class RecentNotificationDatabaseRepository(
    private val notificationDao: RecentNotificationDao,
    private val codingQuestionDao: CodingQuestionDao
) {

    /**
     * Syncs candidates from app content, purges obsolete/expired rows, returns display list.
     */
    suspend fun refreshAndLoad(context: Context, nowMillis: Long = System.currentTimeMillis()):
        List<RecentNotificationEntity> {
        notificationDao.deleteByType("EventRegistration")
        notificationDao.deleteByType("EVENT_REGISTRATION")

        val dayKey = dayKey(nowMillis)
        ensureCodingChallengeNotification(context, dayKey, nowMillis)
        ensureEventNotifications(context, nowMillis)
        ensureSpotlightNotifications(context, dayKey, nowMillis)

        val cutoff = nowMillis - RecentNotificationPolicy.READ_RETENTION_MS
        notificationDao.deleteReadOlderThan(cutoff)

        return notificationDao.getAll().filter {
            RecentNotificationPolicy.shouldRetain(it, nowMillis)
        }
    }

    suspend fun markRead(id: String, nowMillis: Long = System.currentTimeMillis()) {
        notificationDao.markReadIfUnread(id, nowMillis)
    }

    suspend fun getById(id: String): RecentNotificationEntity? = notificationDao.getById(id)

    private suspend fun ensureCodingChallengeNotification(
        context: Context,
        dayKey: String,
        nowMillis: Long
    ) {
        if (codingQuestionDao.getQuestionCount() <= 0) return
        val type = NotificationType.CODING_CHALLENGE
        notificationDao.insertIgnore(
            RecentNotificationEntity(
                id = RecentNotificationPolicy.codingChallengeDailyId(dayKey),
                type = type.name,
                title = context.getString(R.string.coding_challenge),
                description = context.getString(R.string.new_coding_challenge_available),
                iconResId = R.drawable.notif_coding_challenge,
                createdAtMillis = nowMillis,
                relatedContentId = dayKey,
                destinationRoute = type.destinationRoute
            )
        )
    }

    private suspend fun ensureEventNotifications(context: Context, nowMillis: Long) {
        val type = NotificationType.EVENT
        JsonImporter.loadUpcomingEvents(context).take(3).forEach { event ->
            notificationDao.insertIgnore(
                RecentNotificationEntity(
                    id = RecentNotificationPolicy.eventId(event.id),
                    type = type.name,
                    title = context.getString(R.string.upcoming_events),
                    description = event.name,
                    iconResId = R.drawable.upcoming_events,
                    createdAtMillis = nowMillis,
                    relatedContentId = event.id.toString(),
                    destinationRoute = type.destinationRoute
                )
            )
        }
    }

    private suspend fun ensureSpotlightNotifications(
        context: Context,
        dayKey: String,
        nowMillis: Long
    ) {
        // One soft daily spotlight per section so Career/Community/Games remain actionable
        // without inventing fake per-item feeds. Stable id prevents duplicates on refresh.
        insertSpotlight(
            id = RecentNotificationPolicy.careerSpotlightId(dayKey),
            type = NotificationType.CAREER,
            title = context.getString(R.string.apply_for_jobs),
            description = context.getString(R.string.notif_career_spotlight),
            iconResId = R.drawable.jobs,
            nowMillis = nowMillis
        )
        insertSpotlight(
            id = RecentNotificationPolicy.communitySpotlightId(dayKey),
            type = NotificationType.COMMUNITY,
            title = context.getString(R.string.community),
            description = context.getString(R.string.notif_community_spotlight),
            iconResId = R.drawable.community,
            nowMillis = nowMillis
        )
        insertSpotlight(
            id = RecentNotificationPolicy.gameSpotlightId(dayKey),
            type = NotificationType.GAME,
            title = context.getString(R.string.discover),
            description = context.getString(R.string.notif_game_spotlight),
            iconResId = R.drawable.games,
            nowMillis = nowMillis
        )
    }

    private suspend fun insertSpotlight(
        id: String,
        type: NotificationType,
        title: String,
        description: String,
        iconResId: Int,
        nowMillis: Long
    ) {
        notificationDao.insertIgnore(
            RecentNotificationEntity(
                id = id,
                type = type.name,
                title = title,
                description = description,
                iconResId = iconResId,
                createdAtMillis = nowMillis,
                destinationRoute = type.destinationRoute
            )
        )
    }

    private fun dayKey(nowMillis: Long): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        fmt.timeZone = TimeZone.getDefault()
        return fmt.format(Date(nowMillis))
    }
}
