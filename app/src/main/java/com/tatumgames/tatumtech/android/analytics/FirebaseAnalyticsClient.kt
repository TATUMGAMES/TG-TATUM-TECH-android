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

import com.tatumgames.tatumtech.framework.android.http.analytics.AnalyticsClient
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpErrorEvent
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpErrorType

/**
 * Reports Tatum Tech API failures to Firebase as the app's `api_error` event, through
 * [AnalyticsService]. Successful requests are not logged, to keep event volume low.
 *
 * @param reportApiError Receives endpoint, method, status code, duration, and error type.
 */
class FirebaseAnalyticsClient(
    private val reportApiError: (String, String, Int?, Long, String) -> Unit = { endpoint, method, statusCode, durationMs, errorType ->
        AnalyticsService.apiError(endpoint, method, statusCode, durationMs, errorType)
    }
) : AnalyticsClient {

    override fun logHttpError(event: HttpErrorEvent) {
        reportApiError(
            event.path,
            event.method.name,
            event.statusCode,
            event.durationMs,
            event.errorType.toApiErrorType()
        )
    }

    private fun HttpErrorType.toApiErrorType(): String = when (this) {
        HttpErrorType.HTTP -> ApiErrorTypes.HTTP
        HttpErrorType.NETWORK -> ApiErrorTypes.IO
        HttpErrorType.SERIALIZATION -> ApiErrorTypes.PARSE
        HttpErrorType.UNEXPECTED -> ApiErrorTypes.UNKNOWN
    }
}
