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

/**
 * Partner organization in the Tatum Tech directory.
 * Optional fields are omitted from JSON when unavailable — do not invent values.
 */
data class Partner(
    val id: String,
    val name: String,
    val category: String,
    val logo: String? = null,
    val description: String? = null,
    val featured: Boolean = false,
    val contacts: List<PartnerContact>? = null,
    val websiteUrl: String? = null,
    val additionalLinks: List<PartnerLink>? = null,
    val donationUrl: String? = null,
    val socialLinks: PartnerSocialLinks? = null,
    val productName: String? = null,
    val productUrl: String? = null,
    val downloadUrl: String? = null
) {
    fun contactList(): List<PartnerContact> = contacts.orEmpty()
    fun linkList(): List<PartnerLink> = additionalLinks.orEmpty()
    fun emails(): List<PartnerContact> = contactList().filter { !it.email.isNullOrBlank() }
}

data class PartnerContact(
    val name: String,
    val title: String? = null,
    val email: String? = null,
    val phone: String? = null
)

data class PartnerLink(
    val label: String,
    val url: String
)

data class PartnerSocialLinks(
    val x: String? = null,
    val linkedin: String? = null,
    val tiktok: String? = null,
    val instagram: String? = null,
    val meta: String? = null,
    val discord: String? = null,
    val youtube: String? = null
)
