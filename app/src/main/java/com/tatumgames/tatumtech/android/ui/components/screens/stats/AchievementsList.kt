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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.stats.models.Achievement

@Composable
fun AchievementsList(achievements: List<Achievement>) {
    val unlocked = achievements.filter { it.unlocked }
    val locked = achievements.filter { !it.unlocked }
    Column(
    modifier = Modifier
    .fillMaxWidth()
    .verticalScroll(rememberScrollState())
    .background(Color.White, RoundedCornerShape(12.dp))
    .padding(12.dp)
    ) {
        unlocked.forEach { achievement ->
            AchievementsListEntry(achievement)
        }
        if (locked.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            StandardText(
                text = "Locked Achievements",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                color = Color.Gray
            )
            locked.forEach { achievement ->
                AchievementsListEntry(achievement)
            }
        }
    }
}
