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
package com.tatumgames.tatumtech.android.ui.components.screens.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tatumgames.tatumtech.android.enums.TimelineFilter

@Composable
fun TimelineFilterBar(
    filter: TimelineFilter,
    onFilterChange: (TimelineFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        FilterChip(
            selected = filter == TimelineFilter.TODAY,
            onClick = { onFilterChange(TimelineFilter.TODAY) },
            label = { Text("Today") }
        )
        Spacer(modifier = Modifier.width(12.dp))
        FilterChip(
            selected = filter == TimelineFilter.WEEK,
            onClick = { onFilterChange(TimelineFilter.WEEK) },
            label = { Text("Last Week") }
        )
        Spacer(modifier = Modifier.width(12.dp))
        FilterChip(
            selected = filter == TimelineFilter.MONTH,
            onClick = { onFilterChange(TimelineFilter.MONTH) },
            label = { Text("Last Month") }
        )
    }
}
