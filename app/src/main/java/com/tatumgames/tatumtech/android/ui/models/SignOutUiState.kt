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
package com.tatumgames.tatumtech.android.ui.models

import com.tatumgames.tatumtech.framework.android.http.response.ApiError

/**
 * Sign-out state of the Profile screen.
 *
 * @param isConfirmationVisible The confirmation dialog is shown.
 * @param isSigningOut The request is running; the dialog shows progress and cannot be dismissed.
 * @param error The last failure, shown through the standard API error dialog.
 * @param isSignedOut The session is cleared; the screen should open the auth flow.
 */
data class SignOutUiState(
    val isConfirmationVisible: Boolean = false,
    val isSigningOut: Boolean = false,
    val error: ApiError? = null,
    val isSignedOut: Boolean = false
)
