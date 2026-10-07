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
import com.tatumgames.tatumtech.android.analytics.FirebaseAnalyticsClient
import com.tatumgames.tatumtech.android.api.TatumTechApiProvider
import com.tatumgames.tatumtech.android.api.TatumTechAppConfiguration
import com.tatumgames.tatumtech.android.constants.Constants
import com.tatumgames.tatumtech.android.data.content.TatumTechContentRepository
import com.tatumgames.tatumtech.android.reminders.AppForegroundTracker
import com.tatumgames.tatumtech.android.reminders.MeetingReminderNotifier
import com.tatumgames.tatumtech.android.reminders.MeetingReminders
import com.tatumgames.tatumtech.framework.android.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TatumTechApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        AnalyticsService.initialize(this)
        installUnhandledExceptionBridge()
        TatumTechApiProvider.initialize(
            this,
            TatumTechAppConfiguration.fromBuildConfig(),
            FirebaseAnalyticsClient()
        )
        TatumTechApiProvider.getSessionManager().refreshInBackground()
        initializeMeetingReminders()
    }

    /**
     * Reminders are rescheduled from fresh event data on every process start, which also
     * delivers any reminder whose window opened while the app was closed.
     */
    private fun initializeMeetingReminders() {
        AppForegroundTracker.install(this)
        MeetingReminderNotifier.createChannel(this)
        applicationScope.launch {
            MeetingReminders.syncFromRepository(this@TatumTechApplication, TatumTechContentRepository())
        }
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
                e.printStackTrace()
                Logger.e(Constants.TAG, "Failed to record unhandled exception: ${e.message}")
            }
            previous?.uncaughtException(thread, throwable)
        }
    }
}
