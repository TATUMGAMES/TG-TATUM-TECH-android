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
package com.tatumgames.tatumtech.android.ui.components.screens.networking.models

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tatumgames.tatumtech.android.database.entity.ConnectionEntity
import com.tatumgames.tatumtech.android.database.entity.ContactCardEntity
import com.tatumgames.tatumtech.android.database.entity.UserEntity

/**
 * Versioned, self-contained QR payload for offline Tatum Tech contact cards.
 * Intentionally omits local profile image URIs and authentication secrets.
 *
 * [anonymousId] is the stable cross-device identity. Local Room [userId] alone is not unique
 * across devices (each install uses id = 1).
 */
data class ContactCardQrPayload(
    val type: String = TYPE,
    val version: Int = CURRENT_VERSION,
    val cardId: String,
    val userId: Long,
    val anonymousId: String? = null,
    val name: String,
    val jobTitle: String? = null,
    val company: String? = null,
    val description: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val alternateEmail: String? = null,
    val website: String? = null,
    val linkedin: String? = null,
    val twitter: String? = null,
    val customLink: String? = null,
    val calendly: String? = null
) {
    companion object {
        const val TYPE = "tatum_tech_contact"
        const val CURRENT_VERSION = 1
    }
}

sealed class ContactCardQrParseResult {
    data class Success(val payload: ContactCardQrPayload) : ContactCardQrParseResult()
    data class UnsupportedVersion(val version: Int) : ContactCardQrParseResult()
    data object Invalid : ContactCardQrParseResult()
}

object ContactCardQrCodec {
    private val gson = Gson()

    fun fromCard(card: ContactCardEntity, anonymousId: String): ContactCardQrPayload =
        ContactCardQrPayload(
            cardId = card.cardId,
            userId = card.ownerUserId,
            anonymousId = anonymousId,
            name = card.name,
            jobTitle = card.jobTitle,
            company = card.company,
            description = card.description,
            email = card.email,
            phone = card.phone,
            alternateEmail = card.alternateEmail,
            website = card.website,
            linkedin = card.linkedin,
            twitter = card.twitter,
            customLink = card.customLink,
            calendly = card.calendly
        )

    fun encode(payload: ContactCardQrPayload): String = gson.toJson(payload)

    fun parse(raw: String): ContactCardQrParseResult {
        if (raw.isBlank()) return ContactCardQrParseResult.Invalid
        return try {
            val payload = gson.fromJson(raw, ContactCardQrPayload::class.java)
                ?: return ContactCardQrParseResult.Invalid
            when {
                payload.type != ContactCardQrPayload.TYPE -> ContactCardQrParseResult.Invalid
                payload.cardId.isBlank() || payload.name.isBlank() -> ContactCardQrParseResult.Invalid
                payload.version > ContactCardQrPayload.CURRENT_VERSION ->
                    ContactCardQrParseResult.UnsupportedVersion(payload.version)

                payload.version < 1 -> ContactCardQrParseResult.Invalid
                else -> ContactCardQrParseResult.Success(payload)
            }
        } catch (_: JsonSyntaxException) {
            ContactCardQrParseResult.Invalid
        } catch (_: Exception) {
            ContactCardQrParseResult.Invalid
        }
    }

    /**
     * Own-card detection must use a stable identity.
     * Prefer [ContactCardQrPayload.anonymousId]; fall back to matching [localCardId].
     * Do not treat local Room [UserEntity.id] as unique across devices.
     */
    fun isOwnCard(
        payload: ContactCardQrPayload,
        currentUser: UserEntity,
        localCardId: String?
    ): Boolean {
        val payloadAnon = payload.anonymousId?.trim().orEmpty()
        if (payloadAnon.isNotEmpty()) {
            return payloadAnon == currentUser.anonymousId
        }
        val ownCardId = localCardId?.trim().orEmpty()
        return ownCardId.isNotEmpty() && payload.cardId == ownCardId
    }

    fun toConnection(
        ownerUserId: Long,
        payload: ContactCardQrPayload
    ): ConnectionEntity = ConnectionEntity(
        ownerUserId = ownerUserId,
        connectedCardId = payload.cardId,
        connectedUserId = payload.userId,
        name = payload.name,
        jobTitle = blankToNull(payload.jobTitle),
        company = blankToNull(payload.company),
        description = blankToNull(payload.description),
        email = blankToNull(payload.email),
        phone = blankToNull(payload.phone),
        alternateEmail = blankToNull(payload.alternateEmail),
        website = blankToNull(payload.website),
        linkedin = blankToNull(payload.linkedin),
        twitter = blankToNull(payload.twitter),
        customLink = blankToNull(payload.customLink),
        calendly = blankToNull(payload.calendly)
    )

    /**
     * Builds an [ContactsContract.Intents.Insert] intent so the user can review/save
     * the contact in the system Contacts app (no WRITE_CONTACTS permission required).
     *
     * Core fields use Insert extras; Website and alternate email use [Insert.DATA]
     * ContentValues. Social/custom links that lack a clean Contacts field go into NOTES.
     */
    fun createInsertContactIntent(payload: ContactCardQrPayload): Intent {
        val notes = buildString {
            payload.description?.takeIf { it.isNotBlank() }?.let {
                append(it)
                append("\n\n")
            }
            listOfNotNull(
                payload.linkedin?.takeIf { it.isNotBlank() }?.let { "LinkedIn: $it" },
                payload.twitter?.takeIf { it.isNotBlank() }?.let { "Twitter/X: $it" },
                payload.customLink?.takeIf { it.isNotBlank() }?.let { "Link: $it" },
                payload.calendly?.takeIf { it.isNotBlank() }?.let { "Calendly: $it" }
            ).forEach {
                append(it)
                append('\n')
            }
        }.trim().ifBlank { null }

        val dataRows = ArrayList<android.content.ContentValues>()
        blankToNull(payload.website)?.let { url ->
            dataRows.add(
                android.content.ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Website.URL, url)
                    put(
                        ContactsContract.CommonDataKinds.Website.TYPE,
                        ContactsContract.CommonDataKinds.Website.TYPE_HOME
                    )
                }
            )
        }
        blankToNull(payload.alternateEmail)?.let { altEmail ->
            dataRows.add(
                android.content.ContentValues().apply {
                    put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
                    put(ContactsContract.CommonDataKinds.Email.ADDRESS, altEmail)
                    put(
                        ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.TYPE_OTHER
                    )
                }
            )
        }

        return Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, payload.name)
            blankToNull(payload.email)?.let {
                putExtra(ContactsContract.Intents.Insert.EMAIL, it)
                putExtra(
                    ContactsContract.Intents.Insert.EMAIL_TYPE,
                    ContactsContract.CommonDataKinds.Email.TYPE_WORK
                )
            }
            blankToNull(payload.phone)?.let {
                putExtra(ContactsContract.Intents.Insert.PHONE, it)
                putExtra(
                    ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
            }
            blankToNull(payload.company)?.let {
                putExtra(ContactsContract.Intents.Insert.COMPANY, it)
            }
            blankToNull(payload.jobTitle)?.let {
                putExtra(ContactsContract.Intents.Insert.JOB_TITLE, it)
            }
            if (dataRows.isNotEmpty()) {
                putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, dataRows)
            }
            notes?.let { putExtra(ContactsContract.Intents.Insert.NOTES, it) }
        }
    }

    fun launchInsertContact(context: Context, payload: ContactCardQrPayload): Boolean {
        return runCatching {
            context.startActivity(createInsertContactIntent(payload))
            true
        }.getOrDefault(false)
    }

    fun blankToNull(value: String?): String? =
        value?.trim()?.takeIf { it.isNotEmpty() }
}
