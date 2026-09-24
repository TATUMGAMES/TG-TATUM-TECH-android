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
package com.tatumgames.tatumtech.android.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tatumgames.tatumtech.android.BuildConfig
import com.tatumgames.tatumtech.android.constants.Constants
import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.framework.android.logger.Logger

/**
 * Centralized product analytics + crash/exception reporting.
 * UI and domain code should call this instead of Firebase APIs directly.
 *
 * Never logs PII, tokens, or secrets as Analytics parameters.
 */
object AnalyticsService {

    @Volatile
    private var analytics: FirebaseAnalytics? = null

    @Volatile
    private var crashlytics: FirebaseCrashlytics? = null

    fun initialize(context: Context) {
        if (analytics != null) {
            return
        }
        synchronized(this) {
            if (analytics != null) {
                return
            }
            val appContext = context.applicationContext
            analytics = FirebaseAnalytics.getInstance(appContext)
            crashlytics = FirebaseCrashlytics.getInstance().also {
                it.isCrashlyticsCollectionEnabled = true
                it.setCustomKey("application_id", BuildConfig.APPLICATION_ID)
                it.setCustomKey("build_type", BuildConfig.BUILD_TYPE)
            }
            if (BuildConfig.DEBUG) {
                // Enables Firebase Analytics DebugView for debug installs.
                analytics?.setAnalyticsCollectionEnabled(true)
                Logger.d(TAG, "AnalyticsService initialized (debug)")
            }
        }
    }

    fun navigate(screenName: String) {
        val name = AnalyticsSanitizer.screenNameFromRoute(screenName)
        logEvent(
            AnalyticsEvents.NAVIGATE,
            bundleOf(AnalyticsParams.SCREEN_NAME to name)
        )
    }

    fun updateProfile(value: String) {
        logEvent(
            AnalyticsEvents.UPDATE_PROFILE,
            bundleOf(AnalyticsParams.VALUE to value.lowercase())
        )
    }

    fun scanContactCard() {
        logEvent(AnalyticsEvents.SCAN_CONTACT_CARD, Bundle())
    }

    fun createContactCard() {
        logEvent(AnalyticsEvents.CREATE_CONTACT_CARD, Bundle())
    }

    /**
     * Product-level exception signal for Analytics; stack traces go to Crashlytics only.
     */
    fun recordHandledException(throwable: Throwable) {
        logEvent(
            AnalyticsEvents.EXCEPTION,
            bundleOf(AnalyticsParams.HANDLED to "true")
        )
        crashlytics?.recordException(throwable)
        Logger.e(Constants.TAG, "Handled exception: ${throwable.message}")
    }

    /**
     * For uncaught crashes Crashlytics captures automatically. This records a best-effort
     * Analytics signal; delivery before process death is not guaranteed.
     */
    fun recordUnhandledException(throwable: Throwable) {
        logEvent(
            AnalyticsEvents.EXCEPTION,
            bundleOf(AnalyticsParams.HANDLED to "false")
        )
        crashlytics?.recordException(throwable)
    }

    fun apiError(
        endpoint: String,
        method: String,
        statusCode: Int?,
        durationMs: Long,
        errorType: String,
        throwable: Throwable? = null
    ) {
        val params = Bundle().apply {
            putString(AnalyticsParams.ENDPOINT, AnalyticsSanitizer.sanitizeEndpoint(endpoint))
            putString(AnalyticsParams.METHOD, method.lowercase())
            putLong(AnalyticsParams.DURATION_MS, durationMs.coerceAtLeast(0L))
            putString(AnalyticsParams.ERROR_TYPE, errorType.lowercase())
            if (statusCode != null) {
                putLong(AnalyticsParams.STATUS_CODE, statusCode.toLong())
            }
        }
        logEvent(AnalyticsEvents.API_ERROR, params)
        if (throwable != null) {
            crashlytics?.recordException(throwable)
        }
    }

    private fun logEvent(name: String, params: Bundle) {
        try {
            analytics?.logEvent(name, params)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to log analytics event $name: ${e.message}")
        }
    }

    private fun bundleOf(vararg pairs: Pair<String, String>): Bundle =
        Bundle().apply {
            pairs.forEach { (k, v) -> putString(k, v) }
        }
}
