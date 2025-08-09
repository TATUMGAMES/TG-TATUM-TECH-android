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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.enums.ProfileImage
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Attendee
import com.tatumgames.tatumtech.android.utils.Utils.generateColorFromString
import com.tatumgames.tatumtech.android.utils.Utils.getNameInitials

@Composable
fun AttendeeProfileIcon(
    attendee: Attendee,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val attendeeColor = generateColorFromString(attendee.id.toString())

    val shouldShowInitials =
        attendee.profileImage == ProfileImage.INITIALS || attendee.id.hashCode() % 5 == 0

    if (shouldShowInitials) {
        val initials = getNameInitials(context, attendee.name)

        Box(
            modifier = modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(attendeeColor)
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        val drawableId = when (attendee.profileImage) {
            ProfileImage.MALE_PLACEHOLDER_01 -> R.drawable.profile_male_placeholder_01
            ProfileImage.MALE_PLACEHOLDER_02 -> R.drawable.profile_male_placeholder_02
            ProfileImage.MALE_PLACEHOLDER_03 -> R.drawable.profile_male_placeholder_03
            ProfileImage.FEMALE_PLACEHOLDER_01 -> R.drawable.profile_female_placeholder_01
            ProfileImage.FEMALE_PLACEHOLDER_02 -> R.drawable.profile_female_placeholder_02
            ProfileImage.FEMALE_PLACEHOLDER_03 -> R.drawable.profile_female_placeholder_03
            else -> R.drawable.profile_male_placeholder_01
        }

        Image(
            painter = painterResource(id = drawableId),
            contentDescription = null,
            modifier = modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(1.dp, attendeeColor, CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

