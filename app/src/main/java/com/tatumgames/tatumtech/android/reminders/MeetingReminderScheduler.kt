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

import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.VirtualSpeaker

/**
 * Schedules the work that delivers one reminder. Implementations must treat [enqueue] for an
 * already pending key as a no-op.
 */
interface MeetingReminderWorkScheduler {
    fun enqueue(reminder: MeetingReminder, delayMillis: Long)
    fun cancel(key: String)
}

/** A reminder and how long from now it should be delivered (0 = immediately). */
data class ScheduledReminder(val reminder: MeetingReminder, val delayMillis: Long)

/**
 * Keeps scheduled reminders in sync with the latest event data.
 *
 * [scheduleUpcomingMeetings] is idempotent: it schedules every session that has not ended and
 * was not already delivered, delivers immediately when the reminder time has passed (e.g. the
 * app opens one minute before a talk), and cancels reminders for sessions that were removed or
 * moved.
 */
class MeetingReminderScheduler(
    private val store: MeetingReminderStore,
    private val workScheduler: MeetingReminderWorkScheduler,
    private val clock: () -> Long = System::currentTimeMillis
) {

    /** Call with a complete, successfully loaded event list; never with a failed or partial one. */
    fun scheduleUpcomingMeetings(events: List<Event>): List<ScheduledReminder> = synchronized(LOCK) {
        val now = clock()
        store.pruneFired(now)
        val planned = events
            .flatMap { event -> event.virtualSpeakers.mapNotNull { MeetingTimeResolver.resolve(event, it) } }
            .distinctBy { it.key }
            .filter { !it.hasEnded(now) && !store.hasFired(it.key) }
            .map { ScheduledReminder(it, (it.triggerAtMillis - now).coerceAtLeast(0L)) }
        val keys = planned.map { it.reminder.key }.toSet()

        (store.scheduledKeys() - keys).forEach(workScheduler::cancel)
        planned.forEach { workScheduler.enqueue(it.reminder, it.delayMillis) }
        store.replaceScheduledKeys(keys)
        planned
    }

    /**
     * Debug aid: a reminder for [speaker] delivered after [delayMillis], as if the session started
     * [MeetingReminder.LEAD_TIME_MS] later. Not tracked, so later syncs leave it alone.
     */
    fun scheduleTestReminder(
        eventId: String,
        eventName: String?,
        speaker: VirtualSpeaker,
        delayMillis: Long
    ): MeetingReminder {
        val now = clock()
        val start = now + delayMillis + MeetingReminder.LEAD_TIME_MS
        val reminder = MeetingReminder(
            key = "test|${MeetingReminder.key(eventId, speaker.id, start)}|$now",
            eventId = eventId,
            eventName = eventName,
            speakerId = speaker.id,
            speakerName = speaker.name,
            speakingTopic = speaker.speakingTopic,
            startMillis = start,
            endMillis = start + MeetingTimeResolver.DEFAULT_SESSION_MS
        )
        workScheduler.enqueue(reminder, delayMillis)
        return reminder
    }

    private companion object {
        /** Startup and screen refreshes may sync concurrently; they share one preferences file. */
        val LOCK = Any()
    }
}
