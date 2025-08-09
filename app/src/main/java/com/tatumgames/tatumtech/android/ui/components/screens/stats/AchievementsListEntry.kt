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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.stats.models.Achievement

@Composable
fun AchievementsListEntry(achievement: Achievement) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Image(
            painter = painterResource(id = achievement.iconRes),
            contentDescription = achievement.name,
            modifier = Modifier.size(32.dp),
            alpha = if (achievement.unlocked) {
                1f
            } else {
                0.3f
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            StandardText(
                text = achievement.name,
                style = TextStyle(
                    fontWeight = if (achievement.unlocked) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )
            )
            StandardText(
                text = achievement.description,
                style = TextStyle(
                    fontSize = 13.sp
                ),
                color = if (achievement.unlocked) {
                    Color.Black
                } else {
                    Color.Gray
                }
            )
        }
    }
} 
