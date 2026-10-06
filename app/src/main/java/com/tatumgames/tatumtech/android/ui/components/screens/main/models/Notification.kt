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
package com.tatumgames.tatumtech.android.ui.components.screens.main.models

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.tatumgames.tatumtech.android.enums.NotificationType

/**
 * UI model for a recent notification row.
 *
 * @property id Stable content-keyed identity (not display text).
 * @property isUnread True when the notification has not been opened yet.
 * @property destinationRoute Navigation target resolved from [type] / persisted route.
 */
data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val description: String,
    val isUnread: Boolean,
    val destinationRoute: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconResId: Int? = null,
    val relatedContentId: String? = null
)
