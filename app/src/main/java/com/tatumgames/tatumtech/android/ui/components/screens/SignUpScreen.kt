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
package com.tatumgames.tatumtech.android.ui.components.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.utils.Utils
import com.tatumgames.tatumtech.android.ui.components.common.OutlinedButton
import com.tatumgames.tatumtech.android.ui.components.common.OutlinedInputField
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.common.TermsAndPrivacyText
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.activity.MainActivity
import com.tatumgames.tatumtech.android.ui.theme.TatumTechTheme

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    TatumTechTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SignUpScreen(
                navController = rememberNavController()
            )
        }
    }
}

@Composable
fun SignUpScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmTouched by remember { mutableStateOf(false) }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val isEmailValid = Utils.isEmailValid(email)
    val isPasswordValid = Utils.isPasswordValid(password)
    val doPasswordsMatch = confirmPassword == password && confirmPassword.isNotEmpty()
    val isFormValid = isEmailValid && isPasswordValid && doPasswordsMatch

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        val (header, form, signUpButton, footer) = createRefs()

        Header(
            modifier = Modifier.constrainAs(header) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            text = stringResource(R.string.sign_up),
            onBackClick = { navController.navigate(NavRoutes.AUTH_SCREEN) }
        )

        Column(
            modifier = Modifier.constrainAs(form) {
                top.linkTo(header.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        ) {
            StandardText(
                text = stringResource(id = R.string.create_your_account),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.black),
                modifier = Modifier.padding(top = 20.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email
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
                    .alpha(if (emailErrorVisible) {
                        1f
                    } else {
                        0f
                    })
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedInputField(
                value = password,
                onValueChange = { password = it },
                placeholder = stringResource(R.string.password),
                keyboardType = KeyboardType.Password,
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .onFocusChanged { if (!it.isFocused) passwordTouched = true },
                trailingIcon = {
                    StandardText(
                        text = if (showPassword) {
                            stringResource(R.string.hide)
                        } else {
                            stringResource(R.string.show)
                        },
                        color = colorResource(R.color.black),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .clickable { showPassword = !showPassword }
                            .padding(end = 8.dp)
                    )
                }
            )

            val passwordErrorVisible = passwordTouched && password.isNotBlank() && !isPasswordValid
            StandardText(
                text = stringResource(R.string.error_password_minimum_six_characters),
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .alpha(if (passwordErrorVisible) 1f else 0f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            OutlinedInputField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = stringResource(R.string.confirm_password),
                keyboardType = KeyboardType.Password,
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .onFocusChanged { if (!it.isFocused) confirmTouched = true },
                trailingIcon = {
                    StandardText(
                        text = if (showConfirmPassword) {
                            stringResource(R.string.hide)
                        } else {
                            stringResource(R.string.show)
                        },
                        color = colorResource(R.color.black),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .clickable { showConfirmPassword = !showConfirmPassword }
                            .padding(end = 8.dp)
                    )
                }
            )

            val confirmErrorVisible = confirmTouched && confirmPassword.isNotBlank() && !doPasswordsMatch
            StandardText(
                text = stringResource(R.string.error_passwords_do_not_match),
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .alpha(if (confirmErrorVisible) 1f else 0f)
            )
        }

        // Sign-Up Button
        val signUpModifier = Modifier
            .constrainAs(signUpButton) {
                top.linkTo(form.bottom, margin = 20.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }

        if (isFormValid) {
            RoundedButton(
                modifier = signUpModifier.height(60.dp),
                text = stringResource(R.string.sign_up),
                onClick = {
                    focusManager.clearFocus()

                    // TODO Call TG API to register user
                    context.startActivity(
                        Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
            )
        } else {
            OutlinedButton(
                modifier = signUpModifier.height(60.dp),
                text = stringResource(R.string.sign_up),
                onClick = { /* Disabled */ }
            )
        }

        // Terms & Privacy at Bottom
        TermsAndPrivacyText(
            modifier = Modifier.constrainAs(footer) {
                bottom.linkTo(parent.bottom, margin = 30.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            textColor = colorResource(R.color.black)
        )
    }
}
