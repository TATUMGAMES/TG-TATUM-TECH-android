package com.tatumgames.tatumtech.android.reminders

import com.tatumgames.tatumtech.android.api.TatumTechApiClient
import com.tatumgames.tatumtech.android.api.TatumTechClientConfiguration
import com.tatumgames.tatumtech.android.api.TatumTechDataSourceMode
import com.tatumgames.tatumtech.android.api.TatumTechDataSources
import com.tatumgames.tatumtech.android.api.TatumTechEnvironment
import com.tatumgames.tatumtech.android.api.local.LocalJsonAssetSource
import com.tatumgames.tatumtech.android.data.content.TatumTechContentRepository
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.VirtualSpeaker
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequest
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.executor.HttpResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.Instant

class MeetingReminderSchedulerTest {

    private val start = Instant.parse("2026-10-10T21:30:00Z").toEpochMilli()
    private var now = start - 2 * HOUR

    private val store = FakeReminderStore()
    private val work = FakeWorkScheduler()
    private val scheduler = MeetingReminderScheduler(store, work) { now }

    private fun speaker(id: String = "jeff", startTime: String = "2:30 PM", endTime: String = "3:00 PM") =
        VirtualSpeaker(
            id = id,
            name = id,
            speakingTopic = "Topic",
            startTime = startTime,
            endTime = endTime,
            timeZone = "America/Los_Angeles"
        )

    private fun event(vararg speakers: VirtualSpeaker) = Event(
        id = "1",
        name = "Tatum Tech Fall 2026",
        host = "Tatum Games",
        date = "2026-10-10T11:30:00Z",
        durationHours = 6,
        location = "Online",
        featuredImage = "",
        virtualSpeakers = speakers.toList()
    )

    @Test
    fun `upcoming session is scheduled two minutes before it starts`() {
        val scheduled = scheduler.scheduleUpcomingMeetings(listOf(event(speaker())))

        val only = scheduled.single()
        assertEquals(2 * HOUR - 2 * MINUTE, only.delayMillis)
        assertEquals(mapOf(only.reminder.key to only.delayMillis), work.pending)
        assertEquals(setOf(only.reminder.key), store.scheduledKeys())
    }

    @Test
    fun `opening the app inside the reminder window delivers immediately`() {
        now = start - MINUTE

        assertEquals(0L, scheduler.scheduleUpcomingMeetings(listOf(event(speaker()))).single().delayMillis)
    }

    @Test
    fun `session in progress is still delivered but an ended one is ignored`() {
        now = start + 10 * MINUTE
        assertEquals(0L, scheduler.scheduleUpcomingMeetings(listOf(event(speaker()))).single().delayMillis)

        now = start + 30 * MINUTE
        assertTrue(scheduler.scheduleUpcomingMeetings(listOf(event(speaker()))).isEmpty())
        assertTrue(work.pending.isEmpty())
    }

    @Test
    fun `repeated syncs are idempotent`() {
        val events = listOf(event(speaker("jeff"), speaker("reggie", "3:30 PM", "4:00 PM")))

        val first = scheduler.scheduleUpcomingMeetings(events)
        val second = scheduler.scheduleUpcomingMeetings(events)

        assertEquals(first, second)
        assertEquals(2, work.pending.size)
        assertTrue(work.cancelled.isEmpty())
    }

    @Test
    fun `moved session cancels the old reminder and schedules the new time`() {
        val original = scheduler.scheduleUpcomingMeetings(listOf(event(speaker()))).single().reminder

        val moved = scheduler.scheduleUpcomingMeetings(listOf(event(speaker(startTime = "2:45 PM"))))
            .single().reminder

        assertEquals(listOf(original.key), work.cancelled)
        assertEquals(setOf(moved.key), work.pending.keys)
        assertEquals(start + 15 * MINUTE, moved.startMillis)
    }

    @Test
    fun `removed speaker or event cancels its reminder`() {
        val keys = scheduler.scheduleUpcomingMeetings(
            listOf(event(speaker("jeff"), speaker("reggie", "3:30 PM", "4:00 PM")))
        ).map { it.reminder.key }

        scheduler.scheduleUpcomingMeetings(listOf(event(speaker("jeff"))))
        assertEquals(listOf(keys[1]), work.cancelled)

        scheduler.scheduleUpcomingMeetings(emptyList())
        assertEquals(keys.toSet(), work.cancelled.toSet())
        assertTrue(work.pending.isEmpty())
        assertTrue(store.scheduledKeys().isEmpty())
    }

    @Test
    fun `delivered reminder is not scheduled again`() {
        val reminder = scheduler.scheduleUpcomingMeetings(listOf(event(speaker()))).single().reminder
        store.markFired(reminder.key, reminder.endMillis)
        now = start - MINUTE

        assertTrue(scheduler.scheduleUpcomingMeetings(listOf(event(speaker()))).isEmpty())
    }

    @Test
    fun `speakers without a usable schedule are skipped`() {
        val noZone = speaker("nozone").copy(timeZone = null)

        val scheduled = scheduler.scheduleUpcomingMeetings(listOf(event(speaker(), noZone)))

        assertEquals(listOf("jeff"), scheduled.map { it.reminder.speakerId })
    }

    @Test
    fun `test reminder fires after the requested delay and survives syncs`() {
        val reminder = scheduler.scheduleTestReminder("1", null, speaker(), 10_000L)

        assertEquals(10_000L, work.pending[reminder.key])
        assertEquals(2, reminder.minutesUntilStart(now + 10_000L))

        scheduler.scheduleUpcomingMeetings(emptyList())
        assertTrue(reminder.key !in work.cancelled)
    }

    @Test
    fun `bundled local JSON events produce reminders in the speaker zone`() = runBlocking {
        val assets = LocalJsonAssetSource { File("src/main/assets/$it").readText(Charsets.UTF_8) }
        val client = TatumTechDataSources.createClient(configuration(TatumTechDataSourceMode.LOCAL_JSON), assets)
        val events = TatumTechContentRepository { client }.getUpcomingEvents().getOrNull()!!

        val scheduled = scheduler.scheduleUpcomingMeetings(events)

        assertEquals(events.sumOf { it.virtualSpeakers.size }, scheduled.size)
        val jeff = scheduled.first { it.reminder.speakerId == "jeff-bogensberger" }
        assertEquals(start, jeff.reminder.startMillis)
    }

    @Test
    fun `api events produce reminders through the same path`() = runBlocking {
        val body = """{"data":{"events":[{"id":"evt-9","name":"Live","date":"2026-10-10T11:30:00Z",
            "virtualSpeakers":[{"id":"s1","name":"Sam","speakingTopic":"Talk","startTime":"2:30 PM",
            "endTime":"3:00 PM","timeZone":"America/Los_Angeles","meetUrl":"https://meet.google.com/x"}]}]}}"""
        val executor = object : HttpRequestExecutor {
            override suspend fun execute(request: HttpRequest) = HttpResponse(200, request.url, body = body)
        }
        val client = TatumTechApiClient(configuration(TatumTechDataSourceMode.NETWORK), executor = executor)
        val events = TatumTechContentRepository { client }.getUpcomingEvents().getOrNull()!!

        val reminder = scheduler.scheduleUpcomingMeetings(events).single().reminder

        assertEquals("evt-9|s1|$start", reminder.key)
        assertEquals("Sam", reminder.speakerName)
    }

    private fun configuration(mode: TatumTechDataSourceMode) = TatumTechClientConfiguration.Builder()
        .setEnvironment(TatumTechEnvironment.STAGE)
        .setDataSourceMode(mode)
        .build()

    private class FakeWorkScheduler : MeetingReminderWorkScheduler {
        val pending = linkedMapOf<String, Long>()
        val cancelled = mutableListOf<String>()

        override fun enqueue(reminder: MeetingReminder, delayMillis: Long) {
            pending.putIfAbsent(reminder.key, delayMillis)
        }

        override fun cancel(key: String) {
            pending.remove(key)
            cancelled += key
        }
    }

    private companion object {
        const val MINUTE = 60_000L
        const val HOUR = 60 * MINUTE
    }
}

class FakeReminderStore : MeetingReminderStore {
    private var scheduled = emptySet<String>()
    private val fired = mutableMapOf<String, Long>()

    override fun scheduledKeys() = scheduled
    override fun replaceScheduledKeys(keys: Set<String>) {
        scheduled = keys
    }

    override fun hasFired(key: String) = key in fired
    override fun markFired(key: String, endMillis: Long) = fired.putIfAbsent(key, endMillis) == null
    override fun pruneFired(nowMillis: Long) {
        fired.values.removeAll { it < nowMillis - 24 * 60 * 60_000L }
    }

    override var notificationPermissionRequested = false
}
