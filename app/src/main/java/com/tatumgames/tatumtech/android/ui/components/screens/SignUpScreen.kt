package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row // Keep Row for "Already have an account?" link
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.* // Ensure this imports all Material3 components
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
import androidx.constraintlayout.compose.ConstraintLayout // Import ConstraintLayout
import androidx.constraintlayout.compose.Dimension // Import Dimension for fill width/height

// Required for KeyboardOptions and KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.navigation.NavController // Import NavController
import androidx.navigation.compose.rememberNavController // For @Preview



import com.tatumgames.tatumtech.android.R // Ensure this R points to your project's R file

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController, // NavController added here
    onSignUpClick: (String, String, String) -> Unit, // Add email, password, confirmPassword as parameters
    onSignInLinkClick: () -> Unit // For "Already have an account?" link
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Use ConstraintLayout as the parent body
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
        // Create references for each Composable to position them
        val (
            backBtn,
            title,
            description,
            emailField,
            passwordField,
            confirmPasswordField,
            showPwdText, // This will be the Text, not a Box
            signUpBtn,
            signInLinkRow, // Reference for the Row containing "Already have an account?"
            forgotPasswordText // Reference for the new "Forgot Password?" text
        ) = createRefs()

        // Back Button (ImageView equivalent)
        Image(
            painter = painterResource(id = R.drawable.back_arrow),
            contentDescription = stringResource(id = R.string.back),
            modifier = Modifier
                .size(48.dp)
                .padding(12.dp) // Padding internal to the image
                .constrainAs(backBtn) {
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
                .constrainAs(title) {
                    top.linkTo(backBtn.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints // Fills width within constraints
                }
        )

        // Description (TextView equivalent)
        Text(
            text = stringResource(id = R.string.create_your_account), // Make sure you have this string resource
            fontSize = 16.sp,
            modifier = Modifier
                .constrainAs(description) {
                    top.linkTo(title.bottom, margin = 48.dp) // Margin from title
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
        )

        // Email Input (EditText equivalent)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.email)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth() // Implied by constraints
                .constrainAs(emailField) {
                    top.linkTo(description.bottom, margin = 24.dp) // Margin from description
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        // Password Input (EditText equivalent)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth() // Implied by constraints
                .constrainAs(passwordField) {
                    top.linkTo(emailField.bottom, margin = 16.dp) // Margin from email input
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth() // Implied by constraints
                .constrainAs(confirmPasswordField) {
                    top.linkTo(passwordField.bottom, margin = 16.dp) // Margin from password input
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        // Show Password Toggle (TextView equivalent - simplified from Box)
        Text(
            text = stringResource(id = R.string.show),
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier
                .constrainAs(showPwdText) {
                    // Position relative to confirmPasswordField
                    top.linkTo(confirmPasswordField.top, margin = 8.dp) // Align with top of confirm password field
                    end.linkTo(confirmPasswordField.end, margin = 20.dp) // Margin from end of confirm password field
                }
                .clickable { showPassword = !showPassword }
        )

        // Sign Up Button (Button equivalent)
        Button(
            onClick = { onSignUpClick(email, password, confirmPassword) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth() // Implied by constraints
                .constrainAs(signUpBtn) {
                    top.linkTo(confirmPasswordField.bottom, margin = 32.dp) // Margin from confirm password input
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Text(
                text = stringResource(id = R.string.sign_up), // Make sure you have this string resource
                color = Color.White
            )
        }

        // "Already have an account?" link (retains Row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(signInLinkRow) {
                    top.linkTo(signUpBtn.bottom, margin = 16.dp) // Margin from sign up button
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
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

        // Forgot Password Link
        Text(
            text = stringResource(id = R.string.forgotten_password_message), // Assuming you have this string resource
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary, // Using primary theme color for link
            modifier = Modifier
                .constrainAs(forgotPasswordText) {
                    top.linkTo(signInLinkRow.bottom, margin = 16.dp) // Below the "Already have account?" row
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.wrapContent // Wrap content width, but center horizontally
                }
                .clickable {
                    // Navigate to the ForgotPasswordScreen
                    // Ensure AppScreens.ForgotPassword.route is correctly defined
//                    navController.navigate(screens.ForgotPassword.route)
                }
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SignUpScreenPreview() {
//    SignUpScreen(
//        navController = rememberNavController(), // Provide a dummy NavController for preview
//
//        onSignUpClick = { _, _, _ -> /* Preview sign up click */ },
//        onSignInLinkClick = { /* Preview sign in link click */ }
//    )
//}