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
package com.tatumgames.tatumtech.android.constants

object Constants {
    internal const val TAG = "TG_TatumTech"

    // Terms and privacy policy URLs
    const val URL_TERMS = "https://developer.tatumgames.com/terms"
    const val URL_PRIVACY_POLICY = "https://developer.tatumgames.com/privacy"
    // Donation & Community URLs
    // used to retrieve server information from Discord API
    const val DISCORD_API_URL = "https://discord.com/api/v9/invites/6FzqSUDRXQ?with_counts=true&with_expiration=true"
    // used to retrieve vanity server information from Discord API
    const val DISCORD_BASE_URL = "https://discord.gg"
    // used to retrieve banners and icons from Discord API.
    // Just append DISCORD_BANNER_PATH and DISCORD_ICON_PATH
    const val DISCORD_APP_BASE_URL = "https://cdn.discordapp.com"
    const val DISCORD_BANNER_PATH = "/banners/1066030704091447336/"
    const val DISCORD_ICON_PATH = "/icons/1066030704091447336/"
    const val URI_DISCORD = "discord://invite/6FzqSUDRXQ"
    const val URL_DISCORD_PAYROLE = "https://payrole.io/app/store?id=65e729cff968f40012f47835&nojoin"
    const val URL_DISCORD_INVITE = "https://discord.com/invite/6FzqSUDRXQ"
    const val URL_DISCORD_LOGIN = "https://discord.com/invite/6FzqSUDRXQ/login"
    const val URL_STRIPE_CUSTOM = "https://buy.stripe.com/7sI3cH8m6bmd5ck4gj"
    const val URL_STRIPE_2500 = "https://buy.stripe.com/28oaF9cCmdul7kscMO"
    const val URL_STRIPE_5000 = "https://buy.stripe.com/6oEbJd6dY0Hz8ow145"
    const val URL_STRIPE_10000 = "https://buy.stripe.com/3cs28D45QgGxgV2fYY"

    // Key/Values
    const val KEY_USER_ID = "user_id"

    // Errors
    const val ICON_OR_IMAGE_ERROR = "Either icon or image must be provided."
}
