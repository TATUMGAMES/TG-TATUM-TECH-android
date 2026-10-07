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

import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ResponseMetadata
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileViewModelTest {

    private var calls = 0
    private var pending = CompletableDeferred<ApiError?>()
    private val viewModel = UserProfileViewModel(
        signOut = {
            calls++
            pending.await()
        },
        scope = CoroutineScope(Dispatchers.Unconfined)
    )

    private val state get() = viewModel.signOutState.value

    private val offline = ApiError.Network(
        IOException("offline"),
        ResponseMetadata(HttpMethod.POST, "https://example.test/tatum-tech/signout", "tatum-tech/signout", 10)
    )

    @Test
    fun `tapping sign out only opens the confirmation`() {
        viewModel.requestSignOut()

        assertTrue(state.isConfirmationVisible)
        assertFalse(state.isSigningOut)
        assertEquals(0, calls)
    }

    @Test
    fun `cancel closes the dialog without calling the api`() {
        viewModel.requestSignOut()

        viewModel.cancelSignOut()

        assertFalse(state.isConfirmationVisible)
        assertFalse(state.isSignedOut)
        assertEquals(0, calls)
    }

    @Test
    fun `confirming shows progress and cannot be cancelled`() {
        viewModel.requestSignOut()
        viewModel.confirmSignOut()

        viewModel.cancelSignOut()

        assertTrue(state.isSigningOut)
        assertTrue(state.isConfirmationVisible)
        assertEquals(1, calls)
    }

    @Test
    fun `repeated confirms while signing out send one request`() {
        viewModel.requestSignOut()

        viewModel.confirmSignOut()
        viewModel.confirmSignOut()
        viewModel.confirmSignOut()

        assertEquals(1, calls)
    }

    @Test
    fun `success signs out`() {
        viewModel.requestSignOut()
        viewModel.confirmSignOut()

        pending.complete(null)

        assertTrue(state.isSignedOut)
        assertNull(state.error)
    }

    @Test
    fun `confirming after signing out sends nothing`() {
        viewModel.requestSignOut()
        viewModel.confirmSignOut()
        pending.complete(null)

        viewModel.confirmSignOut()

        assertEquals(1, calls)
    }

    @Test
    fun `failure stays signed in, closes the dialog and keeps the error for the standard dialog`() {
        viewModel.requestSignOut()
        viewModel.confirmSignOut()

        pending.complete(offline)

        assertFalse(state.isSignedOut)
        assertFalse(state.isSigningOut)
        assertFalse(state.isConfirmationVisible)
        assertSame(offline, state.error)
    }

    @Test
    fun `retry after a failure sends a new request and shows progress again`() {
        viewModel.requestSignOut()
        viewModel.confirmSignOut()
        pending.complete(offline)
        pending = CompletableDeferred()

        viewModel.confirmSignOut()

        assertEquals(2, calls)
        assertNull(state.error)
        assertTrue(state.isSigningOut)
        assertTrue(state.isConfirmationVisible)
    }

    @Test
    fun `dismissing the error clears it`() {
        viewModel.requestSignOut()
        viewModel.confirmSignOut()
        pending.complete(offline)

        viewModel.dismissError()

        assertNull(state.error)
        assertFalse(state.isSignedOut)
    }
}
