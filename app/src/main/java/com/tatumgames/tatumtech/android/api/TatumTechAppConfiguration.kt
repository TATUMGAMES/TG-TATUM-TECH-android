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
package com.tatumgames.tatumtech.android.api

import com.tatumgames.tatumtech.android.BuildConfig

/**
 * Builds the app's [TatumTechClientConfiguration] from build settings (`tatumTech.*` Gradle or
 * local.properties values, exposed through [BuildConfig]). Session tokens are applied later by
 * [com.tatumgames.tatumtech.android.api.session.TatumTechSessionManager].
 */
object TatumTechAppConfiguration {

    fun fromBuildConfig(): TatumTechClientConfiguration = create(
        environment = BuildConfig.TATUM_TECH_ENVIRONMENT,
        dataSource = BuildConfig.TATUM_TECH_DATA_SOURCE,
        apiKey = BuildConfig.TATUM_TECH_API_KEY,
        connectTimeoutMs = BuildConfig.TATUM_TECH_CONNECT_TIMEOUT_MS,
        debugMode = BuildConfig.DEBUG
    )

    /**
     * @param apiKey Blank for none.
     * @param connectTimeoutMs `0` or less for the framework default.
     */
    internal fun create(
        environment: String,
        dataSource: String,
        apiKey: String,
        connectTimeoutMs: Long,
        debugMode: Boolean
    ): TatumTechClientConfiguration = TatumTechClientConfiguration.Builder()
        .setEnvironment(TatumTechEnvironment.fromName(environment))
        .setDataSourceMode(TatumTechDataSourceMode.fromName(dataSource))
        .setApiKey(apiKey.takeIf { it.isNotBlank() })
        .setHttpClientTimeout(connectTimeoutMs.takeIf { it > 0 })
        .setDebugMode(debugMode)
        .build()
}
