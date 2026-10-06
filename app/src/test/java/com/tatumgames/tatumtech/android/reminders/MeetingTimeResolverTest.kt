package com.tatumgames.tatumtech.android.reminders

import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.VirtualSpeaker
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.util.TimeZone

class MeetingTimeResolverTest {

    private val originalZone = TimeZone.getDefault()

    @After
    fun restoreZone() {
        TimeZone.setDefault(originalZone)
    }

    private fun event(date: String = "2026-10-10T11:30:00Z") = Event(
        id = "1",
        name = "Tatum Tech Fall 2026",
        host = "Tatum Games",
        date = date,
        durationHours = 6,
        location = "Online",
        featuredImage = ""
    )

    private fun speaker(
        startTime: String? = "2:30 PM",
        endTime: String? = "3:00 PM",
        timeZone: String? = "America/Los_Angeles"
    ) = VirtualSpeaker(
        id = "jeff",
        name = "Jeff",
        speakingTopic = "Building games",
        startTime = startTime,
        endTime = endTime,
        timeZone = timeZone
    )

    private fun millis(instant: String) = Instant.parse(instant).toEpochMilli()

    @Test
    fun `speaker wall clock time is read in the speaker zone during daylight saving`() {
        val reminder = MeetingTimeResolver.resolve(event(), speaker())!!

        assertEquals(millis("2026-10-10T21:30:00Z"), reminder.startMillis)
        assertEquals(millis("2026-10-10T22:00:00Z"), reminder.endMillis)
        assertEquals(millis("2026-10-10T21:28:00Z"), reminder.triggerAtMillis)
        assertEquals("1|jeff|${reminder.startMillis}", reminder.key)
        assertEquals("Tatum Tech Fall 2026", reminder.eventName)
    }

    @Test
    fun `standard time shifts the instant by an hour`() {
        val reminder = MeetingTimeResolver.resolve(event("2026-12-05T18:00:00Z"), speaker())!!

        assertEquals(millis("2026-12-05T22:30:00Z"), reminder.startMillis)
    }

    @Test
    fun `device time zone does not change the meeting instant`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
        val inTokyo = MeetingTimeResolver.resolve(event(), speaker())!!
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))
        val inLondon = MeetingTimeResolver.resolve(event(), speaker())!!

        assertEquals(millis("2026-10-10T21:30:00Z"), inTokyo.startMillis)
        assertEquals(inTokyo, inLondon)
    }

    @Test
    fun `event date is taken as the calendar day in the speaker zone`() {
        // 03:00Z on Oct 11 is still Oct 10 in Los Angeles.
        val reminder = MeetingTimeResolver.resolve(event("2026-10-11T03:00:00Z"), speaker())!!

        assertEquals(millis("2026-10-10T21:30:00Z"), reminder.startMillis)
    }

    @Test
    fun `date without offset and 24 hour times are accepted`() {
        val reminder = MeetingTimeResolver.resolve(
            event("2026-10-10"),
            speaker(startTime = "14:30", endTime = "2:45pm")
        )!!

        assertEquals(millis("2026-10-10T21:30:00Z"), reminder.startMillis)
        assertEquals(millis("2026-10-10T21:45:00Z"), reminder.endMillis)
    }

    @Test
    fun `end time past midnight rolls over to the next day`() {
        val reminder = MeetingTimeResolver.resolve(event(), speaker("11:45 PM", "12:15 AM"))!!

        assertEquals(30 * 60_000L, reminder.endMillis - reminder.startMillis)
    }

    @Test
    fun `missing end time uses the default session length`() {
        val reminder = MeetingTimeResolver.resolve(event(), speaker(endTime = null))!!

        assertEquals(MeetingTimeResolver.DEFAULT_SESSION_MS, reminder.endMillis - reminder.startMillis)
    }

    @Test
    fun `speakers without usable schedule data produce no reminder`() {
        assertNull(MeetingTimeResolver.resolve(event(), speaker(timeZone = null)))
        assertNull(MeetingTimeResolver.resolve(event(), speaker(timeZone = "Mars/Olympus")))
        assertNull(MeetingTimeResolver.resolve(event(), speaker(startTime = null)))
        assertNull(MeetingTimeResolver.resolve(event(), speaker(startTime = "soon")))
        assertNull(MeetingTimeResolver.resolve(event(""), speaker()))
    }

    @Test
    fun `minutes until start round up and stop at zero`() {
        val reminder = MeetingTimeResolver.resolve(event(), speaker())!!

        assertEquals(2, reminder.minutesUntilStart(reminder.triggerAtMillis))
        assertEquals(1, reminder.minutesUntilStart(reminder.startMillis - 1))
        assertEquals(0, reminder.minutesUntilStart(reminder.startMillis))
        assertEquals(0, reminder.minutesUntilStart(reminder.startMillis + 60_000L))
    }
}
