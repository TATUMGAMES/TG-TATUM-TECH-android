package com.tatumgames.tatumtech.android.utils

import android.content.Context
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.enums.AttendeeNames
import com.tatumgames.tatumtech.android.enums.ProfileImage
import com.tatumgames.tatumtech.android.ui.components.screens.stats.models.Achievement
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Attendee
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.ApiEvent
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.EventLocation
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.EventRegistration
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.EventAttendee
import com.tatumgames.tatumtech.android.ui.components.screens.main.models.Notification
import com.tatumgames.tatumtech.android.utils.Utils.drawableToUri

object MockData {

    fun getDummyNotifications(
        context: Context
    ): List<Notification> {
        return listOf(
            Notification(
                iconResId = R.drawable.notif_coding_challenge, // Icons.Default.Build,
                title = context.getString(R.string.coding_challenge),
                description = context.getString(R.string.new_coding_challenge_available)
            ),
            Notification(
                iconResId = R.drawable.notif_event_registration, // Icons.Default.DateRange,
                title = context.getString(R.string.event_registration),
                description = context.getString(R.string.event_registration_confirmed)
            )
        )
    }

    fun getMockEvents(
        context: Context
    ): List<Event> {
        // Legacy function - keeping for backward compatibility
        return getMockEventsLegacy(context)
    }

    fun getMockEventsApi(
        context: Context
    ): List<ApiEvent> {
        return listOf(
            ApiEvent(
                eventId = "tt_fall_2025",
                name = context.getString(R.string.event_name_fall_2025),
                description = "A networking event for developers and tech enthusiasts.",
                startTime = "2025-10-15T11:00:00Z",
                endTime = "2025-10-15T16:00:00Z",
                location = EventLocation(
                    name = "Tatum HQ",
                    address = context.getString(R.string.event_location_south_la)
                ),
                registration = EventRegistration(
                    isOpen = true,
                    deadline = "2025-10-13T23:59:59Z"
                ),
                isUserRegistered = false,
                attendees = listOf(
                    EventAttendee(
                        userId = "user_12345",
                        userDetailsUrl = "/user/details/user_12345"
                    ),
                    EventAttendee(
                        userId = "user_67890",
                        userDetailsUrl = "/user/details/user_67890"
                    )
                )
            ),
            ApiEvent(
                eventId = "tt_spring_2026",
                name = context.getString(R.string.event_name_spring_2026),
                description = "Advanced coding workshops and industry networking.",
                startTime = "2026-04-22T12:00:00Z",
                endTime = "2026-04-22T17:00:00Z",
                location = EventLocation(
                    name = "Tatum HQ",
                    address = context.getString(R.string.event_location_south_la)
                ),
                registration = EventRegistration(
                    isOpen = true,
                    deadline = "2026-04-20T23:59:59Z"
                ),
                isUserRegistered = true,
                attendees = listOf(
                    EventAttendee(
                        userId = "user_12345",
                        userDetailsUrl = "/user/details/user_12345"
                    ),
                    EventAttendee(
                        userId = "user_67890",
                        userDetailsUrl = "/user/details/user_67890"
                    )
                )
            )
        )
    }

    private fun getMockEventsLegacy(
        context: Context
    ): List<Event> {
        val allAttendees = listOf(
            Attendee(
                1,
                AttendeeNames.MALE_ATTENDEE_01.displayName,
                ProfileImage.MALE_PLACEHOLDER_01
            ),
            Attendee(
                2,
                AttendeeNames.FEMALE_ATTENDEE_01.displayName,
                ProfileImage.FEMALE_PLACEHOLDER_01
            ),
            Attendee(
                3,
                AttendeeNames.MALE_ATTENDEE_02.displayName,
                ProfileImage.MALE_PLACEHOLDER_02
            ),
            Attendee(
                4,
                AttendeeNames.FEMALE_ATTENDEE_02.displayName,
                ProfileImage.FEMALE_PLACEHOLDER_02
            ),
            Attendee(
                5,
                AttendeeNames.MALE_ATTENDEE_03.displayName,
                ProfileImage.MALE_PLACEHOLDER_03
            ),
            Attendee(
                6,
                AttendeeNames.FEMALE_ATTENDEE_03.displayName,
                ProfileImage.FEMALE_PLACEHOLDER_03
            ),
            Attendee(
                7,
                AttendeeNames.MALE_ATTENDEE_04.displayName,
                ProfileImage.MALE_PLACEHOLDER_04
            ),
            Attendee(
                8,
                AttendeeNames.FEMALE_ATTENDEE_04.displayName,
                ProfileImage.FEMALE_PLACEHOLDER_04
            ),
            Attendee(
                9,
                AttendeeNames.MALE_ATTENDEE_05.displayName,
                ProfileImage.MALE_PLACEHOLDER_05
            ),
            Attendee(
                10,
                AttendeeNames.FEMALE_ATTENDEE_05.displayName,
                ProfileImage.FEMALE_PLACEHOLDER_05
            ),
            Attendee(11, AttendeeNames.MALE_ATTENDEE_06.displayName, ProfileImage.INITIALS),
            Attendee(12, AttendeeNames.FEMALE_ATTENDEE_06.displayName, ProfileImage.INITIALS),
            Attendee(13, AttendeeNames.MALE_ATTENDEE_07.displayName, ProfileImage.INITIALS),
            Attendee(14, AttendeeNames.FEMALE_ATTENDEE_07.displayName, ProfileImage.INITIALS),
            Attendee(15, AttendeeNames.MALE_ATTENDEE_08.displayName, ProfileImage.INITIALS),
            Attendee(16, AttendeeNames.FEMALE_ATTENDEE_08.displayName, ProfileImage.INITIALS),
            Attendee(17, AttendeeNames.MALE_ATTENDEE_09.displayName, ProfileImage.INITIALS),
            Attendee(18, AttendeeNames.FEMALE_ATTENDEE_09.displayName, ProfileImage.INITIALS)
        )

        return listOf(
            Event(
                id = 1,
                name = context.getString(R.string.event_name_fall_2025),
                host = context.getString(R.string.event_host_tatum_games),
                date = "2025-10-15T11:00:00Z",
                durationHours = 5,
                location = context.getString(R.string.event_location_south_la),
                isRegistrationOpen = true,
                attendees = allAttendees.shuffled().take(6),
                featuredImage = drawableToUri(R.drawable.tatum_tech_placeholder_flyer_01)
            ),
            Event(
                id = 2,
                name = context.getString(R.string.event_name_spring_2026),
                host = context.getString(R.string.event_host_tatum_games),
                date = "2026-04-22T12:00:00Z",
                durationHours = 5,
                location = context.getString(R.string.event_location_south_la),
                isRegistrationOpen = true,
                attendees = allAttendees.shuffled().take(8),
                featuredImage = "color://spring_purple"
            ),
            Event(
                id = 3,
                name = context.getString(R.string.event_name_fall_2026),
                host = context.getString(R.string.event_host_tatum_games),
                date = "2026-10-08T11:30:00Z",
                durationHours = 5,
                location = context.getString(R.string.event_location_south_la),
                isRegistrationOpen = false,
                attendees = allAttendees.shuffled().take(10),
                featuredImage = drawableToUri(R.drawable.tatum_tech_placeholder_flyer_02)
            ),
            Event(
                id = 4,
                name = context.getString(R.string.event_name_spring_2027),
                host = context.getString(R.string.event_host_tatum_games),
                date = "2027-04-14T12:00:00Z",
                durationHours = 5,
                location = context.getString(R.string.event_location_south_la),
                isRegistrationOpen = true,
                attendees = allAttendees.shuffled().take(6),
                featuredImage = "color://spring_purple2"
            ),
            Event(
                id = 5,
                name = context.getString(R.string.event_name_fall_2027),
                host = context.getString(R.string.event_host_tatum_games),
                date = "2027-10-20T11:00:00Z",
                durationHours = 5,
                location = context.getString(R.string.event_location_south_la),
                isRegistrationOpen = true,
                attendees = allAttendees.shuffled().take(8),
                featuredImage = drawableToUri(R.drawable.tatum_tech_placeholder_flyer_03)
            )
        )
    }

    fun getAllAchievements(
        context: Context
    ): List<Achievement> {
        return listOf(
            // Event Registration Achievements
            Achievement(
                1,
                context.getString(R.string.achievement_title_first_step),
                context.getString(R.string.achievement_desc_first_step),
                R.drawable.badge_first_step
            ),
            Achievement(
                2,
                context.getString(R.string.achievement_title_committed),
                context.getString(R.string.achievement_desc_committed),
                R.drawable.badge_committed
            ),
            Achievement(
                3,
                context.getString(R.string.achievement_title_event_veteran),
                context.getString(R.string.achievement_desc_event_veteran),
                R.drawable.badge_event_veteran
            ),
            // Event Attendance Achievements
            Achievement(
                1,
                context.getString(R.string.achievement_title_showed_up),
                context.getString(R.string.achievement_desc_showed_up),
                R.drawable.badge_showed_up
            ),
            Achievement(
                2,
                context.getString(R.string.achievement_title_loyal_attendee),
                context.getString(R.string.achievement_desc_loyal_attendee),
                R.drawable.badge_loyal_attendee
            ),
            Achievement(
                3,
                context.getString(R.string.achievement_title_all_star_attendee),
                context.getString(R.string.achievement_desc_all_star_attendee),
                R.drawable.badge_allstar_attendee
            ),
            // Beginner Challenge Achievements
            Achievement(
                1,
                context.getString(R.string.achievement_title_coder_in_training),
                context.getString(R.string.achievement_desc_coder_in_training),
                R.drawable.badge_coder_in_training
            ),
            Achievement(
                2,
                context.getString(R.string.achievement_title_leveling_up),
                context.getString(R.string.achievement_desc_leveling_up),
                R.drawable.badge_leveling_up
            ),
            Achievement(
                3,
                context.getString(R.string.achievement_title_beginner_master),
                context.getString(R.string.achievement_desc_beginner_master),
                R.drawable.badge_beginner_master
            ),
            // Intermediate Challenge Achievements
            Achievement(
                1,
                context.getString(R.string.achievement_title_problem_solver),
                context.getString(R.string.achievement_desc_problem_solver),
                R.drawable.badge_problem_solver
            ),
            Achievement(
                2,
                context.getString(R.string.achievement_title_code_climber),
                context.getString(R.string.achievement_desc_code_climber),
                R.drawable.badge_code_climber
            ),
            Achievement(
                3,
                context.getString(R.string.achievement_title_intermediate_champ),
                context.getString(R.string.achievement_desc_intermediate_champ),
                R.drawable.badge_intermediate_champ
            ),
            // Advanced Challenge Achievements
            Achievement(
                1,
                context.getString(R.string.achievement_title_code_warrior),
                context.getString(R.string.achievement_desc_code_warrior),
                R.drawable.badge_code_warrior
            ),
            Achievement(
                2,
                context.getString(R.string.achievement_title_algorithm_slayer),
                context.getString(R.string.achievement_desc_algorithm_slayer),
                R.drawable.badge_algorithm_slayer
            ),
            Achievement(
                3,
                context.getString(R.string.achievement_title_elite_hacker),
                context.getString(R.string.achievement_desc_elite_hacker),
                R.drawable.badge_elite_hacker
            ),
            // QR Code Achievements
            Achievement(
                1,
                context.getString(R.string.achievement_title_qr_curious),
                context.getString(R.string.achievement_desc_qr_curious),
                R.drawable.badge_qr_curious
            ),
            Achievement(
                2,
                context.getString(R.string.achievement_title_qr_explorer),
                context.getString(R.string.achievement_desc_qr_explorer),
                R.drawable.badge_qr_explorer
            ),
            Achievement(
                3,
                context.getString(R.string.achievement_title_qr_adventurer),
                context.getString(R.string.achievement_desc_qr_adventurer),
                R.drawable.badge_qr_adventurer
            ),
            Achievement(
                4,
                context.getString(R.string.achievement_title_qr_hunter),
                context.getString(R.string.achievement_desc_qr_hunter),
                R.drawable.badge_qr_hunter
            ),
            Achievement(
                5,
                context.getString(R.string.achievement_title_qr_master),
                context.getString(R.string.achievement_desc_qr_master),
                R.drawable.badge_qr_master
            ),
            Achievement(
                6,
                context.getString(R.string.achievement_title_qr_legend),
                context.getString(R.string.achievement_desc_qr_legend),
                R.drawable.badge_qr_legend
            ),
            // Donation Achievement
            Achievement(
                1,
                context.getString(R.string.achievement_title_heart_giver),
                context.getString(R.string.achievement_desc_heart_giver),
                R.drawable.badge_heart_giver
            )
        )
    }
}
