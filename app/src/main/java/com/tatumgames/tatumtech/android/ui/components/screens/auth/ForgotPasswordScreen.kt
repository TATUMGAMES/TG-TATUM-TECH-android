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
package com.tatumgames.tatumtech.android.ui.components.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.OutlinedButton
import com.tatumgames.tatumtech.android.ui.components.common.OutlinedInputField
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.common.TermsAndPrivacyText
import com.tatumgames.tatumtech.android.ui.theme.TatumTechTheme
import com.tatumgames.tatumtech.android.utils.Utils

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    TatumTechTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ForgotPasswordScreen(
                navController = rememberNavController()
            )
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var emailTouched by remember { mutableStateOf(false) }

    val isEmailValid = Utils.isEmailValid(email)
    val isFormValid = isEmailValid

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.forgot_password),
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                StandardText(
                    text = stringResource(id = R.string.input_email_for_reset),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.black),
                    modifier = Modifier.padding(top = 20.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedInputField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = stringResource(R.string.email),
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .onFocusChanged { if (!it.isFocused) emailTouched = true }
                )

                val emailErrorVisible = emailTouched && email.isNotBlank() && !isEmailValid
                StandardText(
                    text = stringResource(R.string.error_input_valid_email),
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .alpha(if (emailErrorVisible) 1f else 0f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isFormValid) {
                    RoundedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        text = stringResource(R.string.reset_password),
                        onClick = {
                            focusManager.clearFocus()
                            // TODO: Send reset email logic here
                            Toast.makeText(
                                context,
                                R.string.reset_password_email_sent,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                } else {
                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        text = stringResource(R.string.reset_password),
                        onClick = { /* Disabled */ }
                    )
                }
            }

            // Terms & Privacy at Bottom
            TermsAndPrivacyText(
                textColor = colorResource(R.color.black)
            )
        }
    }
}
