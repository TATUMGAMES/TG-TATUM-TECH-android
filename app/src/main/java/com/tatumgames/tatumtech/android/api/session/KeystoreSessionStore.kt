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

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.gson.Gson
import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.framework.android.logger.Logger
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores the session in private SharedPreferences, encrypted with an AES-256-GCM key that never
 * leaves the Android Keystore. Keystore keys are not backed up or migrated, so a session restored
 * onto another device cannot be decrypted; it is then discarded and the user signs in again.
 */
class KeystoreSessionStore(context: Context) : TatumTechSessionStore {

    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    override fun load(): TatumTechSession? {
        val encoded = preferences.getString(KEY_SESSION, null) ?: return null
        return try {
            val session = gson.fromJson(decrypt(encoded), TatumTechSession::class.java)
            // Gson bypasses Kotlin null checks; reject records missing required fields.
            @Suppress("SENSELESS_COMPARISON")
            session?.takeIf { it.accessToken != null && it.authMethod != null }
                ?: run { clear(); null }
        } catch (e: Exception) {
            Logger.e(TAG, SESSION_UNREADABLE, e)
            clear()
            null
        }
    }

    override fun save(session: TatumTechSession) {
        val encrypted = try {
            encrypt(gson.toJson(session))
        } catch (e: GeneralSecurityException) {
            throw IOException(SESSION_UNWRITABLE, e)
        }
        if (!preferences.edit().putString(KEY_SESSION, encrypted).commit()) {
            throw IOException(SESSION_UNWRITABLE)
        }
    }

    override fun clear() {
        preferences.edit().remove(KEY_SESSION).commit()
    }

    @Synchronized
    override fun deviceId(): String = preferences.getString(KEY_DEVICE_ID, null)
        ?: UUID.randomUUID().toString().also { preferences.edit().putString(KEY_DEVICE_ID, it).commit() }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val payload = cipher.iv + cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = payload.copyOfRange(0, IV_LENGTH_BYTES)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
        }
        return String(cipher.doFinal(payload, IV_LENGTH_BYTES, payload.size - IV_LENGTH_BYTES), Charsets.UTF_8)
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    private companion object {
        const val PREFERENCES_NAME = "tatum_tech_session"
        const val KEY_SESSION = "session"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_ALIAS = "tatum_tech_session_key"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH_BYTES = 12
        const val TAG_LENGTH_BITS = 128
        const val SESSION_UNREADABLE = "Stored Tatum Tech session could not be read; discarding it"
        const val SESSION_UNWRITABLE = "Tatum Tech session could not be saved"
    }
}
