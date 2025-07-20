package com.tatumgames.tatumtech.android.ui.components.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgressRing(label: String, value: Int, max: Int, color: Color) {
    val progress = (value / max.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "ProgressAnimation"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(64.dp),
            color = color,
            strokeWidth = 8.dp
        )
        Spacer(modifier = Modifier.height(8.dp))
        StandardText(text = "$value", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        StandardText(text = label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = Color.Gray)
    }
}
