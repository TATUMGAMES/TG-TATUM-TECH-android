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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tatumgames.tatumtech.android.R

@Composable
fun CameraPreviewWithControls(
    onSwitchCamera: () -> Unit,
    onCapture: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Placeholder for camera preview
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera Preview", color = Color.White)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onSwitchCamera) {
                Image(
                    painter = painterResource(id = R.drawable.forward_arrow),
                    contentDescription = "Switch Camera",
                    modifier = Modifier.size(36.dp)
                )
            }
            IconButton(onClick = onCapture) {
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Capture",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
    // TODO: Integrate QR code scanning and image capture logic
    // On successful scan, call a callback to update My Timeline
}
