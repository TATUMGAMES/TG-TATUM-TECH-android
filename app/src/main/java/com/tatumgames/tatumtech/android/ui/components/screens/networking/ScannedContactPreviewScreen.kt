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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.TimelineEntity
import com.tatumgames.tatumtech.android.database.repository.ConnectionDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.ContactCardDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.SaveConnectionResult
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.UserDatabaseRepository
import com.tatumgames.tatumtech.android.enums.TimelineType
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.OutlinedButton
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.networking.models.ContactCardQrCodec
import com.tatumgames.tatumtech.android.ui.components.screens.networking.models.ContactCardQrPayload
import com.tatumgames.tatumtech.android.ui.theme.Grey500
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScannedContactPreviewScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val db = remember { AppDatabase.getInstance(context) }
    val connectionRepository = remember { ConnectionDatabaseRepository(db.connectionDao()) }
    val contactCardRepository = remember { ContactCardDatabaseRepository(db.contactCardDao()) }
    val userRepository = remember { UserDatabaseRepository(db.userDao()) }
    val timelineRepository = remember { TimelineDatabaseRepository(db.timelineDao()) }

    var payload by remember { mutableStateOf<ContactCardQrPayload?>(null) }

    LaunchedEffect(Unit) {
        payload = ContactCardScanSession.consume()
    }

    fun navigateBack() {
        if (!navController.popBackStack()) {
            // No prior destination — stay put rather than crashing the flow.
        }
    }

    BackHandler { navigateBack() }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.contact_found_title),
                onBackClick = { navigateBack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ScreenScaffoldLight
    ) { padding ->
        val card = payload
        if (card == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            ) {
                StandardText(text = stringResource(R.string.contact_invalid_qr))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    text = stringResource(R.string.contact_cancel),
                    onClick = { navigateBack() }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StandardText(
                    text = card.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                card.jobTitle?.takeIf { it.isNotBlank() }?.let {
                    StandardText(text = it, style = MaterialTheme.typography.bodyLarge)
                }
                card.company?.takeIf { it.isNotBlank() }?.let {
                    StandardText(text = it, color = Grey500)
                }
                card.description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    StandardText(text = it)
                }
                listOf(
                    card.email,
                    card.phone,
                    card.alternateEmail,
                    card.website,
                    card.linkedin,
                    card.twitter,
                    card.customLink,
                    card.calendly
                ).mapNotNull { it?.takeIf { value -> value.isNotBlank() } }
                    .forEach { StandardText(text = it) }

                Spacer(modifier = Modifier.height(24.dp))
                RoundedButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.contact_save),
                    onClick = {
                        scope.launch {
                            val message = withContext(Dispatchers.IO) {
                                val user = userRepository.getCurrentUser()
                                    ?: return@withContext context.getString(R.string.contact_invalid_qr)
                                val localCard = contactCardRepository.getByOwnerUserId(user.id)
                                if (ContactCardQrCodec.isOwnCard(card, user, localCard?.cardId)) {
                                    return@withContext context.getString(R.string.contact_cannot_save_own)
                                }
                                val connection = ContactCardQrCodec.toConnection(user.id, card)
                                when (val result = connectionRepository.saveOrUpdate(connection)) {
                                    is SaveConnectionResult.Created -> {
                                        timelineRepository.insertTimelineEvent(
                                            TimelineEntity(
                                                type = TimelineType.CONNECTION_MADE.typeValue,
                                                description = context.getString(
                                                    R.string.timeline_connection_made,
                                                    card.name
                                                ),
                                                relatedId = result.connection.id,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                        timelineRepository.insertTimelineEvent(
                                            TimelineEntity(
                                                type = TimelineType.QR_SCAN.typeValue,
                                                description = context.getString(
                                                    R.string.timeline_connection_made,
                                                    card.name
                                                ),
                                                relatedId = result.connection.id,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                        context.getString(R.string.contact_saved, card.name)
                                    }

                                    is SaveConnectionResult.Updated -> {
                                        context.getString(R.string.contact_already_saved, card.name)
                                    }
                                }
                            }

                            val isOwnCardError =
                                message == context.getString(R.string.contact_cannot_save_own)
                            if (isOwnCardError) {
                                snackbarHostState.showSnackbar(message)
                                return@launch
                            }

                            val opened = ContactCardQrCodec.launchInsertContact(context, card)
                            if (!opened) {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.contact_android_open_failed)
                                )
                                return@launch
                            }
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.contact_cancel),
                    onClick = { navigateBack() }
                )
            }
        }
    }
}
