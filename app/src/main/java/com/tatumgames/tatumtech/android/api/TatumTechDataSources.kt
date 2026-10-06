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

import com.tatumgames.tatumtech.android.api.local.LocalJsonAssetSource
import com.tatumgames.tatumtech.android.api.local.LocalJsonRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.analytics.AnalyticsClient
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.executor.OkHttpRequestExecutor

/**
 * The single place where [TatumTechDataSourceMode] is turned into a request executor. The
 * client and the screens using it are the same in both modes.
 */
object TatumTechDataSources {

    fun createExecutor(
        configuration: TatumTechClientConfiguration,
        localJsonSource: LocalJsonAssetSource
    ): HttpRequestExecutor = when (configuration.dataSourceMode) {
        TatumTechDataSourceMode.NETWORK -> OkHttpRequestExecutor(configuration.connectTimeout)
        TatumTechDataSourceMode.LOCAL_JSON -> LocalJsonRequestExecutor(localJsonSource)
    }

    /**
     * Creates a client for [TatumTechClientConfiguration.dataSourceMode]. The mode is fixed for
     * the client's lifetime; `updateConfiguration` does not switch it.
     */
    fun createClient(
        configuration: TatumTechClientConfiguration,
        localJsonSource: LocalJsonAssetSource,
        analyticsClient: AnalyticsClient? = null
    ): TatumTechApiClient = TatumTechApiClient(
        configuration = configuration,
        executor = createExecutor(configuration, localJsonSource),
        analyticsClient = analyticsClient
    )
}
