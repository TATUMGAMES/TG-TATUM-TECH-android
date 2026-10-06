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
package com.tatumgames.tatumtech.android.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.tatumgames.tatumtech.android.analytics.RatingPromptTriggers
import com.tatumgames.tatumtech.android.reminders.MeetingReminderDestination
import com.tatumgames.tatumtech.android.ui.components.common.MeetingReminderBannerHost
import com.tatumgames.tatumtech.android.ui.components.common.NotificationPermissionPrompt
import com.tatumgames.tatumtech.android.ui.components.navigation.graph.MainGraph
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.rating.RatingPromptManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val showAppOpenRatingPrompt = mutableStateOf(false)
    private val pendingReminderDestination = mutableStateOf<MeetingReminderDestination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A non-null bundle means recreation (rotation, process restore), not a new app open.
        if (savedInstanceState == null) {
            val reminderDestination = MeetingReminderDestination.readFrom(intent)
            if (reminderDestination != null) {
                pendingReminderDestination.value = reminderDestination
            } else {
                lifecycleScope.launch {
                    if (RatingPromptManager.recordAppOpen(applicationContext)) {
                        showAppOpenRatingPrompt.value = true
                    }
                }
            }
        }
        setContent {
            val navController = rememberNavController()
            Box(modifier = Modifier.fillMaxSize()) {
                MainGraph(navController)
                MeetingReminderBannerHost(
                    onOpen = { reminder ->
                        navController.navigate(MeetingReminderDestination.of(reminder).route)
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
            NotificationPermissionPrompt()

            val reminderDestination = pendingReminderDestination.value
            LaunchedEffect(reminderDestination) {
                if (reminderDestination != null) {
                    pendingReminderDestination.value = null
                    navController.navigate(reminderDestination.route)
                }
            }

            val showPrompt = showAppOpenRatingPrompt.value
            LaunchedEffect(showPrompt) {
                if (showPrompt) {
                    showAppOpenRatingPrompt.value = false
                    navController.navigate(NavRoutes.ratingRoute(RatingPromptTriggers.APP_OPEN))
                }
            }
        }
    }
}
