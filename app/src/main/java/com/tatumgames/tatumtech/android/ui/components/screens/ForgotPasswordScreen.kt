package com.tatumgames.tatumtech.android.ui.components.screens // Adjust package to match your project structure

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // Ensure this imports all Material3 components
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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


import com.tatumgames.tatumtech.android.R // Ensure this R points to your project's R file

@OptIn(ExperimentalMaterial3Api::class) // Add this annotation if you're using ExperimentalMaterial3Api like OutlinedTextField
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    onSubmitClick: (String, String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) } // State for toggling password visibility

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp, // @dimen/activity_horizontal_margin
                top = 16.dp, // @dimen/activity_vertical_margin
                end = 16.dp, // @dimen/activity_horizontal_margin
                bottom = 16.dp // @dimen/activity_vertical_margin
            )
    ) {
        // Back Button (ImageView equivalent)
        Image(
            painter = painterResource(id = R.drawable.back_arrow), // Assuming ic_arrow_back based on previous context
            contentDescription = stringResource(id = R.string.back),
            modifier = Modifier
                .size(48.dp)
                .padding(12.dp)
                .clickable(onClick = onBackClick)
        )

        // Title (TextView equivalent)
        Text(
            text = stringResource(id = R.string.web), // Assuming R.string.WEB from previous context
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Description (TextView equivalent)
        Text(
            text = stringResource(id = R.string.change_password), // Assuming R.string.change_password
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input (EditText equivalent)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.password)) }, // Assuming R.string.password
            visualTransformation = PasswordVisualTransformation(), // Hides password characters

            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Input (EditText equivalent)
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(id = R.string.confirm_password)) }, // Assuming R.string.confirm_password
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), // Toggle visibility

            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        // Show Password Toggle (TextView equivalent)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-56).dp)
                .height(56.dp)
        ) {
            Text(
                text = stringResource(id = R.string.show), // Assuming R.string.show
                color = Color.Black,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .clickable { showPassword = !showPassword }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Submit Button (Button equivalent)
        Button(
            onClick = { onSubmitClick(password, confirmPassword) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = stringResource(id = R.string.submit), // Assuming R.string.submit
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