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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.DemographicDataEntity
import com.tatumgames.tatumtech.android.database.repository.DemographicDataRepository
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardAlertDialog
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.theme.Grey400
import com.tatumgames.tatumtech.android.ui.theme.Grey500
import com.tatumgames.tatumtech.android.ui.theme.Purple200
import com.tatumgames.tatumtech.android.ui.theme.White
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemographicInfoScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val repository = remember { DemographicDataRepository(db.demographicDataDao()) }
    val snackbarHostState = remember { SnackbarHostState() }

    val ageRangeOptions = stringArrayResource(R.array.demographic_age_ranges).toList()
    val sexOptions = stringArrayResource(R.array.demographic_sex_options).toList()
    val salaryRangeOptions = stringArrayResource(R.array.demographic_salary_ranges).toList()
    val savedSnackbarMessage = stringResource(R.string.demographic_save_success)

    // Session-only dialog: show once per screen visit
    var showAgeDialog by remember { mutableStateOf(true) }

    // Form field state
    var ageRange by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var salaryRange by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }

    // Required consent checkboxes
    var ageConfirmed by remember { mutableStateOf(false) }
    var parentPermissionConfirmed by remember { mutableStateOf(false) }
    var dataUsageAcknowledged by remember { mutableStateOf(false) }

    val isSaveEnabled = ageConfirmed && parentPermissionConfirmed && dataUsageAcknowledged

    // Load any previously saved demographic data
    LaunchedEffect(Unit) {
        val saved = repository.getDemographicData()
        if (saved != null) {
            ageRange = saved.ageRange.orEmpty()
            sex = saved.sex.orEmpty()
            occupation = saved.occupation.orEmpty()
            salaryRange = saved.salaryRange.orEmpty()
            school = saved.school.orEmpty()
        }
    }

    // Age confirmation dialog shown once per session
    if (showAgeDialog) {
        StandardAlertDialog(
            title = stringResource(R.string.demographic_dialog_title),
            description = stringResource(R.string.demographic_dialog_description),
            confirmButtonText = stringResource(R.string.demographic_dialog_confirm),
            onConfirm = { showAgeDialog = false },
            dismissButtonText = stringResource(R.string.cancel),
            onDismiss = {
                showAgeDialog = false
                navController.popBackStack()
            },
            onDismissRequest = {}
        )
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.demographic_info),
                onBackClick = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StandardText(
                text = stringResource(R.string.demographic_section_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            StandardText(
                text = stringResource(R.string.demographic_section_description),
                style = MaterialTheme.typography.bodySmall,
                color = Grey500
            )

            Spacer(modifier = Modifier.height(4.dp))

            ConsentCheckboxRow(
                checked = ageConfirmed,
                onCheckedChange = { ageConfirmed = it },
                label = stringResource(R.string.demographic_consent_age)
            )

            ConsentCheckboxRow(
                checked = parentPermissionConfirmed,
                onCheckedChange = { parentPermissionConfirmed = it },
                label = stringResource(R.string.demographic_consent_parent)
            )

            HorizontalDivider(color = Grey500.copy(alpha = 0.3f))

            DemographicDropdown(
                label = stringResource(R.string.demographic_label_age_range),
                options = ageRangeOptions,
                selectedOption = ageRange,
                onOptionSelected = { ageRange = it }
            )

            DemographicDropdown(
                label = stringResource(R.string.demographic_label_sex),
                options = sexOptions,
                selectedOption = sex,
                onOptionSelected = { sex = it }
            )

            OutlinedTextField(
                value = occupation,
                onValueChange = {
                    occupation = it
                    // Clear salary range when occupation is removed
                    if (it.isBlank()) salaryRange = ""
                },
                label = { StandardText(text = stringResource(R.string.demographic_label_occupation)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // Salary Range: only visible when occupation is provided
            if (occupation.isNotBlank()) {
                DemographicDropdown(
                    label = stringResource(R.string.demographic_label_salary_range),
                    options = salaryRangeOptions,
                    selectedOption = salaryRange,
                    onOptionSelected = { salaryRange = it }
                )
            }

            OutlinedTextField(
                value = school,
                onValueChange = { school = it },
                label = { StandardText(text = stringResource(R.string.demographic_label_school)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = Grey500.copy(alpha = 0.3f))

            StandardText(
                text = stringResource(R.string.demographic_data_usage_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            ConsentCheckboxRow(
                checked = dataUsageAcknowledged,
                onCheckedChange = { dataUsageAcknowledged = it },
                label = stringResource(R.string.demographic_consent_data_usage)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button — disabled until all three consent checkboxes are checked
            RoundedButton(
                text = stringResource(R.string.demographic_save_button),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                backgroundColor = if (isSaveEnabled) Purple200 else Grey400,
                onClick = {
                    if (isSaveEnabled) {
                        CoroutineScope(Dispatchers.IO).launch {
                            repository.insertDemographicData(
                                DemographicDataEntity(
                                    id = 1,
                                    ageRange = ageRange.ifBlank { null },
                                    sex = sex.ifBlank { null },
                                    occupation = occupation.ifBlank { null },
                                    salaryRange = if (occupation.isNotBlank()) {
                                        salaryRange.ifBlank { null }
                                    } else {
                                        null
                                    },
                                    school = school.ifBlank { null }
                                )
                            )
                            withContext(Dispatchers.Main) {
                                snackbarHostState.showSnackbar(savedSnackbarMessage)
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConsentCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        StandardText(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemographicDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { StandardText(text = label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { StandardText(text = option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
