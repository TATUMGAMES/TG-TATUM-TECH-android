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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Attendee

@Composable
fun AttendeeItem(
    attendee: Attendee,
    onAddFriend: ((Attendee) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Yellow),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AttendeeProfileIcon(
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
                    text = stringResource(id = R.string.add_friend),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
