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
package com.tatumgames.tatumtech.android.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavHostController

/**
 * Attaches a single OnDestinationChangedListener that logs [AnalyticsEvents.NAVIGATE]
 * without duplicating on recomposition.
 */
@Composable
fun TrackNavigationAnalytics(navController: NavHostController) {
    val listener = remember {
        NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route
            if (!route.isNullOrBlank()) {
                AnalyticsService.navigate(route)
            }
        }
    }
    DisposableEffect(navController) {
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
}
