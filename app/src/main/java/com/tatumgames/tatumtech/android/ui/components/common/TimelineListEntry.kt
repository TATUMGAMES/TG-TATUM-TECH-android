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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatumgames.tatumtech.android.database.entity.TimelineEntity
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.utils.Utils.formatTimestamp

@Composable
fun TimelineListEntry(
    item: TimelineEntity,
    isLast: Boolean
) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.Top) {
        // Timeline indicator
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Purple500, shape = MaterialTheme.shapes.small)
            )
            if (!isLast) {
                Spacer(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .background(Color.LightGray)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        // Content
        Column {
            Text(item.description, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                formatTimestamp(context, item.timestamp),
                color = Purple500,
                fontSize = 12.sp
            )
        }
    }
}
