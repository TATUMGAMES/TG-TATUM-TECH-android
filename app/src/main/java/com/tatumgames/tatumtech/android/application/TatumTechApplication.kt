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
package com.tatumgames.tatumtech.android.application

import android.app.Application
import com.tatumgames.tatumtech.android.analytics.AnalyticsService
import com.tatumgames.tatumtech.android.constants.Constants
import com.tatumgames.tatumtech.framework.android.logger.Logger

class TatumTechApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AnalyticsService.initialize(this)
        installUnhandledExceptionBridge()
    }

    /**
     * Crashlytics already hooks the default handler. This adds a best-effort Analytics
     * `exception` (handled=false) signal, then delegates to the previous handler.
     */
    private fun installUnhandledExceptionBridge() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                AnalyticsService.recordUnhandledException(throwable)
            } catch (e: Exception) {
                Logger.e(Constants.TAG, "Failed to record unhandled exception: ${e.message}")
            }
            previous?.uncaughtException(thread, throwable)
        }
    }
}
