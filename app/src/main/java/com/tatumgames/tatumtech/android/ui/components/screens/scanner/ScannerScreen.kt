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
package com.tatumgames.tatumtech.android.ui.components.screens.scanner

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.TimelineEntity
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.enums.TimelineType
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.networking.ContactCardScanSession
import com.tatumgames.tatumtech.android.ui.components.screens.networking.models.ContactCardQrCodec
import com.tatumgames.tatumtech.android.ui.components.screens.networking.models.ContactCardQrParseResult
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.utils.Utils.hasPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @param returnToUpcomingEvents when true, back / successful contact scan returns to Upcoming Events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    navController: NavController,
    returnToUpcomingEvents: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scanHandled by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    val db = remember { AppDatabase.getInstance(context) }
    val timelineRepository = remember { TimelineDatabaseRepository(db.timelineDao()) }

    LaunchedEffect(Unit) {
        hasCameraPermission = hasPermissions(context, Manifest.permission.CAMERA)
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun onBack() {
        if (returnToUpcomingEvents) {
            val popped = navController.popBackStack(
                NavRoutes.UPCOMING_EVENTS_SCREEN,
                inclusive = false
            )
            if (!popped) navController.popBackStack()
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.scanner),
                backArrowTint = White,
                onBackClick = { onBack() })
        },
        bottomBar = {},
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Black),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                CameraPreviewWithControls(
                    onBarcodeDetected = { raw ->
                        if (scanHandled) return@CameraPreviewWithControls
                        scanHandled = true
                        scope.launch {
                            when (val parsed = ContactCardQrCodec.parse(raw)) {
                                is ContactCardQrParseResult.Success -> {
                                    ContactCardScanSession.setPending(parsed.payload)
                                    navController.navigate(NavRoutes.SCANNED_CONTACT_PREVIEW) {
                                        launchSingleTop = true
                                    }
                                }

                                is ContactCardQrParseResult.UnsupportedVersion -> {
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.contact_unsupported_qr)
                                    )
                                    scanHandled = false
                                }

                                ContactCardQrParseResult.Invalid -> {
                                    withContext(Dispatchers.IO) {
                                        timelineRepository.insertTimelineEvent(
                                            TimelineEntity(
                                                type = TimelineType.QR_SCAN.typeValue,
                                                description = "Scanned QR code",
                                                relatedId = null,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.contact_invalid_qr)
                                    )
                                    scanHandled = false
                                }
                            }
                        }
                    }
                )
            } else {
                StandardText(
                    text = stringResource(R.string.camera_permission_required),
                    color = White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
