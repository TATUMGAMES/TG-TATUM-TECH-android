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

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.activity.MainActivity
import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.common.TermsAndPrivacyText
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.AuthScreenMessages.ACTIVITY_REQUIRED_GOOGLE_AUTHENTICATION
import com.tatumgames.tatumtech.android.ui.components.screens.AuthScreenMessages.GOOGLE_AUTHENTICATION_FAILED
import com.tatumgames.tatumtech.android.ui.components.screens.AuthScreenMessages.GOOGLE_AUTHENTICATION_SUCCESSFUL
import com.tatumgames.tatumtech.android.ui.theme.TatumTechTheme
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthClient
import com.tatumgames.tatumtech.framework.android.auth.GoogleAuthError
import com.tatumgames.tatumtech.framework.android.auth.configuration.GoogleAuthConfiguration
import com.tatumgames.tatumtech.framework.android.auth.interfaces.GoogleAuthCallback
import com.tatumgames.tatumtech.framework.android.auth.models.GoogleUser
import com.tatumgames.tatumtech.framework.android.logger.Logger

object AuthScreenMessages {
    internal const val GOOGLE_AUTHENTICATION_SUCCESSFUL = "Google authentication successful"
    internal const val GOOGLE_AUTHENTICATION_FAILED = "Google authentication failed"
    internal const val ACTIVITY_REQUIRED_GOOGLE_AUTHENTICATION =
        "Activity is required for Google authentication"
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    TatumTechTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AuthScreen(
                navController = rememberNavController()
            )
        }
    }
}

@Composable
fun AuthScreen(
    navController: NavController
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val activity = context as? Activity

    // Google Authentication Callback implementation
    val authCallback = remember {
        object : GoogleAuthCallback {
            override fun onGoogleAuthSuccess(user: GoogleUser) {
                Logger.d(TAG, "${GOOGLE_AUTHENTICATION_SUCCESSFUL}: ${user.email}")

                // Navigate to main activity
                context.startActivity(
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
            }

            override fun onGoogleAuthFailure(error: GoogleAuthError) {
                errorMessage = error.message
                Logger.e(TAG, "${GOOGLE_AUTHENTICATION_FAILED}: ${error.message}")
            }
        }
    }

    // Legacy launcher for fallback authentication
    val legacyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Only handle legacy result if we have a valid activity
        activity?.let { safeActivity ->
            // Create configuration for legacy result handling
            val configuration = GoogleAuthConfiguration.builder()
                .context(context)
                .activity(safeActivity)
                .webClientId(context.getString(R.string.default_web_client_id))
                .callback(authCallback)
                .legacyLauncher(null) // Not needed for result handling
                .build()

            // Handle legacy result through the framework
            GoogleAuthClient.handleLegacyResult(configuration, result.data)
        }
    }

    // Google Sign-In click handler
    val onGoogleSignInClick = {
        errorMessage = null // Clear previous errors

        // Only proceed if we have a valid activity
        activity?.let {
            // Create configuration for sign-in
            val configuration = GoogleAuthConfiguration.builder()
                .context(context)
                .activity(it)
                .webClientId(context.getString(R.string.default_web_client_id))
                .callback(authCallback)
                .legacyLauncher(legacyLauncher)
                .build()

            GoogleAuthClient.signIn(configuration)
        } ?: run {
            errorMessage = ACTIVITY_REQUIRED_GOOGLE_AUTHENTICATION
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        // Centered content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Description
            StandardText(
                text = stringResource(id = R.string.lets_begin_your_tatum_tech_experience),
                color = Color.Black,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            // Sign-In CTA
            RoundedButton(
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp),
                text = stringResource(id = R.string.sign_in),
                onClick = { navController.navigate(NavRoutes.SIGN_IN_SCREEN) }
            )

            // Sign-Up CTA
            RoundedButton(
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp),
                text = stringResource(id = R.string.sign_up),
                onClick = { navController.navigate(NavRoutes.SIGN_UP_SCREEN) }
            )

            // "OR" Text
            StandardText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.OR),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                textAlign = TextAlign.Center,
                color = colorResource(R.color.black)
            )

            // Google SSO CTA
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(60.dp)
                    .clickable { onGoogleSignInClick() },
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.android_light_sq_signin),
                    contentDescription = stringResource(R.string.content_description_google_sso),
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Terms and Privacy
        TermsAndPrivacyText(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            textColor = colorResource(id = R.color.black)
        )
    }
}
