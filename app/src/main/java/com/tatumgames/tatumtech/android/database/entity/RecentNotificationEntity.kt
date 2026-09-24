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

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_RECENT_NOTIFICATIONS

/**
 * Persisted recent notification with stable [id] tied to underlying content
 * (e.g. `coding_challenge:daily:2026-09-22`, `event:42`).
 *
 * @property readAtMillis null means unread; set when the user opens the notification.
 */
@Entity(tableName = TABLE_RECENT_NOTIFICATIONS)
data class RecentNotificationEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    @DrawableRes val iconResId: Int,
    val createdAtMillis: Long,
    val readAtMillis: Long? = null,
    val relatedContentId: String? = null,
    val destinationRoute: String
)
