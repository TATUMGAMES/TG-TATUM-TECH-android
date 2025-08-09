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
    const val CODING_CHALLENGES_SCREEN = "coding_challenges_screen"
    const val MY_TIMELINE_SCREEN = "my_timeline_screen"
    const val COMMUNITY_SCREEN = "community_screen"
    const val SCANNER_SCREEN = "scanner_screen"
    const val DONATE_SCREEN = "donate_screen"
    const val STATS_SCREEN = "stats_screen"
    const val ATTENDEES_SCREEN = "attendees_screen/{eventId}"
}
