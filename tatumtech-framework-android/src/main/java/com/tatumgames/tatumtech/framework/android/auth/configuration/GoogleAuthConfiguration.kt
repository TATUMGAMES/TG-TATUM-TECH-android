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
package com.tatumgames.tatumtech.framework.android.auth.configuration

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.tatumgames.tatumtech.framework.android.auth.interfaces.GoogleAuthCallback

/**
 * Configuration class for Google Authentication.
 * Uses Builder pattern for immutable construction.
 */
class GoogleAuthConfiguration private constructor(
    val context: Context,
    val activity: Activity,
    val webClientId: String,
    val callback: GoogleAuthCallback,
    val legacyLauncher: ActivityResultLauncher<Intent>?
) {

    /**
     * Builder class for GoogleAuthConfiguration.
     * Provides a fluent API for constructing configuration objects.
     */
    class Builder {
        private var context: Context? = null
        private var activity: Activity? = null
        private var webClientId: String? = null
        private var callback: GoogleAuthCallback? = null
        private var legacyLauncher: ActivityResultLauncher<Intent>? = null

        fun context(context: Context): Builder {
            this.context = context
            return this
        }

        fun activity(activity: Activity): Builder {
            this.activity = activity
            return this
        }

        fun webClientId(webClientId: String): Builder {
            this.webClientId = webClientId
            return this
        }

        fun callback(callback: GoogleAuthCallback): Builder {
            this.callback = callback
            return this
        }

        fun legacyLauncher(legacyLauncher: ActivityResultLauncher<Intent>?): Builder {
            this.legacyLauncher = legacyLauncher
            return this
        }

        /**
         * Builds the GoogleAuthConfiguration object.
         * Throws IllegalArgumentException if required parameters are missing.
         *
         * @return GoogleAuthConfiguration
         * @throws IllegalArgumentException if required parameters are not set
         */
        fun build(): GoogleAuthConfiguration {
            val nonNullContext = requireNotNull(context) { ERROR_CONTEXT_REQUIRED }
            val nonNullActivity = requireNotNull(activity) { ERROR_ACTIVITY_REQUIRED }
            val nonNullWebClientId = webClientId?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException(ERROR_WEB_CLIENT_ID_REQUIRED)
            val nonNullCallback = requireNotNull(callback) { ERROR_CALLBACK_REQUIRED }

            return GoogleAuthConfiguration(
                context = nonNullContext,
                activity = nonNullActivity,
                webClientId = nonNullWebClientId,
                callback = nonNullCallback,
                legacyLauncher = legacyLauncher
            )
        }
    }

    companion object {
        /**
         * Creates a new Builder instance for constructing GoogleAuthConfiguration.
         */
        fun builder(): Builder = Builder()

        private const val ERROR_CONTEXT_REQUIRED = "Context is required"
        private const val ERROR_ACTIVITY_REQUIRED = "Activity is required"
        private const val ERROR_WEB_CLIENT_ID_REQUIRED = "Web client ID is required"
        private const val ERROR_CALLBACK_REQUIRED = "Callback is required"
    }
}
