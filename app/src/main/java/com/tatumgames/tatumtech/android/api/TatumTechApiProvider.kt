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

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.tatumgames.tatumtech.android.BuildConfig
import com.tatumgames.tatumtech.android.api.local.AndroidAssetJsonSource
import com.tatumgames.tatumtech.android.api.session.KeystoreSessionStore
import com.tatumgames.tatumtech.android.api.session.TatumTechSessionManager
import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.framework.android.http.analytics.AnalyticsClient
import com.tatumgames.tatumtech.framework.android.logger.Logger

/**
 * Application-wide holder of the single [TatumTechApiClient] and its [TatumTechSessionManager].
 *
 * Call [initialize] once from the Application class; afterwards [getInstance] and
 * [getSessionManager] return the same objects everywhere. Tokens are managed by the session
 * manager; other settings can be changed with [TatumTechApiClient.updateConfiguration].
 */
object TatumTechApiProvider {

    @Volatile
    private var client: TatumTechApiClient? = null

    @Volatile
    private var sessionManager: TatumTechSessionManager? = null

    val isInitialized: Boolean get() = client != null

    /**
     * Creates the shared client on the first call, reading from the network or bundled assets
     * according to [TatumTechClientConfiguration.dataSourceMode], and restores any stored
     * session into it. Later calls return the existing client and ignore their arguments.
     */
    fun initialize(
        context: Context,
        configuration: TatumTechClientConfiguration,
        analyticsClient: AnalyticsClient? = null
    ): TatumTechApiClient = synchronized(this) {
        client ?: initialize(
            clientFactory = {
                TatumTechDataSources.createClient(configuration, AndroidAssetJsonSource(context), analyticsClient)
            },
            sessionFactory = { TatumTechSessionManager(::getInstance, KeystoreSessionStore(context)) }
        ).also { Logger.i(TAG, describeEnvironment(configuration, BuildConfig.TATUM_TECH_ENVIRONMENT_OVERRIDDEN)) }
    }

    /** One line naming the deployment in use and why it was chosen, for the debug log. */
    internal fun describeEnvironment(configuration: TatumTechClientConfiguration, overridden: Boolean): String {
        val reason = when {
            !configuration.debugMode -> "release build"
            overridden -> "set by tatumTech.environment"
            else -> "debug build default"
        }
        val name = configuration.environment?.name ?: "CUSTOM"
        return "Tatum Tech API: $name ${configuration.baseUrl} ($reason), data source ${configuration.dataSourceMode}"
    }

    @VisibleForTesting
    internal fun initialize(
        clientFactory: () -> TatumTechApiClient,
        sessionFactory: (() -> TatumTechSessionManager)? = null
    ): TatumTechApiClient = client ?: synchronized(this) {
        client ?: clientFactory().also { created ->
            client = created
            sessionManager = sessionFactory?.invoke()?.apply { restore() }
        }
    }

    /**
     * @throws IllegalStateException if [initialize] has not been called yet.
     */
    fun getInstance(): TatumTechApiClient = client ?: throw notInitialized("getInstance")

    /**
     * @throws IllegalStateException if [initialize] has not been called yet.
     */
    fun getSessionManager(): TatumTechSessionManager = sessionManager ?: throw notInitialized("getSessionManager")

    private fun notInitialized(method: String) = IllegalStateException(
        "TatumTechApiProvider.$method() was called before initialize(). " +
            "Initialize it in the Application class's onCreate()."
    )

    @VisibleForTesting
    internal fun reset() {
        synchronized(this) {
            client = null
            sessionManager = null
        }
    }
}
