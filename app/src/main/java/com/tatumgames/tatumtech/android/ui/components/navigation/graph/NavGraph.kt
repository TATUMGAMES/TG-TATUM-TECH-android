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
package com.tatumgames.tatumtech.android.ui.components.navigation.graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.AchievementsScreen
import com.tatumgames.tatumtech.android.ui.components.screens.GameDetailsScreen
import com.tatumgames.tatumtech.android.ui.components.screens.GamesResourcesScreen
import com.tatumgames.tatumtech.android.ui.components.screens.GamesScreen
import com.tatumgames.tatumtech.android.ui.components.screens.GetYourGameDiscoveredScreen
import com.tatumgames.tatumtech.android.ui.components.screens.HomePagerScreen
import com.tatumgames.tatumtech.android.ui.components.screens.PartnersScreen
import com.tatumgames.tatumtech.android.ui.components.screens.ResourcesScreen
import com.tatumgames.tatumtech.android.ui.components.screens.about.AboutContentType
import com.tatumgames.tatumtech.android.ui.components.screens.about.AboutScreen
import com.tatumgames.tatumtech.android.ui.components.screens.auth.ForgotPasswordScreen
import com.tatumgames.tatumtech.android.ui.components.screens.auth.SignInScreen
import com.tatumgames.tatumtech.android.ui.components.screens.auth.SignUpScreen
import com.tatumgames.tatumtech.android.ui.components.screens.auth.splash.AuthScreen
import com.tatumgames.tatumtech.android.ui.components.screens.career.CareerScreen
import com.tatumgames.tatumtech.android.ui.components.screens.coding.AiLlmChallengesScreen
import com.tatumgames.tatumtech.android.ui.components.screens.coding.CodingChallengesScreen
import com.tatumgames.tatumtech.android.ui.components.screens.coding.LeetCodeChallengesScreen
import com.tatumgames.tatumtech.android.ui.components.screens.coding.MockInterviewChallengesScreen
import com.tatumgames.tatumtech.android.ui.components.screens.community.CommunityScreen
import com.tatumgames.tatumtech.android.ui.components.screens.donate.DonateScreen
import com.tatumgames.tatumtech.android.ui.components.screens.events.AttendeesScreen
import com.tatumgames.tatumtech.android.ui.components.screens.events.UpcomingEventsScreen
import com.tatumgames.tatumtech.android.ui.components.screens.events.VirtualSpeakersScreen
import com.tatumgames.tatumtech.android.ui.components.screens.main.DemographicInfoScreen
import com.tatumgames.tatumtech.android.ui.components.screens.main.MainScreen
import com.tatumgames.tatumtech.android.ui.components.screens.main.UserProfileScreen
import com.tatumgames.tatumtech.android.ui.components.screens.networking.ContactCardEditorScreen
import com.tatumgames.tatumtech.android.ui.components.screens.networking.MyContactCardQrScreen
import com.tatumgames.tatumtech.android.ui.components.screens.networking.ScannedContactPreviewScreen
import com.tatumgames.tatumtech.android.ui.components.screens.scanner.ScannerScreen
import com.tatumgames.tatumtech.android.ui.components.screens.stats.StatsScreen
import com.tatumgames.tatumtech.android.ui.components.screens.timeline.MyTimelineScreen

@Composable
fun AccountSetupGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.AUTH_SCREEN
    ) {
        composable(NavRoutes.AUTH_SCREEN) {
            AuthScreen(navController)
        }
        composable(NavRoutes.SIGN_IN_SCREEN) {
            SignInScreen(navController)
        }
        composable(NavRoutes.SIGN_UP_SCREEN) {
            SignUpScreen(navController)
        }
        composable(NavRoutes.FORGOT_PASSWORD_SCREEN) {
            ForgotPasswordScreen(navController)
        }
    }
}

@Composable
fun MainGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME_PAGER_SCREEN
    ) {
        composable(NavRoutes.HOME_PAGER_SCREEN) {
            HomePagerScreen(navController)
        }
        composable(NavRoutes.MAIN_SCREEN) {
            MainScreen(navController)
        }
        composable(NavRoutes.UPCOMING_EVENTS_SCREEN) {
            UpcomingEventsScreen(navController)
        }
        composable(
            route = NavRoutes.VIRTUAL_SPEAKERS_SCREEN,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
            VirtualSpeakersScreen(navController, eventId)
        }
        composable(NavRoutes.CODING_CHALLENGES_SCREEN) {
            CodingChallengesScreen(navController)
        }
        composable(NavRoutes.AI_LLM_CHALLENGES_SCREEN) {
            AiLlmChallengesScreen(navController)
        }
        composable(NavRoutes.LEET_CODE_CHALLENGES_SCREEN) {
            LeetCodeChallengesScreen(navController)
        }
        composable(NavRoutes.MOCK_INTERVIEW_CHALLENGES_SCREEN) {
            MockInterviewChallengesScreen(navController)
        }
        composable(NavRoutes.MY_TIMELINE_SCREEN) {
            MyTimelineScreen(navController)
        }
        composable(NavRoutes.DONATE_SCREEN) {
            DonateScreen(navController)
        }
        composable(NavRoutes.COMMUNITY_SCREEN) {
            CommunityScreen(navController)
        }
        composable(NavRoutes.SCANNER_SCREEN) {
            ScannerScreen(navController, returnToUpcomingEvents = false)
        }
        composable(NavRoutes.SCANNER_FROM_UPCOMING_EVENTS) {
            ScannerScreen(navController, returnToUpcomingEvents = true)
        }
        composable(NavRoutes.CONTACT_CARD_EDITOR_SCREEN) {
            ContactCardEditorScreen(navController)
        }
        composable(NavRoutes.MY_CONTACT_CARD_QR_SCREEN) {
            MyContactCardQrScreen(navController)
        }
        composable(NavRoutes.SCANNED_CONTACT_PREVIEW) {
            ScannedContactPreviewScreen(navController)
        }
        composable(NavRoutes.STATS_SCREEN) {
            StatsScreen(navController)
        }
        composable(NavRoutes.ACHIEVEMENTS_SCREEN) {
            AchievementsScreen(navController)
        }
        composable(NavRoutes.USER_PROFILE_SCREEN) {
            UserProfileScreen(navController)
        }
        composable(NavRoutes.DEMOGRAPHIC_SCREEN) {
            DemographicInfoScreen(navController)
        }
        composable(
            route = NavRoutes.ATTENDEES_SCREEN,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
            AttendeesScreen(navController, eventId)
        }
        composable("about_screen/{contentType}") { backStackEntry ->
            val typeString =
                backStackEntry.arguments?.getString("contentType") ?: AboutContentType.ABOUT.route
            val contentType =
                AboutContentType.entries.find { it.route == typeString } ?: AboutContentType.ABOUT
            AboutScreen(navController, contentType)
        }
        composable(NavRoutes.PARTNERS_SCREEN) {
            PartnersScreen(navController)
        }
        composable(NavRoutes.RESOURCES_SCREEN) {
            ResourcesScreen(navController)
        }
        composable(NavRoutes.GAMES_RESOURCES_SCREEN) {
            GamesResourcesScreen(navController)
        }
        composable(NavRoutes.CAREER_SCREEN) {
            CareerScreen(navController)
        }
        composable(NavRoutes.GAMES_SCREEN) {
            GamesScreen(navController)
        }
        composable(NavRoutes.GET_YOUR_GAME_DISCOVERED_SCREEN) {
            GetYourGameDiscoveredScreen(navController)
        }
        composable(
            route = NavRoutes.GAME_DETAILS_SCREEN,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            GameDetailsScreen(navController, gameId)
        }
    }
}
