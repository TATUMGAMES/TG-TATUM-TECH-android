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
package com.tatumgames.tatumtech.android.ui.components.screens.networking

import com.tatumgames.tatumtech.android.ui.components.screens.networking.models.ContactCardQrPayload

/**
 * Holds the most recently scanned contact-card payload until the preview screen consumes it.
 * Avoids oversized Navigation arguments for self-contained QR JSON.
 */
object ContactCardScanSession {
    @Volatile
    var pendingPayload: ContactCardQrPayload? = null
        private set

    fun setPending(payload: ContactCardQrPayload) {
        pendingPayload = payload
    }

    fun consume(): ContactCardQrPayload? {
        val value = pendingPayload
        pendingPayload = null
        return value
    }

    fun clear() {
        pendingPayload = null
    }
}
