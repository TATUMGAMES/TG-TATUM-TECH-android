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
import androidx.core.content.edit

/**
 * Persistent reminder bookkeeping that must survive process death: which reminders are
 * scheduled (to cancel stale ones) and which were already delivered (to deliver at most once).
 */
interface MeetingReminderStore {

    fun scheduledKeys(): Set<String>

    fun replaceScheduledKeys(keys: Set<String>)

    fun hasFired(key: String): Boolean

    /** Records [key] as delivered. Returns `false` if it already was, so callers deliver once. */
    fun markFired(key: String, endMillis: Long): Boolean

    /** Forgets delivered reminders whose session ended long enough ago. */
    fun pruneFired(nowMillis: Long)

    /** Whether the post-sign-in notification permission explanation was already shown. */
    var notificationPermissionRequested: Boolean
}

class SharedPreferencesMeetingReminderStore(context: Context) : MeetingReminderStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun scheduledKeys(): Set<String> =
        preferences.getStringSet(KEY_SCHEDULED, null)?.toSet().orEmpty()

    override fun replaceScheduledKeys(keys: Set<String>) {
        preferences.edit { putStringSet(KEY_SCHEDULED, keys.toSet()) }
    }

    override fun hasFired(key: String): Boolean = preferences.contains(FIRED_PREFIX + key)

    override fun markFired(key: String, endMillis: Long): Boolean = synchronized(LOCK) {
        if (hasFired(key)) return false
        // Written synchronously: a second delivery path may check right after this returns.
        preferences.edit(commit = true) { putLong(FIRED_PREFIX + key, endMillis) }
        true
    }

    override fun pruneFired(nowMillis: Long) {
        val expired = preferences.all.filter { (name, value) ->
            name.startsWith(FIRED_PREFIX) && ((value as? Long) ?: 0L) < nowMillis - FIRED_RETENTION_MS
        }.keys
        if (expired.isEmpty()) return
        preferences.edit { expired.forEach(::remove) }
    }

    override var notificationPermissionRequested: Boolean
        get() = preferences.getBoolean(KEY_PERMISSION_REQUESTED, false)
        set(value) {
            preferences.edit { putBoolean(KEY_PERMISSION_REQUESTED, value) }
        }

    private companion object {
        const val PREFERENCES_NAME = "meeting_reminders"
        const val KEY_SCHEDULED = "scheduled_keys"
        const val KEY_PERMISSION_REQUESTED = "notification_permission_requested"
        const val FIRED_PREFIX = "fired:"
        const val FIRED_RETENTION_MS = 24L * 60 * 60 * 1000

        /** Shared by every instance: all of them write the same preferences file. */
        val LOCK = Any()
    }
}
