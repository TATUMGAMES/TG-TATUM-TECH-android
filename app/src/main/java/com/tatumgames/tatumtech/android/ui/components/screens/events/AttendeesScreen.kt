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
package com.tatumgames.tatumtech.android.ui.components.screens.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.AttendeeEntity
import com.tatumgames.tatumtech.android.database.entity.TimelineEntity
import com.tatumgames.tatumtech.android.database.repository.AttendeeDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.enums.TimelineType
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import kotlinx.coroutines.launch

@Composable
fun AttendeesScreen(
    navController: NavController,
    eventId: Long
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val attendeeRepository = remember { AttendeeDatabaseRepository(db.attendeeDao()) }
    val timelineRepository = remember { TimelineDatabaseRepository(db.timelineDao()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var attendees by remember { mutableStateOf<List<AttendeeEntity>>(emptyList()) }

    LaunchedEffect(eventId) {
        Log.d("AttendeesScreen", "Event ID: $eventId")
        attendees = attendeeRepository.getAttendeesForEvent(eventId)
        Log.d("AttendeesScreen", "Found ${attendees.size} attendees for event $eventId")
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.attendees),
                onBackClick = { navController.popBackStack() })
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (attendees.isEmpty()) {
                StandardText(
                    text = "No attendees found",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    items(attendees) { attendee ->
                        AttendeeListEntry(
                            attendee = attendee,
                            onToggleFriend = {
                                scope.launch {
                                    if (attendee.isFriend) {
                                        attendeeRepository.removeFriend(attendee.id)

                                        val removedText = context.getString(
                                            R.string.snackbar_removed_friend,
                                            attendee.name
                                        )
                                        snackbarHostState.showSnackbar(removedText)

                                        timelineRepository.insertTimelineEvent(
                                            TimelineEntity(
                                                type = TimelineType.FRIEND_REMOVE.typeValue,
                                                description = context.getString(
                                                    R.string.timeline_removed_friend,
                                                    attendee.name
                                                ),
                                                relatedId = attendee.id,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    } else {
                                        attendeeRepository.addFriend(attendee.id)

                                        val addedText = context.getString(
                                            R.string.snackbar_added_friend,
                                            attendee.name
                                        )
                                        snackbarHostState.showSnackbar(addedText)

                                        timelineRepository.insertTimelineEvent(
                                            TimelineEntity(
                                                type = TimelineType.FRIEND_ADD.typeValue,
                                                description = context.getString(
                                                    R.string.timeline_added_friend,
                                                    attendee.name
                                                ),
                                                relatedId = attendee.id,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                    attendees = attendeeRepository.getAttendeesForEvent(eventId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
