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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.VirtualSpeaker
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.Grey300
import com.tatumgames.tatumtech.android.ui.theme.Grey500
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.ui.utils.GameMediaResolver
import com.tatumgames.tatumtech.android.ui.utils.JsonImporter
import com.tatumgames.tatumtech.android.utils.Utils.openUrl

@Composable
fun VirtualSpeakersScreen(
    navController: NavController,
    eventId: Long
) {
    val context = LocalContext.current
    val event = remember(eventId) { JsonImporter.loadUpcomingEventById(context, eventId) }
    val speakers = remember(event) { event?.speakersInOrder().orEmpty() }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.virtual_speakers),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = ScreenScaffoldLight
    ) { padding ->
        if (speakers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                StandardText(text = stringResource(R.string.virtual_speakers_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(speakers, key = { it.id }) { speaker ->
                    VirtualSpeakerCard(speaker = speaker)
                }
            }
        }
    }
}

@Composable
private fun VirtualSpeakerCard(speaker: VirtualSpeaker) {
    val context = LocalContext.current
    val imageData = remember(speaker.profileImage) {
        speaker.profileImage?.let { name ->
            GameMediaResolver.resolve(context, "drawable:$name")
        }
    }
    val placeholder = painterResource(R.drawable.profile_male_placeholder_01)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageData)
                    .crossfade(true)
                    .build(),
                contentDescription = if (imageData != null) {
                    stringResource(R.string.cd_speaker_photo, speaker.name)
                } else {
                    stringResource(R.string.cd_speaker_photo_placeholder)
                },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = placeholder,
                error = placeholder,
                fallback = placeholder
            )
            Spacer(modifier = Modifier.height(12.dp))
            StandardText(
                text = speaker.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            speaker.companyName?.takeIf { it.isNotBlank() }?.let {
                StandardText(
                    text = it,
                    color = Grey500,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            speaker.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                StandardText(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            StandardText(
                text = speaker.speakingTopic,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            speaker.speakingSchedule?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                StandardText(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            speaker.timeZone?.takeIf { it.isNotBlank() }?.let {
                StandardText(
                    text = stringResource(R.string.virtual_speakers_timezone, it),
                    color = Grey500,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { openUrl(context, speaker.meetUrl) },
                enabled = speaker.joinEnabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.purple_200),
                    contentColor = Black,
                    disabledContainerColor = Grey300,
                    disabledContentColor = Grey500
                )
            ) {
                StandardText(
                    text = stringResource(R.string.virtual_speakers_join),
                    color = if (speaker.joinEnabled) {
                        White
                    } else {
                        Grey500
                    }
                )
            }
        }
    }
}
