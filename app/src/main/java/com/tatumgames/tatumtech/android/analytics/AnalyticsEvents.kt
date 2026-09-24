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

/**
 * Centralized Analytics / Crashlytics event and parameter names.
 * All custom names are lowercase with underscores (Firebase-safe).
 */
object AnalyticsEvents {
    const val NAVIGATE = "navigate"
    const val UPDATE_PROFILE = "update_profile"
    const val SCAN_CONTACT_CARD = "scan_contact_card"
    const val CREATE_CONTACT_CARD = "create_contact_card"
    const val EXCEPTION = "exception"
    const val API_ERROR = "api_error"
}

object AnalyticsParams {
    const val SCREEN_NAME = "screen_name"
    const val VALUE = "field"
    const val HANDLED = "handled"
    const val ENDPOINT = "endpoint"
    const val METHOD = "method"
    const val STATUS_CODE = "status_code"
    const val DURATION_MS = "duration_ms"
    const val ERROR_TYPE = "error_type"
}

object ProfileFields {
    const val FIRST_NAME = "first_name"
    const val LAST_NAME = "last_name"
    const val EMAIL = "email"
}

object ApiErrorTypes {
    const val HTTP = "http"
    const val TIMEOUT = "timeout"
    const val CONNECTION = "connection"
    const val IO = "io"
    const val PARSE = "parse"
    const val UNKNOWN = "unknown"
}
