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

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.activity.AuthActivity
import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.framework.android.logger.Logger

/**
 * Posts the system notification for a reminder on the "Virtual Speakers" channel. Sound,
 * vibration, and heads-up behavior come from the channel, so the user's settings apply.
 */
class MeetingReminderNotifier(context: Context) {

    private val context = context.applicationContext

    fun show(reminder: MeetingReminder) {
        if (!canPostNotifications()) return
        createChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_speaker)
            .setColor(ContextCompat.getColor(context, R.color.purple_500))
            .setContentTitle(meetingReminderTitle(context, reminder, System.currentTimeMillis()))
            .setContentText(reminder.speakingTopic)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "${reminder.speakingTopic}\n${context.getString(R.string.meeting_reminder_join_hint)}"
                )
            )
            .setSubText(reminder.eventName)
            .setWhen(reminder.startMillis)
            .setShowWhen(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setTimeoutAfter((reminder.endMillis - System.currentTimeMillis()).coerceAtLeast(1L))
            .setContentIntent(contentIntent(reminder))
            .build()
        try {
            NotificationManagerCompat.from(context).notify(notificationId(reminder), notification)
        } catch (e: SecurityException) {
            Logger.w(TAG, "Meeting reminder notification not permitted: ${e.message}")
        }
    }

    private fun canPostNotifications(): Boolean {
        val permitted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permitted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Opens through the launcher activity so the session check runs before navigating.
     */
    private fun contentIntent(reminder: MeetingReminder): PendingIntent {
        val intent = Intent(context, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        MeetingReminderDestination.of(reminder).writeTo(intent)
        return PendingIntent.getActivity(
            context,
            notificationId(reminder),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "virtual_speakers"

        fun notificationId(reminder: MeetingReminder): Int = reminder.key.hashCode()

        /**
         * Idempotent; safe to call on every app start.
         */
        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.meeting_reminder_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.meeting_reminder_channel_description)
                enableVibration(true)
                setShowBadge(true)
            }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
}

/**
 * "Jeff is speaking in a couple of minutes" before the start, "Jeff is speaking now" after it.
 */
fun meetingReminderTitle(context: Context, reminder: MeetingReminder, nowMillis: Long): String {
    val minutes = reminder.minutesUntilStart(nowMillis)
    return if (minutes > 0) {
        context.resources.getQuantityString(
            R.plurals.meeting_reminder_title_soon, minutes, reminder.speakerName, minutes
        )
    } else {
        context.getString(R.string.meeting_reminder_title_now, reminder.speakerName)
    }
}
