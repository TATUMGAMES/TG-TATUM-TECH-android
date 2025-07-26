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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.ClickableText
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import com.tatumgames.tatumtech.android.utils.Utils.parseDate

@Composable
fun EventCard(
    modifier: Modifier = Modifier,
    event: Event,
    isRegistered: Boolean = false,
    onImageClick: (() -> Unit)? = null,
    onRegister: (() -> Unit)? = null,
    onSeeAllAttendees: ((Event) -> Unit)? = null,
    showSnackbar: ((String) -> Unit)? = null
) {
    var showFullscreenImage by remember { mutableStateOf(false) }
    val isColorBackground = event.featuredImage.startsWith("color://")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val backgroundModifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))

            if (isColorBackground) {
                val color = when (event.featuredImage) {
                    "color://spring_purple" -> Color(0xFFD1C4E9)
                    "color://spring_purple2" -> Color(0xFFB39DDB)
                    else -> Color.LightGray
                }
                Box(modifier = backgroundModifier.background(color))
            } else {
                Box(
                    modifier = backgroundModifier
                        .clickable {
                            onImageClick?.invoke() ?: run { showFullscreenImage = true }
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(event.featuredImage),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay gradient and text
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        StandardText(
                            text = event.name,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        StandardText(
                            text = "Hosted by ${event.host}",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            StandardText(
                text = event.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            StandardText(
                text = "Hosted by ${event.host}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            StandardText(
                text = parseDate(event.date),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            StandardText(
                text = "${event.location} · ${event.durationHours} hours",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                event.attendees.take(5).forEach { attendee ->
                    AttendeeProfileIcon(
                        attendee = attendee,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                if (event.attendees.size > 5) {
                    StandardText(
                        text = "+${event.attendees.size - 5} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                ClickableText(
                    text = stringResource(R.string.see_all_attendees),
                    color = colorResource(R.color.purple_500),
                    style = MaterialTheme.typography.bodySmall,
                    onClick = { onSeeAllAttendees?.invoke(event) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (event.isRegistrationOpen) {
                    Button(
                        onClick = {
                            onRegister?.invoke()
                            showSnackbar?.invoke(
                                if (!isRegistered) "Registered to ${event.name}" else "Unregistered from ${event.name}"
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRegistered) Color(0xFF018786) else colorResource(
                                R.color.purple_200
                            )
                        )
                    ) {
                        StandardText(
                            text = if (isRegistered) "Registered" else stringResource(R.string.register),
                            color = if (isRegistered) Color.White else Color.Black
                        )
                    }
                } else {
                    StandardText(
                        text = stringResource(R.string.registration_closed),
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    // Fullscreen image dialog
    if (showFullscreenImage && !isColorBackground) {
        Dialog(
            onDismissRequest = { showFullscreenImage = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(event.featuredImage),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                // Overlay text
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    StandardText(
                        text = event.name,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    StandardText(
                        text = "Hosted by ${event.host}",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    StandardText(
                        text = parseDate(event.date),
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    StandardText(
                        text = event.location,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
} 
