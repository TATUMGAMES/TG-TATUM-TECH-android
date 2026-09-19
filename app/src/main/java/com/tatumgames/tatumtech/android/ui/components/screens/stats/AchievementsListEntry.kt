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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.models.Achievement
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.Grey500

@Composable
fun AchievementsListEntry(achievement: Achievement) {
    val context = LocalContext.current
    val iconRes = AchievementIconResolver.resolve(context, achievement.icon)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = achievement.title,
            modifier = Modifier.size(40.dp),
            alpha = if (achievement.isUnlocked) 1f else 0.3f
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            StandardText(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (achievement.isUnlocked) FontWeight.Bold else FontWeight.Medium
                ),
                textAlign = TextAlign.Start
            )
            StandardText(
                text = achievement.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (achievement.isUnlocked) {
                    Black
                } else {
                    Grey500
                },
                textAlign = TextAlign.Start
            )
        }
    }
}
