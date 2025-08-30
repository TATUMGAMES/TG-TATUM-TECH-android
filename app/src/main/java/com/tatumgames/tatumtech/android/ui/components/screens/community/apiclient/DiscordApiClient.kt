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
package com.tatumgames.tatumtech.android.ui.components.screens.community.apiclient

import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.framework.android.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class DiscordServerInfo(
    val memberCount: Int? = null,
    val onlineCount: Int? = null,
    val serverName: String? = null,
    val serverIcon: String? = null,
    val serverBanner: String? = null,
    val serverDescription: String? = null,
    val vanityUrl: String? = null,
    val boostLevel: Int? = null,
    val boostCount: Int? = null,
    val guildId: String? = null
)

sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String, val exception: Exception? = null) : ApiResult<T>()
}

class DiscordApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun fetchDiscordServerInfo(inviteCode: String): ApiResult<DiscordServerInfo> = withContext(Dispatchers.IO) {
        try {
            val url = "https://discord.com/api/v9/invites/$inviteCode?with_counts=true&with_expiration=true"
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "TatumTech-Android/1.0")
                .build()

            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext ApiResult.Error("HTTP ${response.code}: ${response.message}")
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrEmpty()) {
                return@withContext ApiResult.Error("Empty response from server")
            }

            val json = JSONObject(responseBody)
            val guild = json.optJSONObject("guild")
            
            val serverInfo = DiscordServerInfo(
                memberCount = json.optInt("approximate_member_count").takeIf { it > 0 },
                onlineCount = json.optInt("approximate_presence_count").takeIf { it > 0 },
                serverName = guild?.optString("name"),
                serverIcon = guild?.optString("icon"),
                serverBanner = guild?.optString("banner"),
                serverDescription = guild?.optString("description"),
                vanityUrl = guild?.optString("vanity_url_code"),
                boostLevel = guild?.optInt("premium_tier")?.takeIf { it > 0 },
                boostCount = guild?.optInt("premium_subscription_count")?.takeIf { it > 0 },
                guildId = guild?.optString("id")
            )

            ApiResult.Success(serverInfo)
        } catch (e: IOException) {
            Logger.e(TAG, "Network error: ${e.message}")
            ApiResult.Error("Network error: ${e.message}", e)
        } catch (e: Exception) {
            Logger.e(TAG, "API error: ${e.message}")
            ApiResult.Error("API error: ${e.message}", e)
        }
    }
}
