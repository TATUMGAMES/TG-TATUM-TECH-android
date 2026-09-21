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
package com.tatumgames.tatumtech.android.ui.components.screens.timeline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.enums.TimelineFilter
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.theme.Grey500
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight

/**
 * Rolling window filters (documented product behavior):
 * - TODAY = last 24 hours
 * - WEEK = last 7 × 24 hours
 * - MONTH = last 30 × 24 hours
 *
 * Timeline updates reactively via Room [kotlinx.coroutines.flow.Flow].
 */
@Composable
fun MyTimelineScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val timelineRepository = remember { TimelineDatabaseRepository(db.timelineDao()) }
    var filter by remember { mutableStateOf(TimelineFilter.TODAY) }

    val fromTimestamp = remember(filter) {
        TimelineFilterWindow.fromTimestamp(filter, System.currentTimeMillis())
    }

    val timelineItems by timelineRepository
        .observeTimelineEventsFrom(fromTimestamp)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.my_timeline),
                onBackClick = { navController.popBackStack() })
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = ScreenScaffoldLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TimelineFilterBar(
                filter = filter,
                onFilterChange = { filter = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (timelineItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    StandardText(
                        text = stringResource(R.string.timeline_no_activity_period),
                        color = Grey500
                    )
                }
            } else {
                TimelineList(items = timelineItems)
            }
        }
    }
}
