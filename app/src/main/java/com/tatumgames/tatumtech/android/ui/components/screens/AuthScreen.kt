package com.tatumgames.tatumtech.android.ui.components.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var buttonWidth = 300.dp
        var buttonHeight = 60.dp
        Text(
            text = stringResource(id = R.string.lets_begin_your_tatum_tech_experience),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up button
        RoundedButton(
            modifier = Modifier
                .width(buttonWidth)
                .height(buttonHeight),
            text = stringResource(id = R.string.sign_up),
            onClick = onSignUpClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sign In button
        RoundedButton(
            modifier = Modifier
                .width(buttonWidth)
                .height(buttonHeight),
            text = stringResource(id = R.string.sign_in),
            onClick = onSignInClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // StandardText composable for "OR"
        StandardText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.OR),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Center,
            color = colorResource(R.color.black)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Google SSO button (Image clickable)
        Image(
            painter = painterResource(id = R.drawable.android_light_sq_signin),
            contentDescription = "Google Sign In",
            modifier = Modifier
                .width(buttonWidth)
                .height(buttonHeight)
                .clickable { onGoogleSignInClick() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        email?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Signed in as $it", color = Color.Black)
        }

        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error: $it", color = Color.Red)
        }
    }
}
