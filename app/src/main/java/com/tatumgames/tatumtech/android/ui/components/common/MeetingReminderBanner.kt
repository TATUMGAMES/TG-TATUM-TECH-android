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
package com.tatumgames.tatumtech.android.ui.components.common

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.reminders.MeetingReminder
import com.tatumgames.tatumtech.android.reminders.MeetingReminderBanners
import com.tatumgames.tatumtech.android.reminders.meetingReminderTitle
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.Purple200
import com.tatumgames.tatumtech.android.ui.theme.White
import kotlinx.coroutines.delay

/**
 * App-wide host for the meeting reminder banner. Place it once above the navigation content;
 * it slides in from the top whenever [MeetingReminderBanners] has a reminder, stays until the
 * user dismisses or taps it, and never affects the layout underneath.
 *
 * @param onOpen Called when the banner is tapped; the banner is dismissed first.
 */
@Composable
fun MeetingReminderBannerHost(
    onOpen: (MeetingReminder) -> Unit,
    modifier: Modifier = Modifier
) {
    val reminder by MeetingReminderBanners.current.collectAsState()
    var lastShown by remember { mutableStateOf<MeetingReminder?>(null) }
    val view = LocalView.current
    val animationsEnabled = MotionDefaults.animationsEnabled()

    LaunchedEffect(reminder) {
        val current = reminder ?: return@LaunchedEffect
        lastShown = current
        view.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.CONTEXT_CLICK
            }
        )
    }

    AnimatedVisibility(
        visible = reminder != null,
        modifier = modifier,
        enter = slideInVertically(
            MotionDefaults.durationSpec(animationsEnabled, MotionDefaults.FEEDBACK_ENTER_MS)
        ) { -it } + fadeIn(MotionDefaults.durationSpec(animationsEnabled, MotionDefaults.FEEDBACK_ENTER_MS)),
        exit = slideOutVertically(
            MotionDefaults.durationSpec(animationsEnabled, MotionDefaults.FEEDBACK_EXIT_MS)
        ) { -it } + fadeOut(MotionDefaults.durationSpec(animationsEnabled, MotionDefaults.FEEDBACK_EXIT_MS))
    ) {
        val shown = reminder ?: lastShown ?: return@AnimatedVisibility
        MeetingReminderBanner(
            reminder = shown,
            onClick = {
                MeetingReminderBanners.dismiss(shown)
                onOpen(shown)
            },
            onDismiss = { MeetingReminderBanners.dismiss(shown) }
        )
    }
}

@Composable
private fun MeetingReminderBanner(
    reminder: MeetingReminder,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val now by produceState(System.currentTimeMillis(), reminder) {
        while (true) {
            delay(TITLE_REFRESH_MS)
            value = System.currentTimeMillis()
        }
    }
    Card(
        onClick = onClick,
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Black.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_notification_speaker),
                contentDescription = null,
                tint = Purple200,
                modifier = Modifier.size(28.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                StandardText(
                    text = meetingReminderTitle(context, reminder, now),
                    color = White,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                StandardText(
                    text = reminder.speakingTopic,
                    color = White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                StandardText(
                    text = stringResource(R.string.meeting_reminder_join_hint),
                    color = Purple200,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.meeting_reminder_dismiss),
                    tint = White
                )
            }
        }
    }
}

private const val TITLE_REFRESH_MS = 15_000L
