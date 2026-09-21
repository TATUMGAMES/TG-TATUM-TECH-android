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
package com.tatumgames.tatumtech.android.ui.components.screens.networking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.theme.White

@Composable
fun NetworkingContactSection(
    hasCard: Boolean,
    onCreateOrEdit: () -> Unit,
    onShare: () -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StandardText(
                text = stringResource(R.string.networking_share_contact_info),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasCard) {
                    RoundedButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.networking_edit),
                        onClick = onCreateOrEdit
                    )
                    RoundedButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.networking_share),
                        onClick = onShare
                    )
                    RoundedButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.networking_scan),
                        onClick = onScan
                    )
                } else {
                    RoundedButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.networking_create),
                        onClick = onCreateOrEdit
                    )
                    RoundedButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.networking_scan),
                        onClick = onScan
                    )
                }
            }
        }
    }
}
