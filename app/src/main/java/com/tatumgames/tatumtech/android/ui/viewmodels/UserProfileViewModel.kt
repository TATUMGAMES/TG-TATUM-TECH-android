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
package com.tatumgames.tatumtech.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tatumgames.tatumtech.android.ui.models.SignOutUiState
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sign-out flow of the Profile screen: confirmation, a single in-flight request, and the result.
 *
 * @param signOut Signs out; returns `null` on success or the API failure.
 * @param scope Overrides [viewModelScope], for tests.
 */
class UserProfileViewModel(
    private val signOut: suspend () -> ApiError?,
    private val scope: CoroutineScope? = null
) : ViewModel() {

    private val _signOutState = MutableStateFlow(SignOutUiState())
    val signOutState: StateFlow<SignOutUiState> = _signOutState.asStateFlow()

    fun requestSignOut() {
        _signOutState.update { if (it.isSigningOut) it else it.copy(isConfirmationVisible = true) }
    }

    fun cancelSignOut() {
        _signOutState.update { if (it.isSigningOut) it else it.copy(isConfirmationVisible = false) }
    }

    /** Ignored while a request is running or after signing out, so it is never sent twice. */
    fun confirmSignOut() {
        val current = _signOutState.value
        if (current.isSigningOut || current.isSignedOut) return
        _signOutState.value = current.copy(isConfirmationVisible = true, isSigningOut = true, error = null)

        (scope ?: viewModelScope).launch {
            val failure = signOut()
            _signOutState.update {
                if (failure == null) {
                    it.copy(isSignedOut = true)
                } else {
                    it.copy(isConfirmationVisible = false, isSigningOut = false, error = failure)
                }
            }
        }
    }

    fun dismissError() {
        _signOutState.update { it.copy(error = null) }
    }
}
