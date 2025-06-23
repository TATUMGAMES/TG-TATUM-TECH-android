package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.tatumgames.tatumtech.android.R

@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    onSubmitClick: (String, String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
    ) {
        // Create references for each Composable
        val (
            backButtonRef,
            titleRef,
            descriptionRef,
            passwordInputRef,
            confirmPasswordInputRef,
            showPasswordToggleRef,
            submitButtonRef
        ) = createRefs()

        // Back Button (ImageView equivalent)
        Image(
            painter = painterResource(id = R.drawable.back_arrow),
            contentDescription = stringResource(id = R.string.back),
            modifier = Modifier
                .size(48.dp)
                .padding(12.dp)
                .clickable(onClick = onBackClick)
                .constrainAs(backButtonRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        )

        // Title (TextView equivalent)
        Text(
            text = stringResource(id = R.string.web),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .constrainAs(titleRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints // Allows it to fill width with padding
                }
        )

        // Description (TextView equivalent)
        Text(
            text = stringResource(id = R.string.change_password),
            fontSize = 16.sp,
            modifier = Modifier
                .constrainAs(descriptionRef) {
                    top.linkTo(titleRef.bottom, margin = 48.dp) // Adjusted margin from title
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
        )

        // Password Input (EditText equivalent)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .constrainAs(passwordInputRef) {
                    top.linkTo(descriptionRef.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        // Confirm Password Input (EditText equivalent)
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(id = R.string.confirm_password)) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .constrainAs(confirmPasswordInputRef) {
                    top.linkTo(passwordInputRef.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        // Show Password Toggle (TextView equivalent)
        Text(
            text = stringResource(id = R.string.show),
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier
                .constrainAs(showPasswordToggleRef) {
                    // Position relative to confirmPasswordInputRef for alignment
                    top.linkTo(confirmPasswordInputRef.top, margin = 8.dp)
                    end.linkTo(confirmPasswordInputRef.end, margin = 8.dp)
                }
                .clickable { showPassword = !showPassword }
        )

        // Submit Button (Button equivalent)
        Button(
            onClick = { onSubmitClick(password, confirmPassword) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .constrainAs(submitButtonRef) {
                    top.linkTo(confirmPasswordInputRef.bottom, margin = 32.dp) // Link to bottom of confirm password input
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Text(
                text = stringResource(id = R.string.submit),
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    ChangePasswordScreen(
        onBackClick = { /* Preview back click */ },
        onSubmitClick = { _, _ -> /* Preview submit click */ }
    )
}