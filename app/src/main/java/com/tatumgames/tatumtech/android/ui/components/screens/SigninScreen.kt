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
fun SignInScreen(
    onBackClick: () -> Unit,
    onSignInClick: (String, String) -> Unit // Email and password as parameters
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) } // State for toggling password visibility

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                // Referencing dimensions from R.dimen in a real app,
                // using fixed dp values for this direct conversion example.
                // Original XML had padding for container, then margins for individual elements.
                // For Compose, it's often cleaner to apply overall padding to the parent Column.
                // The XML had paddingLeft/Right/Top/Bottom using @dimen, but then also specific
                // margins like layout_marginTop="16dp" for the back arrow and title.
                // Let's stick to the overall padding from the parent, and then add individual
                // spacers/padding to match the vertical gaps.
                start = 16.dp, // @dimen/activity_horizontal_margin
                top = 16.dp,  // @dimen/activity_vertical_margin
                end = 16.dp,  // @dimen/activity_horizontal_margin
                bottom = 16.dp // @dimen/activity_vertical_margin
            )
    ) {
        // Back Button (ImageView equivalent)
        // Original XML had layout_marginTop="16dp" and layout_marginStart="16dp" on the ImageView itself,
        // despite the parent having padding. This implies an *additional* offset.
        // We will include this in the Image's own modifier, or remove the parent padding if it's meant to be absolute.
        // Assuming the parent padding acts like overall screen padding, and these are additional.
        Image(
            painter = painterResource(id = R.drawable.back_arrow), // Replace with your actual drawable
            contentDescription = stringResource(id = R.string.back), // Replace with your actual string
            modifier = Modifier
                .size(48.dp)
                .padding(12.dp) // Original padding on ImageView
                .clickable(onClick = onBackClick)
            // .offset(x = 16.dp, y = 16.dp) // If you need to mimic the extra margins *relative to padding*
        )

        // Title (TextView equivalent)
        // Constraints: app:layout_constraintStart_toStartOf="parent", app:layout_constraintTop_toTopOf="parent", app:layout_constraintEnd_toEndOf="parent"
        // and android:layout_marginTop="16dp"
        Text(
            text = stringResource(id = R.string.web), // Replace with your actual string
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally) // Centers the content within its width
                .padding(top = 16.dp) // Mimics android:layout_marginTop="16dp" from title to top of parent
        )

        // Spacer to create vertical gap after title
        Spacer(modifier = Modifier.height(48.dp)) // android:layout_marginTop="48dp" for description from title

        // Description (TextView equivalent)
        // Constraints: app:layout_constraintTop_toBottomOf="@id/title", app:layout_constraintStart_toStartOf="parent", app:layout_constraintEnd_toEndOf="parent"
        // android:layout_width="0dp" implies fillMaxWidth
        Text(
            text = stringResource(id = R.string.Signin_your_account), // Replace with your actual string
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )

        // Spacer to create vertical gap after description
        Spacer(modifier = Modifier.height(24.dp)) // android:layout_marginTop="24dp" for email from description

        // Email Input (EditText equivalent)
        // Constraints: app:layout_constraintTop_toBottomOf="@id/description", app:layout_constraintStart_toStartOf="parent", app:layout_constraintEnd_toEndOf="parent"
        // android:layout_width="0dp" implies fillMaxWidth
        // android:layout_marginHorizontal="16dp" -- This is handled by the parent Column's padding.
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.email)) }, // Replace with your actual string
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Fixed height
        )

        // Spacer to create vertical gap after email input
        Spacer(modifier = Modifier.height(16.dp)) // android:layout_marginTop="16dp" for et_password from email

        // Password Input (EditText equivalent)
        // Constraints: app:layout_constraintTop_toBottomOf="@id/email", app:layout_constraintStart_toStartOf="parent", app:layout_constraintEnd_toEndOf="parent"
        // android:layout_width="0dp" implies fillMaxWidth
        // android:layout_marginHorizontal="16dp" -- This is handled by the parent Column's padding.
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.password)) }, // Replace with your actual string
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), // Toggle visibility
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Fixed height
        )

        // Show Password Toggle (TextView equivalent)
        // Constraints: app:layout_constraintTop_toTopOf="@id/et_password", app:layout_constraintEnd_toEndOf="parent"
        // android:layout_marginTop="8dp", android:layout_marginEnd="20dp"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-56).dp) // Roughly align with the top of the et_password field
                .height(56.dp)
        ) {
            Text(
                text = stringResource(id = R.string.show), // Replace with your actual string
                color = Color.Black,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 20.dp) // Mimics android:layout_marginTop="8dp" and android:layout_marginEnd="20dp"
                    .clickable { showPassword = !showPassword }
            )
        }

        // Spacer to create vertical gap before button
        Spacer(modifier = Modifier.height(32.dp)) // android:layout_marginTop="32dp" for btn_signup_button from et_password

        // Sign In Button (Button equivalent)
        // Constraints: app:layout_constraintTop_toBottomOf="@id/et_password", app:layout_constraintStart_toStartOf="parent", app:layout_constraintEnd_toEndOf="parent"
        // android:layout_width="0dp" implies fillMaxWidth
        // android:layout_marginHorizontal="16dp" -- Handled by parent Column's padding.
        Button(
            onClick = { onSignInClick(email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // backgroundTint="#4CAF50"
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp) // minHeight="48dp"
        ) {
            Text(
                text = stringResource(id = R.string.sign_in), // Replace with your actual string
                color = Color.White // textColor="#FFFFFF"
            )
        }
        // There is no "Already have an account?" link in this XML layout, so it's not included here.
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    SignInScreen(
        onBackClick = { /* Preview back click */ },
        onSignInClick = { _, _ -> /* Preview sign in click */ }
    )
}