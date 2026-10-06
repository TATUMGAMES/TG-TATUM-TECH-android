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
package com.tatumgames.tatumtech.android.api.session

import com.tatumgames.tatumtech.android.api.models.TatumTechUser
import java.io.IOException

enum class TatumTechAuthMethod { EMAIL, GOOGLE }

/**
 * Signed-in Tatum Tech session.
 *
 * @param expiresAtMillis Access token expiry (epoch ms), or `null` if the server did not say.
 */
data class TatumTechSession(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtMillis: Long?,
    val authMethod: TatumTechAuthMethod,
    val user: TatumTechUser? = null
) {
    override fun toString(): String =
        "TatumTechSession(authMethod=$authMethod, expiresAtMillis=$expiresAtMillis, userId=${user?.id})"
}

/**
 * Persists the session across launches. Implementations must keep tokens out of plain text.
 */
interface TatumTechSessionStore {
    /** The stored session, or `null` if none or it can no longer be read. */
    fun load(): TatumTechSession?

    @Throws(IOException::class)
    fun save(session: TatumTechSession)

    fun clear()

    /** Stable random id for this installation, sent as `deviceId`; created on first use. */
    fun deviceId(): String
}
