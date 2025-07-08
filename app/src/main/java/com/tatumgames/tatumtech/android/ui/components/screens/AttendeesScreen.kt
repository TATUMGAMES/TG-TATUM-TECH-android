package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.AttendeeDisplay
import com.tatumgames.tatumtech.android.ui.components.screens.models.Attendee
import com.tatumgames.tatumtech.android.ui.components.screens.models.Event
import com.tatumgames.tatumtech.android.utils.MockData
import kotlinx.coroutines.launch

@Composable
fun AttendeesScreen(
    navController: NavController,
    eventId: String
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // Get the event from MockData
    val event = remember { MockData.getMockEvents().find {
        it.eventId == eventId }
    }

    // Use explicit MutableState holding an immutable Map<String, Boolean>
    val friendStates = remember {
        mutableStateOf(
            event?.attendees
                ?.associate { it.userId to false }
                ?: emptyMap()
        )
    }

    Scaffold(
        topBar = {
            Header(text = "Attendees", onBackClick = { navController.popBackStack() })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (event == null) {
                StandardText(
                    text = "Event not found",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(event.attendees) { attendee ->
                        AttendeeListItem(
                            attendee = attendee,
                            isFriend = friendStates.value[attendee.userId] == true,
                            onToggleFriend = {
                                val newState = !(friendStates.value[attendee.userId] ?: false)
                                friendStates.value = friendStates.value.toMutableMap().apply {
                                    put(attendee.userId, newState)
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (newState) "Added ${attendee.name} as a friend"
                                        else "Removed ${attendee.name} from friends"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendeeListItem(
    attendee: Attendee,
    isFriend: Boolean,
    onToggleFriend: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AttendeeDisplay(
            attendee = attendee,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        StandardText(
            text = attendee.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onToggleFriend,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFriend) {
                    Color(0xFFD32F2F)
                } else {
                    colorResource(R.color.purple_200)
                },
                contentColor = if (isFriend) {
                    Color.White
                } else {
                    Color.Black
                }
            ),
            shape = MaterialTheme.shapes.small
        ) {
            StandardText(text = if (isFriend) {
                "Remove Friend"
            } else {
                "Add Friend"
            })
        }
    }
} 
