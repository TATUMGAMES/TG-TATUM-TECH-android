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
package com.tatumgames.tatumtech.android.reminders

import android.content.Intent
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes

/**
 * Where a reminder tap leads: the event's virtual speakers, scrolled to the speaker, whose Join
 * button opens the current Meet link. Carried as intent extras so it survives a cold start.
 */
data class MeetingReminderDestination(val eventId: String, val speakerId: String) {

    val route: String get() = NavRoutes.virtualSpeakersRoute(eventId, speakerId)

    fun writeTo(intent: Intent): Intent = intent
        .putExtra(EXTRA_EVENT_ID, eventId)
        .putExtra(EXTRA_SPEAKER_ID, speakerId)

    companion object {
        private const val EXTRA_EVENT_ID = "com.tatumgames.tatumtech.android.extra.REMINDER_EVENT_ID"
        private const val EXTRA_SPEAKER_ID = "com.tatumgames.tatumtech.android.extra.REMINDER_SPEAKER_ID"

        fun of(reminder: MeetingReminder) = MeetingReminderDestination(reminder.eventId, reminder.speakerId)

        fun readFrom(intent: Intent?): MeetingReminderDestination? {
            val eventId = intent?.getStringExtra(EXTRA_EVENT_ID)?.takeIf { it.isNotBlank() } ?: return null
            return MeetingReminderDestination(eventId, intent.getStringExtra(EXTRA_SPEAKER_ID).orEmpty())
        }
    }
}
