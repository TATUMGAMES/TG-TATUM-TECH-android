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
package com.tatumgames.tatumtech.android.api.models

// Requests: null properties are omitted from the JSON body.

data class TatumTechEmailSignInRequest(
    val email: String,
    val password: String,
    val deviceId: String
)

data class TatumTechGoogleSignInRequest(
    val googleIdToken: String,
    val deviceId: String
)

data class TatumTechSignUpRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val deviceId: String
)

data class TatumTechRefreshTokenRequest(
    val refreshToken: String,
    val deviceId: String
)

data class TatumTechForgotPasswordRequest(
    val email: String
)

data class TatumTechResetPasswordRequest(
    val verifyToken: String,
    val password: String,
    val confirmPassword: String? = null,
    val email: String? = null
)

data class TatumTechUpdateUserProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null
)

// Responses

/**
 * `data` of sign-in, sign-up, and refresh-token responses.
 *
 * @param expiresIn Access token lifetime in seconds.
 */
data class TatumTechAuthSession(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null,
    val tokenType: String? = null,
    val user: TatumTechUser? = null
)

data class TatumTechUser(
    val id: String = "",
    val anonymousId: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val createdAt: String? = null
)
