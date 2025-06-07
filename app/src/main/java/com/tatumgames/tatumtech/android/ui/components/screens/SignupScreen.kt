package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatumgames.tatumtech.android.R // Ensure this R points to your project's R file

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onSignUpClick: (String, String, String) -> Unit, // Add email, password, confirmPassword as parameters
    onSignInLinkClick: () -> Unit // For "Already have an account?" link
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp) // Directly using 24dp as in your XML
    ) {
        // Back Button (ImageView equivalent)
        Image(
            painter = painterResource(id = R.drawable.back_arrow), // Replace with your actual drawable
            contentDescription = stringResource(id = R.string.back), // Replace with your actual string
            modifier = Modifier
                .size(48.dp)
                .padding(12.dp)
                .clickable(onClick = onBackClick) // Add clickable behavior
        )

        // Title (TextView equivalent)
        Text(
            text = stringResource(id = R.string.web), // Replace with your actual string
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally) // Centers the content within its width
                .padding(top = 16.dp) // Mimics android:layout_marginTop="16dp" from tv_title to top of parent
        )

        // Spacer to create vertical gap after title
        Spacer(modifier = Modifier.height(48.dp)) // android:layout_marginTop="48dp" for tv_description from tv_title

        // Description (TextView equivalent)
        Text(
            text = stringResource(id = R.string.create_your_account), // Replace with your actual string
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        // Spacer to create vertical gap after description
        Spacer(modifier = Modifier.height(24.dp)) // android:layout_marginTop="24dp" for et_email from tv_description

        // Email Input (EditText equivalent)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.email)) }, // Replace with your actual string
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        // Spacer to create vertical gap after email input
        Spacer(modifier = Modifier.height(16.dp)) // android:layout_marginTop="16dp" for et_password from et_email

        // Password Input (EditText equivalent)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.password)) }, // Replace with your actual string
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        // Spacer to create vertical gap after password input
        Spacer(modifier = Modifier.height(16.dp)) // android:layout_marginTop="16dp" for et_confirm_Password from et_password

        // Confirm Password Input (EditText equivalent)
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(id = R.string.confirm_password)) }, // Replace with your actual string
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        // Show Password Toggle (TextView equivalent)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-56).dp) // Roughly align with the top of the et_confirm_Password field
                .height(56.dp)
        ) {
            Text(
                text = stringResource(id = R.string.show), // Replace with your actual string
                color = Color.Black,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .clickable { showPassword = !showPassword }
            )
        }
    }
}

