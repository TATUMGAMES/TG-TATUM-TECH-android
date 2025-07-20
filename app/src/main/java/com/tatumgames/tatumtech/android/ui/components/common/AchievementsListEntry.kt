package com.tatumgames.tatumtech.android.ui.components.common

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatumgames.tatumtech.android.ui.components.screens.models.Achievement

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
            Text(
                achievement.name,
                fontWeight = if (achievement.unlocked) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )
            Text(
                achievement.description,
                fontSize = 13.sp,
                color = if (achievement.unlocked) {
                    Color.Black
                } else {
                    Color.Gray
                }
            )
        }
    }
} 
