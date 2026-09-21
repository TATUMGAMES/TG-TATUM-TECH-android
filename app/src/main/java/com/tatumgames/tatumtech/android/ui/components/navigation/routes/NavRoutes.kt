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
package com.tatumgames.tatumtech.android.ui.components.navigation.routes

import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes.GAME_DETAILS_SCREEN


object NavRoutes {
    // account setup routes
    const val AUTH_SCREEN = "auth_screen"
    const val SIGN_IN_SCREEN = "sign_in_screen"
    const val SIGN_UP_SCREEN = "sign_up_screen"
    const val FORGOT_PASSWORD_SCREEN = "forgot_password_screen"
    const val CHANGE_PASSWORD_SCREEN = "change_password_screen"

    // main routes
    const val MAIN_SCREEN = "main_screen"
    const val UPCOMING_EVENTS_SCREEN = "upcoming_events_screen"
    const val VIRTUAL_SPEAKERS_SCREEN = "virtual_speakers_screen/{eventId}"
    const val CODING_CHALLENGES_SCREEN = "coding_challenges_screen"
    const val AI_LLM_CHALLENGES_SCREEN = "ai_llm_challenges_screen"
    const val LEET_CODE_CHALLENGES_SCREEN = "leet_code_challenges_screen"
    const val MOCK_INTERVIEW_CHALLENGES_SCREEN = "mock_interview_challenges_screen"
    const val MY_TIMELINE_SCREEN = "my_timeline_screen"
    const val COMMUNITY_SCREEN = "community_screen"
    const val SCANNER_SCREEN = "scanner_screen"

    /** Scanner opened from Upcoming Events networking; back returns to Upcoming Events. */
    const val SCANNER_FROM_UPCOMING_EVENTS = "scanner_from_upcoming_events"
    const val CONTACT_CARD_EDITOR_SCREEN = "contact_card_editor_screen"
    const val MY_CONTACT_CARD_QR_SCREEN = "my_contact_card_qr_screen"
    const val SCANNED_CONTACT_PREVIEW = "scanned_contact_preview"
    const val DONATE_SCREEN = "donate_screen"
    const val STATS_SCREEN = "stats_screen"
    const val ACHIEVEMENTS_SCREEN = "achievements_screen"
    const val USER_PROFILE_SCREEN = "user_profile_screen"
    const val DEMOGRAPHIC_SCREEN = "demographic_screen"
    const val ATTENDEES_SCREEN = "attendees_screen/{eventId}"

    // Additional routes for home pager sections
    const val HOME_PAGER_SCREEN = "home_pager"
    const val PARTNERS_SCREEN = "partners_screen"
    const val RESOURCES_SCREEN = "resources_screen"
    const val GAMES_RESOURCES_SCREEN = "games_resources_screen"
    const val CAREER_SCREEN = "career_screen"
    const val GAMES_SCREEN = "games_screen"
    const val GAME_DETAILS_SCREEN = "game_details_screen/{gameId}"
    const val GET_YOUR_GAME_DISCOVERED_SCREEN = "get_your_game_discovered_screen"

    /** Use this when navigating to game details so the route matches [GAME_DETAILS_SCREEN]. */
    fun gameDetailsRoute(gameId: String): String = "game_details_screen/$gameId"

    fun virtualSpeakersRoute(eventId: Long): String = "virtual_speakers_screen/$eventId"

    /**
     * Helper function to build the attendees screen route with event ID.
     *
     * @param eventId The ID of the event to display attendees for.
     * @return The complete route string for the attendees screen.
     */
    fun attendeesScreenRoute(eventId: Long): String = "attendees_screen/$eventId"
}
