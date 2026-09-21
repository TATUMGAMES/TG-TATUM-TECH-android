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
package com.tatumgames.tatumtech.android.ui.components.screens.timeline

import com.tatumgames.tatumtech.android.enums.TimelineFilter

/**
 * Rolling time windows for My Timeline filters (product convention):
 * TODAY = last 24h, WEEK = last 7×24h, MONTH = last 30×24h.
 */
object TimelineFilterWindow {
    fun fromTimestamp(filter: TimelineFilter, nowMillis: Long): Long = when (filter) {
        TimelineFilter.TODAY -> nowMillis - 24 * 60 * 60 * 1000L
        TimelineFilter.WEEK -> nowMillis - 7 * 24 * 60 * 60 * 1000L
        TimelineFilter.MONTH -> nowMillis - 30 * 24 * 60 * 60 * 1000L
    }
}
