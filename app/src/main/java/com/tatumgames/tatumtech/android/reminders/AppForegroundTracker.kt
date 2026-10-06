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
package com.tatumgames.tatumtech.android.reminders

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Whether any Tatum Tech activity is currently started. A process-wide singleton because the
 * answer belongs to the process and is read by background work that has no UI reference; it
 * holds only a counter, never an Activity.
 */
object AppForegroundTracker : Application.ActivityLifecycleCallbacks {

    private var startedActivities = 0
    private var restartingForConfigurationChange = false
    private var installed = false

    val isInForeground: Boolean
        @Synchronized get() = startedActivities > 0

    @Synchronized
    fun install(application: Application) {
        if (installed) return
        installed = true
        application.registerActivityLifecycleCallbacks(this)
    }

    @Synchronized
    override fun onActivityStarted(activity: Activity) {
        if (restartingForConfigurationChange) {
            restartingForConfigurationChange = false
        } else {
            startedActivities++
        }
    }

    /**
     * A rotation stops and restarts the activity; it never leaves the foreground.
     */
    @Synchronized
    override fun onActivityStopped(activity: Activity) {
        if (activity.isChangingConfigurations) {
            restartingForConfigurationChange = true
        } else {
            startedActivities = (startedActivities - 1).coerceAtLeast(0)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
