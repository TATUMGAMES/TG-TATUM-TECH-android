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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.Grey300
import com.tatumgames.tatumtech.android.ui.theme.Grey500
import com.tatumgames.tatumtech.android.ui.theme.SpringPurple100
import com.tatumgames.tatumtech.android.ui.theme.SpringPurple300
import com.tatumgames.tatumtech.android.ui.theme.Transparent
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.ui.utils.GameMediaResolver
import com.tatumgames.tatumtech.android.utils.Utils.openUrl
import com.tatumgames.tatumtech.android.utils.Utils.parseDate

@Composable
fun EventCard(
    modifier: Modifier = Modifier,
    event: Event,
    onVirtualSpeakersClick: ((Event) -> Unit)? = null,
    onImageClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showFullscreenImage by remember { mutableStateOf(false) }
    val isColorBackground = event.featuredImage.startsWith("color://")
    val featuredImageData = remember(event.featuredImage) {
        when {
            isColorBackground -> null
            event.featuredImage.startsWith("http", ignoreCase = true) ||
                    event.featuredImage.startsWith("android.resource://") -> event.featuredImage

            event.featuredImage.startsWith("drawable:") ->
                GameMediaResolver.resolve(context, event.featuredImage)

            else -> GameMediaResolver.resolve(context, "drawable:${event.featuredImage}")
                ?: event.featuredImage
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val backgroundModifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))

            if (isColorBackground) {
                val color = when (event.featuredImage) {
                    "color://spring_purple" -> SpringPurple100
                    "color://spring_purple2" -> SpringPurple300
                    else -> Grey300
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
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(featuredImageData)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = event.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Transparent,
                                        Black.copy(alpha = 0.7f)
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
                            color = White,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        StandardText(
                            text = "Hosted by ${event.host}",
                            color = White.copy(alpha = 0.9f),
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
                color = Grey500,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            StandardText(
                text = parseDate(event.date),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            StandardText(
                text = "${event.location} · ${event.durationHours} hours",
                color = Grey500,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { openUrl(context, event.lumaUrl) },
                    enabled = event.registerEnabled,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.purple_200),
                        contentColor = White,
                        disabledContainerColor = Grey300,
                        disabledContentColor = Grey500
                    )
                ) {
                    StandardText(
                        text = stringResource(R.string.register),
                        color = if (event.registerEnabled) White else Grey500
                    )
                }
                if (event.hasVirtualSpeakers) {
                    Button(
                        onClick = { onVirtualSpeakersClick?.invoke(event) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.purple_200),
                            contentColor = White
                        )
                    ) {
                        StandardText(
                            text = stringResource(R.string.virtual_speakers),
                            color = White
                        )
                    }
                }
            }
        }
    }

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
                    .background(Black)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(featuredImageData),
                    contentDescription = event.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    StandardText(
                        text = event.name,
                        color = White,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    StandardText(
                        text = "Hosted by ${event.host}",
                        color = White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    StandardText(
                        text = parseDate(event.date),
                        color = White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    StandardText(
                        text = event.location,
                        color = White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
