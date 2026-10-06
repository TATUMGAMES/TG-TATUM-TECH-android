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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.reminders.MeetingReminders

/**
 * One-time explanation shown after sign-in on Android 13+, followed by the system notification
 * permission request. Never shown again once answered, or when permission is already granted.
 */
@Composable
fun NotificationPermissionPrompt() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val store = remember { MeetingReminders.store(context) }
    var showExplanation by remember {
        mutableStateOf(
            !store.notificationPermissionRequested &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    if (!showExplanation) return

    fun answer(requestPermission: Boolean) {
        store.notificationPermissionRequested = true
        showExplanation = false
        if (requestPermission) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    AlertDialog(
        onDismissRequest = { answer(requestPermission = false) },
        title = { StandardText(text = stringResource(R.string.notification_permission_title)) },
        text = { StandardText(text = stringResource(R.string.notification_permission_message)) },
        confirmButton = {
            TextButton(onClick = { answer(requestPermission = true) }) {
                StandardText(text = stringResource(R.string.notification_permission_allow))
            }
        },
        dismissButton = {
            TextButton(onClick = { answer(requestPermission = false) }) {
                StandardText(text = stringResource(R.string.notification_permission_not_now))
            }
        }
    )
}
