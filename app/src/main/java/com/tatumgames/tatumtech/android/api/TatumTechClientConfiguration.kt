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

import com.tatumgames.tatumtech.framework.android.http.config.CommonClientConfiguration
import com.tatumgames.tatumtech.framework.android.http.config.ConfigurationBuilder

/**
 * Tatum Tech backend deployments.
 */
enum class TatumTechEnvironment(val baseUrl: String) {
    PRODUCTION("https://tg-api-new.uc.r.appspot.com"),
    STAGE("https://tg-api-new-stage.uc.r.appspot.com");

    companion object {
        /** Returns the environment hosted at [baseUrl], or `null` for any other URL. */
        fun fromBaseUrl(baseUrl: String?): TatumTechEnvironment? =
            entries.firstOrNull { it.baseUrl.equals(baseUrl?.trimEnd('/'), ignoreCase = true) }

        /** Parses an environment name (case-insensitive), or `null` for blank or unknown names. */
        fun fromName(name: String?): TatumTechEnvironment? =
            entries.firstOrNull { it.name.equals(name?.trim(), ignoreCase = true) }
    }
}

/**
 * Where [TatumTechApiClient] reads its data from. Independent of the build type and of
 * [TatumTechClientConfiguration.debugMode]: a debug build can use either mode.
 */
enum class TatumTechDataSourceMode {
    /** Live Tatum Tech API over HTTP. */
    NETWORK,

    /** Known-good JSON bundled in the app's assets, for comparing against the live API. */
    LOCAL_JSON;

    companion object {
        /** Parses a mode name (case-insensitive), falling back to [NETWORK] for unknown values. */
        fun fromName(name: String?): TatumTechDataSourceMode =
            entries.firstOrNull { it.name.equals(name?.trim(), ignoreCase = true) } ?: NETWORK
    }
}

/**
 * Configuration for [TatumTechApiClient]. The base URL always comes from here; pick a deployment
 * with [Builder.setEnvironment] or point at any other host with `setBaseUrl`.
 */
class TatumTechClientConfiguration private constructor(
    values: Values,
    val dataSourceMode: TatumTechDataSourceMode
) : CommonClientConfiguration(values) {

    /** Known deployment matching [baseUrl], or `null` for a custom host. */
    val environment: TatumTechEnvironment? = TatumTechEnvironment.fromBaseUrl(baseUrl)

    class Builder : ConfigurationBuilder<Builder, TatumTechClientConfiguration>() {

        private var dataSourceMode = TatumTechDataSourceMode.NETWORK

        fun setEnvironment(environment: TatumTechEnvironment): Builder =
            setBaseUrl(environment.baseUrl)

        fun setDataSourceMode(mode: TatumTechDataSourceMode): Builder {
            dataSourceMode = mode
            return this
        }

        /** Copies every setting, including [dataSourceMode]. */
        fun from(configuration: TatumTechClientConfiguration): Builder {
            super.from(configuration)
            return setDataSourceMode(configuration.dataSourceMode)
        }

        override fun self(): Builder = this

        override fun build(): TatumTechClientConfiguration =
            TatumTechClientConfiguration(values(), dataSourceMode)
    }
}
