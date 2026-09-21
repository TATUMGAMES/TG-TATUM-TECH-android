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

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.ContactCardEntity
import com.tatumgames.tatumtech.android.database.entity.TimelineEntity
import com.tatumgames.tatumtech.android.database.repository.ContactCardDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.UserDatabaseRepository
import com.tatumgames.tatumtech.android.enums.TimelineType
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.networking.models.ContactCardQrCodec
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.utils.QrCodeBitmapGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MyContactCardQrScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val cardRepository = remember { ContactCardDatabaseRepository(db.contactCardDao()) }
    val userRepository = remember { UserDatabaseRepository(db.userDao()) }
    val timelineRepository = remember { TimelineDatabaseRepository(db.timelineDao()) }

    var card by remember { mutableStateOf<ContactCardEntity?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Log share once per screen entry (not on recomposition).
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val user = userRepository.getCurrentUser()
            val loaded = user?.let { cardRepository.getByOwnerUserId(it.id) }
            card = loaded
            if (loaded != null && user != null) {
                val payload = ContactCardQrCodec.fromCard(loaded, user.anonymousId)
                qrBitmap = QrCodeBitmapGenerator.generate(ContactCardQrCodec.encode(payload))
                timelineRepository.insertTimelineEvent(
                    TimelineEntity(
                        type = TimelineType.CONTACT_CARD_SHARED.typeValue,
                        description = context.getString(R.string.timeline_contact_card_shared),
                        relatedId = null,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.contact_card_my_qr_title),
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                card == null || qrBitmap == null -> {
                    StandardText(
                        text = stringResource(R.string.contact_card_qr_error),
                        textAlign = TextAlign.Center
                    )
                }

                else -> {
                    val loaded = card!!
                    if (!loaded.profileImageUri.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(loaded.profileImageUri),
                            contentDescription = stringResource(R.string.contact_card_photo),
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    StandardText(
                        text = loaded.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                    val subtitle = listOfNotNull(loaded.jobTitle, loaded.company)
                        .joinToString(" · ")
                    if (subtitle.isNotBlank()) {
                        StandardText(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = stringResource(R.string.contact_card_my_qr_title),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(White)
                            .padding(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    StandardText(
                        text = stringResource(R.string.contact_card_scan_instruction),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
