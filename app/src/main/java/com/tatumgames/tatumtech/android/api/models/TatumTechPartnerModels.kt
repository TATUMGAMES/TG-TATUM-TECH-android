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

/**
 * Values accepted by the `category` query parameter of `partners`.
 */
enum class TatumTechPartnerCategory(val apiValue: String) {
    COMMUNITY("Community"),
    CORPORATE("Corporate"),
    EDUCATION("Education"),
    GAME_STUDIOS("Game Studios"),
    GOVERNMENT("Government"),
    TECHNOLOGY("Technology")
}

data class TatumTechPartner(
    val id: String = "",
    val name: String? = null,
    val category: String? = null,
    val logo: String? = null,
    val description: String? = null,
    val featured: Boolean = false,
    val contacts: List<TatumTechPartnerContact> = emptyList(),
    val websiteUrl: String? = null,
    val additionalLinks: List<TatumTechPartnerLink> = emptyList(),
    val donationUrl: String? = null,
    val socialLinks: TatumTechPartnerSocialLinks? = null,
    val productName: String? = null,
    val productUrl: String? = null,
    val downloadUrl: String? = null
)

data class TatumTechPartnerContact(
    val name: String? = null,
    val title: String? = null,
    val email: String? = null,
    val phone: String? = null
)

data class TatumTechPartnerLink(
    val label: String? = null,
    val url: String? = null
)

data class TatumTechPartnerSocialLinks(
    val x: String? = null,
    val linkedin: String? = null,
    val tiktok: String? = null,
    val instagram: String? = null,
    val meta: String? = null,
    val discord: String? = null,
    val youtube: String? = null
)

/** `data` of `partners`. */
data class TatumTechPartnersData(
    val partners: List<TatumTechPartner> = emptyList()
)

/** `data` of `partners/{partnerId}`. */
data class TatumTechPartnerData(
    val partner: TatumTechPartner? = null
)
