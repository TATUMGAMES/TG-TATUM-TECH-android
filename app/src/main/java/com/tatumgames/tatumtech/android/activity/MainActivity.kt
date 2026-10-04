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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.tatumgames.tatumtech.android.analytics.RatingPromptTriggers
import com.tatumgames.tatumtech.android.ui.components.navigation.graph.MainGraph
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.rating.RatingPromptManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val showAppOpenRatingPrompt = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A non-null bundle means recreation (rotation, process restore), not a new app open.
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                if (RatingPromptManager.recordAppOpen(applicationContext)) {
                    showAppOpenRatingPrompt.value = true
                }
            }
        }
        setContent {
            val navController = rememberNavController()
            MainGraph(navController)

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
