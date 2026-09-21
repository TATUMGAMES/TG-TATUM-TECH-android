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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.theme.Grey500
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.ui.theme.White

/**
 * Compact bar list of completed challenges by category/language.
 * Hides itself when [totalCompletions] is 0 (no placeholder chart).
 */
@Composable
fun CategoryBreakdownView(
    categoryCounts: Map<String, Int>,
    totalCompletions: Int,
    modifier: Modifier = Modifier
) {
    if (totalCompletions <= 0) {
        StandardText(
            text = stringResource(R.string.category_breakdown_empty),
            color = Grey500,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth()
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categoryCounts.forEach { (category, count) ->
            CategoryBarRow(
                label = category,
                count = count,
                maxCount = totalCompletions.coerceAtLeast(1)
            )
        }
    }
}

@Composable
private fun CategoryBarRow(
    label: String,
    count: Int,
    maxCount: Int
) {
    val fraction = (count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StandardText(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.width(110.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Grey500.copy(alpha = 0.2f))
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Purple500)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        StandardText(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(24.dp)
        )
    }
}
