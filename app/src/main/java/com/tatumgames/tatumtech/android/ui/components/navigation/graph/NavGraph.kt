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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tatumgames.tatumtech.android.constants.Constants.KEY_USER_ID
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.AttendeesScreen
import com.tatumgames.tatumtech.android.ui.components.screens.AuthScreen
import com.tatumgames.tatumtech.android.ui.components.screens.CodingChallengesScreen
import com.tatumgames.tatumtech.android.ui.components.screens.CommunityScreen
import com.tatumgames.tatumtech.android.ui.components.screens.DonateScreen
import com.tatumgames.tatumtech.android.ui.components.screens.ForgotPasswordScreen
import com.tatumgames.tatumtech.android.ui.components.screens.MainScreen
import com.tatumgames.tatumtech.android.ui.components.screens.MyTimelineScreen
import com.tatumgames.tatumtech.android.ui.components.screens.ScannerScreen
import com.tatumgames.tatumtech.android.ui.components.screens.SignInScreen
import com.tatumgames.tatumtech.android.ui.components.screens.SignUpScreen
import com.tatumgames.tatumtech.android.ui.components.screens.StatsScreen
import com.tatumgames.tatumtech.android.ui.components.screens.UpcomingEventsScreen

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
        startDestination = NavRoutes.MAIN_SCREEN
    ) {
        composable(NavRoutes.MAIN_SCREEN) {
            MainScreen(navController)
        }
        composable(NavRoutes.UPCOMING_EVENTS_SCREEN) {
            UpcomingEventsScreen(navController)
        }
        composable(NavRoutes.CODING_CHALLENGES_SCREEN) {
            CodingChallengesScreen(navController)
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
            ScannerScreen(navController)
        }
        composable(NavRoutes.STATS_SCREEN) {
            StatsScreen(navController)
        }
        composable(NavRoutes.ATTENDEES_SCREEN) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong(KEY_USER_ID) ?: 0
            AttendeesScreen(navController, eventId)
        }
    }
}
