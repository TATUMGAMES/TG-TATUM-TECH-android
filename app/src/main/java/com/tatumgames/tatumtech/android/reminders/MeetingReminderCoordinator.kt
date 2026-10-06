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
 * Surfaces a due reminder exactly once: as the in-app banner while the app is in the foreground,
 * otherwise as a system notification.
 */
class MeetingReminderCoordinator(
    private val store: MeetingReminderStore,
    private val isAppInForeground: () -> Boolean,
    private val showBanner: (MeetingReminder) -> Unit,
    private val postNotification: (MeetingReminder) -> Unit,
    private val clock: () -> Long = System::currentTimeMillis
) {

    enum class Delivery { BANNER, NOTIFICATION, ALREADY_DELIVERED, ENDED }

    fun deliver(reminder: MeetingReminder): Delivery {
        if (reminder.hasEnded(clock())) return Delivery.ENDED
        if (!store.markFired(reminder.key, reminder.endMillis)) return Delivery.ALREADY_DELIVERED
        return if (isAppInForeground()) {
            showBanner(reminder)
            Delivery.BANNER
        } else {
            postNotification(reminder)
            Delivery.NOTIFICATION
        }
    }
}
