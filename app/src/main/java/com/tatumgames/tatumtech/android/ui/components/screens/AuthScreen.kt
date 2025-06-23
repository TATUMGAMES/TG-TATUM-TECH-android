package com.tatumgames.tatumtech.android.ui.components.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout // Import ConstraintLayout
import androidx.constraintlayout.compose.Dimension // Import Dimension for fill width/height
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    navController: NavController,
    onSignInSuccess: () -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val onGoogleSignInClick = {
        coroutineScope.launch {
            isLoading = true
            val credentialManager = CredentialManager.create(context)

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(true)
                        .setServerClientId(context.getString(R.string.default_web_client_id))
                        .build()
                )
                .build()

            try {
                val result = credentialManager.getCredential(context as Activity, request)
                val googleCredential = result.credential as? GoogleIdTokenCredential

                val idToken = googleCredential?.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            email = FirebaseAuth.getInstance().currentUser?.email
                            error = null
                            onSignInSuccess()
                        } else {
                            error = task.exception?.message
                        }
                    }
            } catch (e: Exception) {
                isLoading = false
                email = null
                error = e.message
            }
        }
    }


    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Create references for each Composable to position them within the ConstraintLayout
        val (titleRef, signUpButtonRef, signInButtonRef, orTextRef, googleButtonRef, progressIndicatorRef, emailTextRef, errorTextRef) = createRefs()

        val buttonWidth = 300.dp
        val buttonHeight = 60.dp

        // Title Text
        Text(
            text = stringResource(id = R.string.lets_begin_your_tatum_tech_experience),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .constrainAs(titleRef) {
                    top.linkTo(parent.top, margin = 64.dp) // Adjust margin from top as needed
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints // Allows it to fill width with padding
                }
        )

        // Sign Up button
        RoundedButton(
            modifier = Modifier
                .width(buttonWidth)
                .height(buttonHeight)
                .constrainAs(signUpButtonRef) {
                    top.linkTo(titleRef.bottom, margin = 32.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            text = stringResource(id = R.string.sign_up),
            onClick = onSignUpClick
        )

        // Sign In button
        RoundedButton(
            modifier = Modifier
                .width(buttonWidth)
                .height(buttonHeight)
                .constrainAs(signInButtonRef) {
                    top.linkTo(signUpButtonRef.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            text = stringResource(id = R.string.sign_in),
            onClick = onSignInClick
        )

        // StandardText composable for "OR"
        StandardText(
            modifier = Modifier
                .constrainAs(orTextRef) {
                    top.linkTo(signInButtonRef.bottom, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints // Allows it to fill width with padding
                },
            text = stringResource(id = R.string.OR),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Center,
            color = colorResource(R.color.black)
        )

        // Google SSO button (Image clickable)
        Image(
            painter = painterResource(id = R.drawable.android_light_sq_signin),
            contentDescription = "Google Sign In",
            modifier = Modifier
                .width(buttonWidth)
                .height(buttonHeight)
                .clickable { onGoogleSignInClick() }
                .constrainAs(googleButtonRef) {
                    top.linkTo(orTextRef.bottom, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.constrainAs(progressIndicatorRef) {
                    top.linkTo(googleButtonRef.bottom, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
        }

        email?.let {
            Text(
                "Signed in as $it",
                color = Color.Black,
                modifier = Modifier.constrainAs(emailTextRef) {
                    top.linkTo(progressIndicatorRef.bottom, margin = 16.dp) // Link to progress or google button if no progress
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
        }

        error?.let {
            Text(
                "Error: $it",
                color = Color.Red,
                modifier = Modifier.constrainAs(errorTextRef) {
                    top.linkTo(emailTextRef.bottom, margin = 16.dp) // Link to email text or previous element
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
        }
    }
}