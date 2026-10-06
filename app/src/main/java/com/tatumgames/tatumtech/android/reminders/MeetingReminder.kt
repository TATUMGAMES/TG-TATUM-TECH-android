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

/**
 * A virtual speaker session that produces a reminder, normalized from the event and speaker
 * models (API or local JSON alike).
 *
 * @param key Stable identity built from event, speaker, and start time; a moved session gets a
 * new key, so its old reminder is cancelled and a new one scheduled.
 * @param startMillis Session start as an absolute instant (epoch milliseconds).
 */
data class MeetingReminder(
    val key: String,
    val eventId: String,
    val eventName: String?,
    val speakerId: String,
    val speakerName: String,
    val speakingTopic: String,
    val startMillis: Long,
    val endMillis: Long
) {
    val triggerAtMillis: Long get() = startMillis - LEAD_TIME_MS

    fun hasEnded(nowMillis: Long): Boolean = nowMillis >= endMillis

    /** Whole minutes until the start, rounded up; 0 once the session has started. */
    fun minutesUntilStart(nowMillis: Long): Int {
        val remaining = startMillis - nowMillis
        return if (remaining <= 0) 0 else ((remaining + MINUTE_MS - 1) / MINUTE_MS).toInt()
    }

    companion object {
        /** How long before the start the reminder is delivered. */
        const val LEAD_TIME_MS: Long = 2 * 60_000L

        private const val MINUTE_MS = 60_000L

        fun key(eventId: String, speakerId: String, startMillis: Long): String =
            "$eventId|$speakerId|$startMillis"
    }
}
