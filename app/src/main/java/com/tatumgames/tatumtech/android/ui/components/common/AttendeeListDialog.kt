package com.tatumgames.tatumtech.android.ui.components.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.screens.AttendeeDisplay
import com.tatumgames.tatumtech.android.ui.components.screens.models.Attendee
import com.tatumgames.tatumtech.android.ui.components.screens.models.Event

@Composable
fun AttendeeListDialog(
    event: Event,
    onDismiss: () -> Unit,
    onAddFriend: ((Attendee) -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StandardText(
                        text = "Attendees",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    StandardText(
                        text = "${event.attendees.size} people",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Attendee list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(event.attendees) { attendee ->
                        AttendeeListItem(
                            attendee = attendee,
                            onAddFriend = onAddFriend
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    StandardText(
                        text = "Close",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendeeListItem(
    attendee: Attendee,
    onAddFriend: ((Attendee) -> Unit)? = null
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
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        
        if (onAddFriend != null) {
            OutlinedButton(
                onClick = { onAddFriend(attendee) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                StandardText(
                    text = "Add Friend",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 