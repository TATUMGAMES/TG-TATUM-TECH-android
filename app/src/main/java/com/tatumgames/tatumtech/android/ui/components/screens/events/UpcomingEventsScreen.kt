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
import androidx.compose.runtime.derivedStateOf
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.AttendeeEntity
import com.tatumgames.tatumtech.android.database.entity.TimelineEntity
import com.tatumgames.tatumtech.android.database.entity.UserEntity
import com.tatumgames.tatumtech.android.database.interfaces.EventRegistrationInterface
import com.tatumgames.tatumtech.android.database.repository.AttendeeDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.EventRegistrationDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.UserDatabaseRepository
import com.tatumgames.tatumtech.android.enums.ProfileImage
import com.tatumgames.tatumtech.android.enums.TimelineType
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Attendee
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import com.tatumgames.tatumtech.android.ui.components.screens.events.viewmodels.UpcomingEventsViewModel
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.utils.MockData
import com.tatumgames.tatumtech.android.utils.Utils.getUserNameOrAnonymous
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingEventsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    
    // Use remember for stable database and repository instances
    val db = remember { AppDatabase.getInstance(context) }
    val repository: EventRegistrationInterface = remember { 
        EventRegistrationDatabaseRepository(db.eventRegistrationDao()) 
    }
    val attendeeRepository = remember { AttendeeDatabaseRepository(db.attendeeDao()) }
    val timelineRepository = remember { TimelineDatabaseRepository(db.timelineDao()) }
    val userRepository = remember { UserDatabaseRepository(db.userDao()) }
    
    val viewModel: UpcomingEventsViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return UpcomingEventsViewModel(repository) as T
        }
    })
    
    val registeredEvents by viewModel.registeredEvents.collectAsState()
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAttendeeList by remember { mutableStateOf<Event?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Use derivedStateOf to create stable state for registered events
    val registeredEventsSet by remember(registeredEvents) {
        derivedStateOf { registeredEvents.toSet() }
    }

    // Create stable event list to prevent unnecessary recompositions
    val stableEvents by remember(events) {
        derivedStateOf { events }
    }

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
                    items(
                        items = stableEvents,
                        key = { event -> event.id } // Add stable keys for better performance
                    ) { event ->
                        val isRegistered = registeredEventsSet.contains(event.id)
                        EventCard(
                            event = event,
                            isRegistered = isRegistered,
                            onRegister = {
                                scope.launch {
                                    if (!isRegistered) {
                                        viewModel.onRegister(event.id)
                                        
                                        // Add current user to attendee list
                                        val currentUser = userRepository.getCurrentUser()
                                        if (currentUser != null) {
                                            val displayName = getUserNameOrAnonymous(currentUser)
                                            val userAttendee = AttendeeEntity(
                                                id = System.currentTimeMillis(), // Generate unique ID
                                                eventId = event.id,
                                                name = displayName,
                                                profileImage = null,
                                                isFriend = false
                                            )
                                            attendeeRepository.insertAttendee(userAttendee)
                                        }
                                        
                                        // Log registration to timeline
                                        val timelineRegistered = context.getString(R.string.timeline_registered, event.name)
                                        timelineRepository.insertTimelineEvent(
                                            TimelineEntity(
                                                type = TimelineType.EVENT_REGISTRATION.typeValue,
                                                description = timelineRegistered,
                                                relatedId = event.id,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    } else {
                                        viewModel.onUnregister(event.id)
                                        
                                        // Remove current user from attendee list
                                        val currentUser = userRepository.getCurrentUser()
                                        if (currentUser != null) {
                                            val displayName = getUserNameOrAnonymous(currentUser)
                                            val userAttendee = attendeeRepository.getAttendeesForEvent(event.id)
                                                .find { it.name == displayName }
                                            userAttendee?.let {
                                                attendeeRepository.removeAttendee(it.id)
                                            }
                                        }
                                        
                                        // Log un-registration to timeline
                                        val timelineUnregistered = context.getString(R.string.timeline_unregistered, event.name)
                                        timelineRepository.insertTimelineEvent(
                                            TimelineEntity(
                                                type = TimelineType.EVENT_UNREGISTRATION.typeValue,
                                                description = timelineUnregistered,
                                                relatedId = event.id,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                    
                                    // Refresh events list to show updated attendees
                                    val updatedEvents = events.map { e ->
                                        val dbAttendees = attendeeRepository.getAttendeesForEvent(e.id)
                                        e.copy(attendees = dbAttendees.map {
                                            Attendee(
                                                id = it.id,
                                                name = it.name,
                                                profileImage = ProfileImage.fromValue(it.profileImage ?: "")
                                            )
                                        })
                                    }
                                    events = updatedEvents
                                }
                            },
                            onSeeAllAttendees = {
                                navController.navigate(NavRoutes.attendeesScreenRoute(event.id))
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
