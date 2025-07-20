package com.tatumgames.tatumtech.android.ui.components.screens

import android.widget.Toast
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
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
fun ChangePasswordScreenPreview() {
    TatumTechTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ChangePasswordScreen(
                navController = rememberNavController()
            )
        }
    }
}

@Composable
fun ChangePasswordScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordTouched by remember { mutableStateOf(false) }
    var confirmTouched by remember { mutableStateOf(false) }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val isPasswordValid = Utils.isPasswordValid(password)
    val doPasswordsMatch = confirmPassword == password && confirmPassword.isNotEmpty()
    val isFormValid = isPasswordValid && doPasswordsMatch

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        val (header, form, submitButton, footer) = createRefs()

        Header(
            modifier = Modifier.constrainAs(header) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            text = stringResource(R.string.change_password),
            onBackClick = { navController.popBackStack() }
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
                text = stringResource(id = R.string.set_new_password),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.black),
                modifier = Modifier.padding(top = 20.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // New Password
            OutlinedInputField(
                value = password,
                onValueChange = { password = it },
                placeholder = stringResource(R.string.new_password),
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

            val confirmErrorVisible =
                confirmTouched && confirmPassword.isNotBlank() && !doPasswordsMatch
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

        val submitModifier = Modifier
            .constrainAs(submitButton) {
                top.linkTo(form.bottom, margin = 20.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }

        if (isFormValid) {
            RoundedButton(
                modifier = submitModifier.height(60.dp),
                text = stringResource(R.string.new_password),
                onClick = {
                    focusManager.clearFocus()

                    // TODO: Call API to update password
                    Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()

                    // Navigate to login screen or home
                }
            )
        } else {
            OutlinedButton(
                modifier = submitModifier.height(60.dp),
                text = stringResource(R.string.new_password),
                onClick = { /* Disabled */ }
            )
        }

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

