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

// Required for KeyboardOptions and KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

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
            .padding(
                start = 16.dp, // Matches SignInScreen
                top = 16.dp,  // Matches SignInScreen
                end = 16.dp,  // Matches SignInScreen
                bottom = 16.dp // Matches SignInScreen
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
            text = stringResource(id = R.string.web),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .padding(top = 16.dp) // Matches SignInScreen
        )

        // Spacer to create vertical gap after title
        Spacer(modifier = Modifier.height(48.dp)) // Matches SignInScreen

        // Description (TextView equivalent)
        Text(
            text = stringResource(id = R.string.create_your_account), // Make sure you have this string resource
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        // Spacer to create vertical gap after description
        Spacer(modifier = Modifier.height(24.dp)) // Matches SignInScreen

        // Email Input (EditText equivalent)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.email)) },
            keyboardOptions = KeyboardOptions( // Using imported KeyboardOptions
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        // Spacer to create vertical gap after email input
        Spacer(modifier = Modifier.height(16.dp)) // Matches SignInScreen

        // Password Input (EditText equivalent)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions( // Using imported KeyboardOptions
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        // Spacer to create vertical gap after password input
        Spacer(modifier = Modifier.height(16.dp)) // Matches SignInScreen

        // Confirm Password Input (EditText equivalent)
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(id = R.string.confirm_password)) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions( // Using imported KeyboardOptions
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
                .offset(y = (-56).dp) // Roughly align with the top of the confirm_Password field
                .height(56.dp)
        ) {
            Text(
                text = stringResource(id = R.string.show),
                color = Color.Black,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 20.dp) // Matches SignInScreen
                    .clickable { showPassword = !showPassword }
            )
        }

        // Spacer to create vertical gap before button
        Spacer(modifier = Modifier.height(32.dp)) // Matches SignInScreen

        // Sign Up Button (Button equivalent)
        Button(
            onClick = { onSignUpClick(email, password, confirmPassword) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = stringResource(id = R.string.sign_up), // Make sure you have this string resource
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp)) // Spacing before the "Already have an account?" link

        // "Already have an account?" link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.already_have_an_account), // You might want a string resource for this
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(id = R.string.sign_in), // Reusing sign_in string
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary, // Using primary theme color for link
                modifier = Modifier.clickable(onClick = onSignInLinkClick)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        onBackClick = { /* Preview back click */ },
        onSignUpClick = { _, _, _ -> /* Preview sign up click */ },
        onSignInLinkClick = { /* Preview sign in link click */ }
    )
}