package com.tatumgames.tatumtech.android.reminders

import com.tatumgames.tatumtech.android.reminders.MeetingReminderCoordinator.Delivery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MeetingReminderCoordinatorTest {

    private val reminder = MeetingReminder(
        key = "1|jeff|1000000",
        eventId = "1",
        eventName = "Tatum Tech Fall 2026",
        speakerId = "jeff",
        speakerName = "Jeff",
        speakingTopic = "Topic",
        startMillis = 1_000_000L,
        endMillis = 2_800_000L
    )

    private val store = FakeReminderStore()
    private var foreground = false
    private var now = reminder.triggerAtMillis
    private val banners = mutableListOf<MeetingReminder>()
    private val notifications = mutableListOf<MeetingReminder>()

    private val coordinator = MeetingReminderCoordinator(
        store = store,
        isAppInForeground = { foreground },
        showBanner = { banners += it },
        postNotification = { notifications += it },
        clock = { now }
    )

    @Test
    fun `foreground app shows the in-app banner`() {
        foreground = true

        assertEquals(Delivery.BANNER, coordinator.deliver(reminder))
        assertEquals(listOf(reminder), banners)
        assertTrue(notifications.isEmpty())
    }

    @Test
    fun `background or closed app posts a system notification`() {
        assertEquals(Delivery.NOTIFICATION, coordinator.deliver(reminder))
        assertEquals(listOf(reminder), notifications)
        assertTrue(banners.isEmpty())
    }

    @Test
    fun `a reminder is delivered only once`() {
        coordinator.deliver(reminder)
        foreground = true

        assertEquals(Delivery.ALREADY_DELIVERED, coordinator.deliver(reminder))
        assertEquals(1, notifications.size + banners.size)
        assertTrue(store.hasFired(reminder.key))
    }

    @Test
    fun `late work for an ended session delivers nothing`() {
        now = reminder.endMillis

        assertEquals(Delivery.ENDED, coordinator.deliver(reminder))
        assertTrue(banners.isEmpty() && notifications.isEmpty())
        assertTrue(!store.hasFired(reminder.key))
    }
}
