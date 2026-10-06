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

import android.content.Context
import com.tatumgames.tatumtech.android.data.content.TatumTechContentRepository
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/** Production wiring for the reminder components. Holds no state. */
object MeetingReminders {

    fun store(context: Context): MeetingReminderStore = SharedPreferencesMeetingReminderStore(context)

    fun scheduler(context: Context) = MeetingReminderScheduler(
        store = store(context),
        workScheduler = WorkManagerMeetingReminderScheduler(context)
    )

    fun coordinator(context: Context) = MeetingReminderCoordinator(
        store = store(context),
        isAppInForeground = { AppForegroundTracker.isInForeground },
        showBanner = MeetingReminderBanners::show,
        postNotification = MeetingReminderNotifier(context)::show
    )

    /**
     * Brings scheduled reminders in line with freshly loaded [events]. Call only with a
     * successful load; an empty list cancels every pending reminder.
     */
    suspend fun sync(context: Context, events: List<Event>) {
        withContext(Dispatchers.IO) { scheduler(context).scheduleUpcomingMeetings(events) }
    }

    /** Loads upcoming events (API or bundled JSON) and syncs; a failed load changes nothing. */
    suspend fun syncFromRepository(context: Context, repository: TatumTechContentRepository) {
        repository.getUpcomingEvents().getOrNull()?.let { sync(context, it) }
    }
}

/**
 * The reminder the in-app banner shows, or `null`. A process-wide singleton because the worker
 * that produces reminders and the activity that displays them share nothing but the process;
 * it holds plain data, never a Context.
 */
object MeetingReminderBanners {

    private val _current = MutableStateFlow<MeetingReminder?>(null)
    val current: StateFlow<MeetingReminder?> = _current.asStateFlow()

    fun show(reminder: MeetingReminder) {
        _current.value = reminder
    }

    /** Hides [reminder] unless a newer one replaced it meanwhile. */
    fun dismiss(reminder: MeetingReminder) {
        _current.compareAndSet(reminder, null)
    }
}
