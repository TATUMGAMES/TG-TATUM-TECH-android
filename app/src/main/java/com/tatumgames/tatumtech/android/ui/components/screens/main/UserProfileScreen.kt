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
package com.tatumgames.tatumtech.android.ui.components.screens.main

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.activity.AuthActivity
import com.tatumgames.tatumtech.android.analytics.AnalyticsService
import com.tatumgames.tatumtech.android.analytics.ProfileFields
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.repository.UserDatabaseRepository
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.OutlinedButton
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.theme.DestructiveRed
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.ui.theme.White
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UserProfileScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val userRepository = remember { UserDatabaseRepository(db.userDao()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // Load user data
    LaunchedEffect(Unit) {
        val currentUser = userRepository.getCurrentUser()
        if (currentUser != null) {
            username = currentUser.name
            firstName = currentUser.firstName ?: ""
            lastName = currentUser.lastName ?: ""
            email = currentUser.email ?: ""
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.profile),
                onBackClick = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = White
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    // Show loading state
                    StandardText(
                        text = "Loading profile...",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    // Username (non-editable)
                    OutlinedTextField(
                        value = username,
                        onValueChange = {},
                        label = { StandardText(text = "Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Username",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        readOnly = true
                    )

                    // First Name
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { StandardText(text = "First Name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "First Name",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    // Last Name
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { StandardText(text = "Last Name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Last Name",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )


                    // Email (editable)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { StandardText(text = "Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Button
                    RoundedButton(
                        text = "Save",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        onClick = {
                            // Update user in database
                            CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                val currentUser = userRepository.getCurrentUser()
                                if (currentUser != null) {
                                    val newFirst = firstName.ifBlank { null }
                                    val newLast = lastName.ifBlank { null }
                                    val newEmail = email.ifBlank { null }
                                    val changedFields = buildList {
                                        if (currentUser.firstName != newFirst) {
                                            add(ProfileFields.FIRST_NAME)
                                        }
                                        if (currentUser.lastName != newLast) {
                                            add(ProfileFields.LAST_NAME)
                                        }
                                        if (currentUser.email != newEmail) {
                                            add(ProfileFields.EMAIL)
                                        }
                                    }
                                    val updatedUser = currentUser.copy(
                                        firstName = newFirst,
                                        lastName = newLast,
                                        email = newEmail
                                        // Keep the original username unchanged
                                    )
                                    userRepository.updateUser(updatedUser)
                                    changedFields.forEach { field ->
                                        AnalyticsService.updateProfile(field)
                                    }

                                    // Show success message on main thread
                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        snackbarHostState.showSnackbar("Profile updated successfully!")
                                    }
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                DeleteAccountButton(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 30.dp),
                    onClick = { showDeleteDialog = true }
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            isDeleting = isDeleting,
            onConfirm = {
                isDeleting = true
                scope.launch {
                    AccountDeletionManager.deleteAccount(context)
                    val intent = Intent(context, AuthActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                }
            },
            onDismiss = { if (!isDeleting) showDeleteDialog = false }
        )
    }
}

@Composable
private fun DeleteAccountButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = DestructiveRed,
            modifier = Modifier.size(24.dp)
        )
        StandardText(
            text = stringResource(R.string.delete_account),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = DestructiveRed
        )
    }
}

/**
 * "No" is the primary (purple) action. While [isDeleting] the dialog cannot be dismissed, so
 * deletion is never interrupted halfway.
 */
@Composable
private fun DeleteAccountDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isDeleting,
            dismissOnClickOutside = !isDeleting
        ),
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = stringResource(R.string.content_description_warning),
                tint = DestructiveRed
            )
        },
        title = {
            StandardText(
                text = stringResource(R.string.delete_account),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            StandardText(
                text = stringResource(
                    if (isDeleting) R.string.deleting_account else R.string.delete_account_dialog_message
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            if (isDeleting) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple500)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        text = stringResource(R.string.yes),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        borderColor = DestructiveRed,
                        textColor = DestructiveRed,
                        onClick = onConfirm
                    )
                    RoundedButton(
                        text = stringResource(R.string.no),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        onClick = onDismiss
                    )
                }
            }
        },
        containerColor = White
    )
}
