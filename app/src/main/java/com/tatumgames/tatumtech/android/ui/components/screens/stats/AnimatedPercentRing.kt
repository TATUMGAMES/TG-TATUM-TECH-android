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
package com.tatumgames.tatumtech.android.ui.components.screens.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.theme.Teal700

@Composable
fun AnimatedPercentRing(percent: Int) {
    val clampedPercent = percent.coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedPercent / 100f,
        label = "PercentProgressAnimation"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(100.dp),
            color = Teal700,
            strokeWidth = 10.dp
        )
        StandardText(
            text = "$clampedPercent%",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        )
    }
}
