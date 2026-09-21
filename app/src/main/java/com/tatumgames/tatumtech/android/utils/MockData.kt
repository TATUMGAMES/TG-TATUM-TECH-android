package com.tatumgames.tatumtech.android.utils

import android.content.Context
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import com.tatumgames.tatumtech.android.ui.components.screens.main.models.Notification
import com.tatumgames.tatumtech.android.ui.models.Achievement
import com.tatumgames.tatumtech.android.ui.utils.JsonImporter

object MockData {

    fun getDummyNotifications(
        context: Context
    ): List<Notification> {
        return listOf(
            Notification(
                iconResId = R.drawable.notif_coding_challenge,
                title = context.getString(R.string.coding_challenge),
                description = context.getString(R.string.new_coding_challenge_available)
            ),
            Notification(
                iconResId = R.drawable.notif_event_registration,
                title = context.getString(R.string.event_registration),
                description = context.getString(R.string.event_registration_confirmed)
            )
        )
    }

    fun getMockEvents(
        context: Context
    ): List<Event> {
        return JsonImporter.loadUpcomingEvents(context)
    }

    fun getAllAchievements(
        context: Context
    ): List<Achievement> {
        return JsonImporter.loadAchievements(context)
    }
}
