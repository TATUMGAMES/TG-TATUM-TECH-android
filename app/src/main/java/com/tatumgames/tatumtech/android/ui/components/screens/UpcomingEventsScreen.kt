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
package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.AttendeeEntity
import com.tatumgames.tatumtech.android.database.entity.TimelineEntity
import com.tatumgames.tatumtech.android.database.interfaces.EventRegistrationInterface
import com.tatumgames.tatumtech.android.database.repository.AttendeeDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.EventRegistrationDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.enums.ProfileImage
import com.tatumgames.tatumtech.android.enums.TimelineType
import com.tatumgames.tatumtech.android.ui.components.common.AttendeeList
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.EventCard
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.screens.models.Attendee
import com.tatumgames.tatumtech.android.ui.components.screens.models.Event
import com.tatumgames.tatumtech.android.ui.components.screens.viewmodels.UpcomingEventsViewModel
import com.tatumgames.tatumtech.android.utils.MockData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingEventsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repository: EventRegistrationInterface =
        remember { EventRegistrationDatabaseRepository(db.eventRegistrationDao()) }
    val viewModel: UpcomingEventsViewModel =
        viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return UpcomingEventsViewModel(repository) as T
            }
        })
    val registeredEvents by viewModel.registeredEvents.collectAsState()
    val attendeeRepository = remember { AttendeeDatabaseRepository(db.attendeeDao()) }
    val timelineRepository = remember { TimelineDatabaseRepository(db.timelineDao()) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAttendeeList by remember { mutableStateOf<Event?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        val mockEvents = MockData.getMockEvents(context)
        // For each event, check if attendees exist in DB; if not, generate and insert
        mockEvents.forEach { event ->
            val dbAttendees = attendeeRepository.getAttendeesForEvent(event.id)
            if (dbAttendees.isEmpty()) {
                val attendees = event.attendees.map {
                    AttendeeEntity(
                        id = it.id,
                        eventId = event.id,
                        name = it.name,
                        profileImage = it.profileImage.assetName,
                        isFriend = false
                    )
                }
                attendeeRepository.insertAttendees(attendees)
            }
        }
        // Now fetch all events with attendees from DB
        events = mockEvents.map { event ->
            val dbAttendees = attendeeRepository.getAttendeesForEvent(event.id)
            event.copy(attendees = dbAttendees.map {
                Attendee(
                    id = it.id,
                    name = it.name,
                    profileImage = ProfileImage.fromValue(it.profileImage ?: "")
                )
            })
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.upcoming_events),
                onBackClick = { navController.popBackStack() })
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(events) { event ->
                        val isRegistered = registeredEvents.contains(event.id)
                        EventCard(
                            event = event,
                            isRegistered = isRegistered,
                            onRegister = {
                                if (!isRegistered) {
                                    viewModel.onRegister(event.id)
                                    // Log registration to timeline
                                    val timelineRegistered = context.getString(R.string.timeline_registered, event.name)
                                    scope.launch {
                                        timelineRepository.insertTimelineEvent(
                                            TimelineEntity(
                                                type = TimelineType.EVENT_REGISTRATION.typeValue,
                                                description = timelineRegistered,
                                                relatedId = event.id,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                } else {
                                    viewModel.onUnregister(event.id)
                                    // Log un-registration to timeline
                                    val timelineUnregistered = context.getString(R.string.timeline_unregistered, event.name)
                                    scope.launch {
                                        timelineRepository.insertTimelineEvent(
                                            TimelineEntity(
                                                type = TimelineType.EVENT_UNREGISTRATION.typeValue,
                                                description = timelineUnregistered,
                                                relatedId = event.id,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }
                            },
                            onSeeAllAttendees = {
                                navController.navigate("attendees_screen/${event.id}")
                            },
                            showSnackbar = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Attendee list dialog
        showAttendeeList?.let { event ->
            AttendeeList(
                event = event,
                onDismiss = { showAttendeeList = null },
                onAddFriend = { attendee ->
                    val message = context.getString(R.string.friend_request_sent, attendee.name)
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            )
        }
    }
}
