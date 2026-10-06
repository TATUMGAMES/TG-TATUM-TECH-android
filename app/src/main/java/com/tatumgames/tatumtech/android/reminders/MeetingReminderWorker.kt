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
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * Delivers one reminder when its delay elapses. WorkManager persists pending work and restores
 * it after reboots and process death, so no service or boot receiver of our own is needed.
 */
class MeetingReminderWorker(
    context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val reminder = inputData.toMeetingReminder() ?: return Result.failure()
        MeetingReminders.coordinator(applicationContext).deliver(reminder)
        return Result.success()
    }

    internal companion object {
        private const val KEY = "key"
        private const val EVENT_ID = "event_id"
        private const val EVENT_NAME = "event_name"
        private const val SPEAKER_ID = "speaker_id"
        private const val SPEAKER_NAME = "speaker_name"
        private const val SPEAKING_TOPIC = "speaking_topic"
        private const val START_MILLIS = "start_millis"
        private const val END_MILLIS = "end_millis"

        fun inputData(reminder: MeetingReminder): Data = workDataOf(
            KEY to reminder.key,
            EVENT_ID to reminder.eventId,
            EVENT_NAME to reminder.eventName,
            SPEAKER_ID to reminder.speakerId,
            SPEAKER_NAME to reminder.speakerName,
            SPEAKING_TOPIC to reminder.speakingTopic,
            START_MILLIS to reminder.startMillis,
            END_MILLIS to reminder.endMillis
        )

        private fun Data.toMeetingReminder(): MeetingReminder? {
            val key = getString(KEY) ?: return null
            val eventId = getString(EVENT_ID) ?: return null
            val speakerId = getString(SPEAKER_ID) ?: return null
            val startMillis = getLong(START_MILLIS, -1L).takeIf { it >= 0 } ?: return null
            return MeetingReminder(
                key = key,
                eventId = eventId,
                eventName = getString(EVENT_NAME),
                speakerId = speakerId,
                speakerName = getString(SPEAKER_NAME).orEmpty(),
                speakingTopic = getString(SPEAKING_TOPIC).orEmpty(),
                startMillis = startMillis,
                endMillis = getLong(END_MILLIS, startMillis + MeetingTimeResolver.DEFAULT_SESSION_MS)
            )
        }
    }
}

/** One unique WorkManager request per reminder key, so re-enqueueing never duplicates. */
class WorkManagerMeetingReminderScheduler(context: Context) : MeetingReminderWorkScheduler {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun enqueue(reminder: MeetingReminder, delayMillis: Long) {
        val request = OneTimeWorkRequestBuilder<MeetingReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(MeetingReminderWorker.inputData(reminder))
            .addTag(WORK_TAG)
            .build()
        workManager.enqueueUniqueWork(uniqueName(reminder.key), ExistingWorkPolicy.KEEP, request)
    }

    override fun cancel(key: String) {
        workManager.cancelUniqueWork(uniqueName(key))
    }

    private fun uniqueName(key: String) = "$WORK_TAG:$key"

    private companion object {
        const val WORK_TAG = "meeting_reminder"
    }
}
