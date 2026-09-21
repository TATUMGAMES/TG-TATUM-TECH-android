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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.tatumgames.tatumtech.android.ui.theme.White

/**
 * Reusable alert dialog that accepts localized title, description, and CTA labels
 * from the caller (typically resolved via [androidx.compose.ui.res.stringResource]).
 *
 * @param title Dialog title text.
 * @param description Dialog body text.
 * @param confirmButtonText Primary CTA label.
 * @param onConfirm Invoked when the confirm CTA is pressed.
 * @param dismissButtonText Optional secondary CTA label. When null, no dismiss button is shown.
 * @param onDismiss Invoked when the dismiss CTA is pressed. Required when [dismissButtonText] is set.
 * @param onDismissRequest Invoked when the dialog requests dismissal (e.g. back / outside tap).
 * @param containerColor Dialog surface color.
 */
@Composable
fun StandardAlertDialog(
    title: String,
    description: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null,
    onDismissRequest: () -> Unit = {},
    containerColor: Color = White
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            StandardText(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            StandardText(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            RoundedButton(
                text = confirmButtonText,
                modifier = Modifier.fillMaxWidth(),
                onClick = onConfirm
            )
        },
        dismissButton = if (dismissButtonText != null && onDismiss != null) {
            {
                OutlinedButton(
                    text = dismissButtonText,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss
                )
            }
        } else {
            null
        },
        containerColor = containerColor
    )
}
