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

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
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
import com.tatumgames.tatumtech.android.ui.components.common.OutlinedButton
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.networking.models.ContactCardQrCodec
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import com.tatumgames.tatumtech.android.utils.Utils.getUserNameOrAnonymous
import com.tatumgames.tatumtech.android.utils.Utils.hasPermissions
import com.tatumgames.tatumtech.android.utils.Utils.isEmailValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
fun ContactCardEditorScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val db = remember { AppDatabase.getInstance(context) }
    val cardRepository = remember { ContactCardDatabaseRepository(db.contactCardDao()) }
    val userRepository = remember { UserDatabaseRepository(db.userDao()) }
    val timelineRepository = remember { TimelineDatabaseRepository(db.timelineDao()) }

    var cardId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var isNewCard by remember { mutableStateOf(true) }
    var ownerUserId by remember { mutableStateOf(1L) }
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var alternateEmail by remember { mutableStateOf("") }
    var linkedin by remember { mutableStateOf("") }
    var twitter by remember { mutableStateOf("") }
    var customLink by remember { mutableStateOf("") }
    var calendly by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) profileImageUri = uri.toString()
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { profileImageUri = it.toString() }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.photo_capture_cancelled))
            }
        }
    }

    fun openCameraCapture() {
        try {
            val dir = File(context.cacheDir, "contact_card_images").apply { mkdirs() }
            val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (_: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.camera_unavailable))
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCameraCapture()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.camera_permission_for_photo)
                )
            }
        }
    }

    fun onTakePhotoClick() {
        if (hasPermissions(context, Manifest.permission.CAMERA)) {
            openCameraCapture()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val user = userRepository.getCurrentUser()
            if (user != null) {
                ownerUserId = user.id
                val existing = cardRepository.getByOwnerUserId(user.id)
                if (existing != null) {
                    isNewCard = false
                    cardId = existing.cardId
                    profileImageUri = existing.profileImageUri
                    name = existing.name
                    jobTitle = existing.jobTitle.orEmpty()
                    company = existing.company.orEmpty()
                    description = existing.description.orEmpty()
                    website = existing.website.orEmpty()
                    email = existing.email.orEmpty()
                    phone = existing.phone.orEmpty()
                    alternateEmail = existing.alternateEmail.orEmpty()
                    linkedin = existing.linkedin.orEmpty()
                    twitter = existing.twitter.orEmpty()
                    customLink = existing.customLink.orEmpty()
                    calendly = existing.calendly.orEmpty()
                } else {
                    name = getUserNameOrAnonymous(user)
                    email = user.email.orEmpty()
                }
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.contact_card_editor_title),
                onBackClick = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ScreenScaffoldLight
    ) { padding ->
        if (isLoading) {
            Spacer(modifier = Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StandardText(
                    text = stringResource(R.string.contact_card_section_profile),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!profileImageUri.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUri),
                            contentDescription = stringResource(R.string.contact_card_photo),
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    OutlinedButton(
                        text = stringResource(R.string.contact_card_choose_photo),
                        onClick = { galleryLauncher.launch("image/*") }
                    )
                    OutlinedButton(
                        text = stringResource(R.string.contact_card_take_photo),
                        onClick = { onTakePhotoClick() }
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_name)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { StandardText(text = it) } }
                )
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_job_title)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_company)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_description)) },
                    minLines = 3
                )
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_website)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                StandardText(
                    text = stringResource(R.string.contact_card_section_contact),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_email)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError != null,
                    supportingText = emailError?.let { { StandardText(text = it) } }
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_phone)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = alternateEmail,
                    onValueChange = { alternateEmail = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_alternate_email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                StandardText(
                    text = stringResource(R.string.contact_card_section_links),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                OutlinedTextField(
                    value = linkedin,
                    onValueChange = { linkedin = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_linkedin)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = twitter,
                    onValueChange = { twitter = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_twitter)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = customLink,
                    onValueChange = { customLink = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_custom_link)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = calendly,
                    onValueChange = { calendly = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { StandardText(text = stringResource(R.string.contact_card_calendly)) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                RoundedButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.contact_card_save),
                    onClick = {
                        val trimmedName = name.trim()
                        val trimmedEmail = email.trim()
                        var valid = true
                        if (trimmedName.isEmpty()) {
                            nameError = context.getString(R.string.contact_card_name_required)
                            valid = false
                        }
                        if (trimmedEmail.isEmpty()) {
                            emailError = context.getString(R.string.contact_card_email_required)
                            valid = false
                        } else if (!isEmailValid(trimmedEmail)) {
                            emailError = context.getString(R.string.contact_card_email_invalid)
                            valid = false
                        }
                        if (!valid) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.contact_card_fix_invalid)
                                )
                            }
                            return@RoundedButton
                        }
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val card = ContactCardEntity(
                                    cardId = cardId,
                                    ownerUserId = ownerUserId,
                                    profileImageUri = profileImageUri,
                                    name = trimmedName,
                                    jobTitle = ContactCardQrCodec.blankToNull(jobTitle),
                                    company = ContactCardQrCodec.blankToNull(company),
                                    description = ContactCardQrCodec.blankToNull(description),
                                    email = trimmedEmail,
                                    phone = ContactCardQrCodec.blankToNull(phone),
                                    alternateEmail = ContactCardQrCodec.blankToNull(alternateEmail),
                                    website = ContactCardQrCodec.blankToNull(website),
                                    linkedin = ContactCardQrCodec.blankToNull(linkedin),
                                    twitter = ContactCardQrCodec.blankToNull(twitter),
                                    customLink = ContactCardQrCodec.blankToNull(customLink),
                                    calendly = ContactCardQrCodec.blankToNull(calendly),
                                    updatedAt = System.currentTimeMillis()
                                )
                                cardRepository.upsert(card)
                                if (isNewCard) {
                                    timelineRepository.insertTimelineEvent(
                                        TimelineEntity(
                                            type = TimelineType.CONTACT_CARD_CREATED.typeValue,
                                            description = context.getString(
                                                R.string.timeline_contact_card_created
                                            ),
                                            relatedId = null,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.contact_card_saved)
                            )
                            navController.popBackStack()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
