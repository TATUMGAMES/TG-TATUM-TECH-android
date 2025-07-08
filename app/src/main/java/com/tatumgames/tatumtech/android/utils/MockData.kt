package com.tatumgames.tatumtech.android.utils

import android.content.Context
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.screens.models.Attendee
import com.tatumgames.tatumtech.android.ui.components.screens.models.Event
import com.tatumgames.tatumtech.android.ui.components.screens.models.NotificationItem
import com.tatumgames.tatumtech.android.utils.Utils.drawableToUri
import com.tatumgames.tatumtech.android.utils.Utils.parseDate

object MockData {

    fun getDummyNotifications(
        context: Context
    ): List<NotificationItem> {
        return listOf(
            NotificationItem(
                iconResId = R.drawable.notif_coding_challenge, // Icons.Default.Build,
                title = context.getString(R.string.coding_challenge),
                description = context.getString(R.string.new_coding_challenge_available)
            ),
            NotificationItem(
                iconResId = R.drawable.notif_event_registration, // Icons.Default.DateRange,
                title = context.getString(R.string.event_registration),
                description = context.getString(R.string.event_registration_confirmed)
            )
        )
    }

    fun getMockEvents(): List<Event> {
        val allAttendees = listOf(
            // Profile image attendees (80% of total)
            Attendee("1", "Alex Johnson", "profile_male_placeholder_01"),
            Attendee("2", "Sarah Chen", "profile_female_placeholder_01"),
            Attendee("3", "Mike Rodriguez", "profile_male_placeholder_02"),
            Attendee("4", "Emily Davis", "profile_female_placeholder_02"),
            Attendee("5", "David Kim", "profile_male_placeholder_03"),
            Attendee("6", "Lisa Wang", "profile_female_placeholder_03"),
            Attendee("7", "James Wilson", "profile_male_placeholder_04"),
            Attendee("8", "Maria Garcia", "profile_female_placeholder_04"),
            Attendee("9", "Robert Brown", "profile_male_placeholder_05"),
            Attendee("10", "Jennifer Lee", "profile_female_placeholder_05"),
            // Initials-only attendees (20% of total)
            Attendee("11", "Tempestt Tatum", "initials"),
            Attendee("12", "Tyerra Garland", "initials"),
            Attendee("13", "Jordan Lindsey", "initials"),
            Attendee("14", "Kevin Lim", "initials"),
            Attendee("15", "Lebron James", "initials"),
            Attendee("16", "Aisha Thompson", "initials"),
            Attendee("17", "Marcus Johnson", "initials"),
            Attendee("18", "Sofia Rodriguez", "initials")
        )

        return listOf(
            Event(
                eventId = "event_001",
                name = "Tatum Tech Fall 2025",
                host = "Tatum Games",
                date = parseDate("2025-10-15T11:00:00Z"),
                durationHours = 5,
                location = "South Los Angeles, CA",
                isRegistrationOpen = true,
                attendees = allAttendees.shuffled().take(6),
                featuredImage = drawableToUri(R.drawable.tatum_tech_placeholder_flyer_01)
            ),
            Event(
                eventId = "event_002",
                name = "Tatum Tech Spring 2026",
                host = "Tatum Games",
                date = parseDate("2026-04-22T12:00:00Z"),
                durationHours = 5,
                location = "South Los Angeles, CA",
                isRegistrationOpen = true,
                attendees = allAttendees.shuffled().take(8),
                featuredImage = "color://spring_purple"
            ),
            Event(
                eventId = "event_003",
                name = "Tatum Tech Fall 2026",
                host = "Tatum Games",
                date = parseDate("2026-10-08T11:30:00Z"),
                durationHours = 5,
                location = "South Los Angeles, CA",
                isRegistrationOpen = false,
                attendees = allAttendees.shuffled().take(10),
                featuredImage = drawableToUri(R.drawable.tatum_tech_placeholder_flyer_02)
            ),
            Event(
                eventId = "event_004",
                name = "Tatum Tech Spring 2027",
                host = "Tatum Games",
                date = parseDate("2027-04-14T12:00:00Z"),
                durationHours = 5,
                location = "South Los Angeles, CA",
                isRegistrationOpen = true,
                attendees = allAttendees.shuffled().take(6),
                featuredImage = "color://spring_purple2"
            ),
            Event(
                eventId = "event_005",
                name = "Tatum Tech Fall 2027",
                host = "Tatum Games",
                date = parseDate("2027-10-20T11:00:00Z"),
                durationHours = 5,
                location = "South Los Angeles, CA",
                isRegistrationOpen = true,
                attendees = allAttendees.shuffled().take(8),
                featuredImage = drawableToUri(R.drawable.tatum_tech_placeholder_flyer_03)
            )
        )
    }
}
