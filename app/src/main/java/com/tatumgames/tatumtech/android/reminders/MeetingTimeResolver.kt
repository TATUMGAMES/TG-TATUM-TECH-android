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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

/**
 * Turns a speaker's wall-clock schedule into absolute instants.
 *
 * A speaker carries only clock times (`"2:30 PM"`) and an IANA zone (`"America/Los_Angeles"`);
 * the calendar date is the event's date as seen in that zone. Combining date, time, and zone
 * (never the device's zone) keeps the instant correct across daylight saving time and for users
 * in or traveling to other time zones.
 */
object MeetingTimeResolver {

    /** Used when a speaker has no usable end time. */
    const val DEFAULT_SESSION_MS: Long = 30 * 60_000L

    private val TIME_FORMATS: List<DateTimeFormatter> = listOf("h:mm a", "h:mma", "H:mm").map {
        DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(it).toFormatter(Locale.US)
    }

    /** @return `null` when the speaker has no parseable start time, time zone, or event date. */
    fun resolve(event: Event, speaker: VirtualSpeaker): MeetingReminder? {
        val zone = parseZone(speaker.timeZone) ?: return null
        val date = eventDate(event.date, zone) ?: return null
        val startTime = parseTime(speaker.startTime) ?: return null
        val start = ZonedDateTime.of(date, startTime, zone)
        val end = parseTime(speaker.endTime)
            ?.let { ZonedDateTime.of(date, it, zone) }
            ?.let { if (it.isAfter(start)) it else it.plusDays(1) }
        val startMillis = start.toInstant().toEpochMilli()
        val endMillis = end?.toInstant()?.toEpochMilli() ?: (startMillis + DEFAULT_SESSION_MS)
        return MeetingReminder(
            key = MeetingReminder.key(event.id, speaker.id, startMillis),
            eventId = event.id,
            eventName = event.name.takeIf { it.isNotBlank() },
            speakerId = speaker.id,
            speakerName = speaker.name,
            speakingTopic = speaker.speakingTopic,
            startMillis = startMillis,
            endMillis = endMillis
        )
    }

    private fun parseZone(value: String?): ZoneId? =
        value?.trim()?.takeIf { it.isNotEmpty() }?.let { runCatching { ZoneId.of(it) }.getOrNull() }

    /** An offset timestamp is converted to [zone]; one without offset is taken as local already. */
    private fun eventDate(value: String, zone: ZoneId): LocalDate? {
        val text = value.trim().takeIf { it.isNotEmpty() } ?: return null
        return runCatching { OffsetDateTime.parse(text).atZoneSameInstant(zone).toLocalDate() }
            .recoverCatching { LocalDateTime.parse(text).toLocalDate() }
            .recoverCatching { LocalDate.parse(text) }
            .getOrNull()
    }

    private fun parseTime(value: String?): LocalTime? {
        val text = value
            ?.replace('\u202F', ' ')
            ?.replace('\u00A0', ' ')
            ?.trim()
            ?.replace(Regex("\\s+"), " ")
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        return TIME_FORMATS.firstNotNullOfOrNull { format ->
            runCatching { LocalTime.parse(text, format) }.getOrNull()
        }
    }
}
