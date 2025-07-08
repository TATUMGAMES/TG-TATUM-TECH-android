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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.ui.components.common.AttendeeListDialog
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.EventCard
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.screens.models.Event
import com.tatumgames.tatumtech.android.utils.MockData
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.repository.EventRegistrationDatabaseRepository
import com.tatumgames.tatumtech.android.database.interfaces.EventRegistrationRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingEventsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repository: EventRegistrationRepository = remember { EventRegistrationDatabaseRepository(db.eventRegistrationDao()) }
    val viewModel: UpcomingEventsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
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

    LaunchedEffect(Unit) {
        isLoading = true
        events = MockData.getMockEvents()
        isLoading = false
    }

    Scaffold(
        topBar = {
            Header(text = "Upcoming Events", onBackClick = { navController.popBackStack() })
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(events) { event ->
                        val isRegistered = registeredEvents.contains(event.eventId)
                        EventCard(
                            event = event,
                            isRegistered = isRegistered,
                            onRegister = {
                                if (!isRegistered) viewModel.onRegister(event.eventId)
                                else viewModel.onUnregister(event.eventId)
                            },
                            onSeeAllAttendees = {
                                navController.navigate("attendees_screen/${event.eventId}")
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
            AttendeeListDialog(
                event = event,
                onDismiss = { showAttendeeList = null },
                onAddFriend = { attendee ->
                    scope.launch {
                        snackbarHostState.showSnackbar("Friend request sent to ${attendee.name}")
                    }
                }
            )
        }
    }
}
