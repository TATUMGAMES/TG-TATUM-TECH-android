package com.tatumgames.tatumtech.android.ui.components.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.entity.AttendeeEntity
import com.tatumgames.tatumtech.android.enums.ProfileImage
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Attendee

@Composable
fun AttendeeListEntry(
    attendee: AttendeeEntity,
    onToggleFriend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AttendeeProfileIcon(
            attendee = Attendee(
                id = attendee.id,
                name = attendee.name,
                profileImage = ProfileImage.fromValue(attendee.profileImage ?: "")
            ),
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
                containerColor = if (attendee.isFriend) {
                    Color(0xFFD32F2F)
                } else {
                    colorResource(R.color.purple_200)
                },
                contentColor = if (attendee.isFriend) {
                    Color.White
                } else {
                    Color.Black
                }
            ),
            shape = MaterialTheme.shapes.small
        ) {
            StandardText(
                text = if (attendee.isFriend) {
                    stringResource(R.string.remove_friend)
                } else {
                    stringResource(R.string.add_friend)
                }
            )
        }
    }
}
