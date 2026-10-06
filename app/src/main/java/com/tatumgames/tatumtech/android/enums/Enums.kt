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
package com.tatumgames.tatumtech.android.enums

import androidx.annotation.StringRes
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.constants.Constants.URL_STRIPE_10000
import com.tatumgames.tatumtech.android.constants.Constants.URL_STRIPE_2500
import com.tatumgames.tatumtech.android.constants.Constants.URL_STRIPE_5000
import com.tatumgames.tatumtech.android.constants.Constants.URL_STRIPE_CUSTOM
import com.tatumgames.tatumtech.android.enums.HomePagerCategory.Companion.toString

enum class TimelineFilter { TODAY, WEEK, MONTH }

/**
 * Home pager tab categories.
 *
 * [value] is the stable non-localized identifier used by data/navigation logic.
 * [displayNameResId] is the localized label shown in the UI.
 */
enum class HomePagerCategory(
    val value: String,
    @StringRes val displayNameResId: Int
) {
    EVENTS("Events", R.string.events),
    CODING("Coding", R.string.coding),
    COMMUNITY("Community", R.string.community),
    CAREER("Career", R.string.title_career),
    GAMES("Games", R.string.games);

    companion object {
        /**
         * Converts an existing String representation into the corresponding [HomePagerCategory].
         * Falls back to [EVENTS] when the value is unrecognized.
         *
         * Note: This is intentionally named [toString] and is not an override of [Any.toString].
         */
        fun toString(value: String): HomePagerCategory {
            return entries.firstOrNull { it.value == value } ?: EVENTS
        }
    }
}

enum class TimelineType(val typeValue: String) {
    EVENT_REGISTRATION("event_registration"),
    EVENT_UNREGISTRATION("event_unregistration"),
    CHALLENGE_COMPLETION("challenge_completion"),
    QR_SCAN("qr_scan"),

    /** @deprecated Prefer [CONNECTION_MADE]; retained so historical rows still decode. */
    FRIEND_ADD("friend_add"),

    /** @deprecated Prefer connection semantics; retained for historical rows. */
    FRIEND_REMOVE("friend_remove"),
    CONTACT_CARD_CREATED("contact_card_created"),
    CONTACT_CARD_SHARED("contact_card_shared"),
    CONNECTION_MADE("connection_made"),
    DONATION("donation");

    companion object {
        fun fromValue(value: String): TimelineType {
            return entries.firstOrNull { it.typeValue == value } ?: EVENT_REGISTRATION
        }
    }
}

enum class EventId(val id: Long) {
    FALL_2026(1),
    SPRING_2027(2),
    FALL_2027(3),
    SPRING_2028(4),
    FALL_2028(5),
    SPRING_2029(6),
    FALL_2029(7),
    SPRING_2030(8),
    FALL_2030(9);

    companion object {
        fun fromValue(value: Long): EventId {
            return entries.firstOrNull { it.id == value } ?: FALL_2026
        }
    }
}

enum class DonationTierType(
    @StringRes val nameRes: Int,
    val amount: String,
    val url: String
) {
    FOUNDING_SUPPORTER(
        nameRes = R.string.donation_founding_supporter,
        amount = "Custom",
        url = URL_STRIPE_CUSTOM
    ),
    SILVER_SPONSOR(
        nameRes = R.string.donation_silver_sponsor,
        amount = "$2,500",
        url = URL_STRIPE_2500
    ),
    GOLD_SPONSOR(
        nameRes = R.string.donation_gold_sponsor,
        amount = "$5,000",
        url = URL_STRIPE_5000
    ),
    PREMIER_SPONSOR(
        nameRes = R.string.donation_premier_sponsor,
        amount = "$10,000",
        url = URL_STRIPE_10000
    );
}
