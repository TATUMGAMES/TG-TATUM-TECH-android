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
package com.tatumgames.tatumtech.android.data.content

import com.tatumgames.tatumtech.android.api.TatumTechApiClient
import com.tatumgames.tatumtech.android.api.TatumTechApiProvider
import com.tatumgames.tatumtech.android.api.models.TatumTechEvent
import com.tatumgames.tatumtech.android.api.models.TatumTechPartner
import com.tatumgames.tatumtech.android.api.models.TatumTechSpeaker
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.VirtualSpeaker
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.inSpeakingOrder
import com.tatumgames.tatumtech.android.ui.components.screens.partners.PartnerCategoryFilters
import com.tatumgames.tatumtech.android.ui.models.Partner
import com.tatumgames.tatumtech.android.ui.models.PartnerContact
import com.tatumgames.tatumtech.android.ui.models.PartnerLink
import com.tatumgames.tatumtech.android.ui.models.PartnerSocialLinks
import com.tatumgames.tatumtech.framework.android.http.response.ApiResponse

/**
 * Events, speakers, and partners for the UI, loaded through [TatumTechApiClient] (live API or
 * bundled JSON, depending on the configured data source) and mapped to the screens' models.
 */
class TatumTechContentRepository(
    private val clientProvider: () -> TatumTechApiClient = TatumTechApiProvider::getInstance
) {

    /**
     * Upcoming events.
     */
    suspend fun getUpcomingEvents(): ApiResponse<List<Event>> =
        clientProvider().getUpcomingEvents().map { events ->
            events.map { it.toEvent() }
        }

    /**
     * Speakers for [eventId] in speaking order.
     */
    suspend fun getEventSpeakers(eventId: String): ApiResponse<List<VirtualSpeaker>> =
        clientProvider().getEventSpeakers(eventId).map { speakers ->
            speakers.map { it.toVirtualSpeaker() }.inSpeakingOrder()
        }

    /**
     * Partners associated with the events.
     */
    suspend fun getPartners(): ApiResponse<List<Partner>> =
        clientProvider().getPartners().map { partners ->
            partners.map { it.toPartner() }
        }
}

internal fun TatumTechEvent.toEvent() = Event(
    id = id,
    name = name.orEmpty(),
    host = host.orEmpty(),
    date = date.orEmpty(),
    durationHours = durationHours ?: 0,
    location = location.orEmpty(),
    featuredImage = featuredImage.orEmpty(),
    lumaUrl = lumaUrl,
    virtualSpeakers = virtualSpeakers.map { it.toVirtualSpeaker() }
)

internal fun TatumTechSpeaker.toVirtualSpeaker() = VirtualSpeaker(
    id = id,
    name = name.orEmpty(),
    companyName = companyName,
    profileImage = profileImage,
    description = description,
    speakingTopic = speakingTopic.orEmpty(),
    speakingSchedule = speakingSchedule,
    startTime = startTime,
    endTime = endTime,
    timeZone = timeZone,
    meetUrl = meetUrl,
    sortOrder = sortOrder
)

/**
 * The API's category values ("Game Studios") become the labels the UI shows and filters on.
 */
internal fun TatumTechPartner.toPartner() = Partner(
    id = id,
    name = name.orEmpty(),
    category = PartnerCategoryFilters.categoryForApiValue(category),
    logo = logo,
    description = description,
    featured = featured,
    contacts = contacts.map { PartnerContact(it.name.orEmpty(), it.title, it.email, it.phone) }
        .takeIf { it.isNotEmpty() },
    websiteUrl = websiteUrl,
    additionalLinks = additionalLinks.map { PartnerLink(it.label.orEmpty(), it.url.orEmpty()) }
        .takeIf { it.isNotEmpty() },
    donationUrl = donationUrl,
    socialLinks = socialLinks?.let {
        PartnerSocialLinks(it.x, it.linkedin, it.tiktok, it.instagram, it.meta, it.discord, it.youtube)
    },
    productName = productName,
    productUrl = productUrl,
    downloadUrl = downloadUrl
)
