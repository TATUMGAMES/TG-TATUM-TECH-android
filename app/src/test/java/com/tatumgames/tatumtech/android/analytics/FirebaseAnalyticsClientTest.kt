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

import com.tatumgames.tatumtech.framework.android.http.analytics.HttpErrorEvent
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpErrorType
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpRequestEvent
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpResponseEvent
import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseAnalyticsClientTest {

    private data class Reported(
        val endpoint: String,
        val method: String,
        val statusCode: Int?,
        val durationMs: Long,
        val errorType: String
    )

    private val reported = mutableListOf<Reported>()

    private val client = FirebaseAnalyticsClient { endpoint, method, statusCode, durationMs, errorType ->
        reported += Reported(endpoint, method, statusCode, durationMs, errorType)
    }

    @Test
    fun httpError_isReportedWithPathMethodStatusAndDuration() {
        client.logHttpError(HttpErrorEvent(HttpMethod.GET, "tatum-tech/partners", 404, 120, HttpErrorType.HTTP))

        assertEquals(
            listOf(Reported("tatum-tech/partners", "GET", 404, 120, ApiErrorTypes.HTTP)),
            reported
        )
    }

    @Test
    fun errorTypes_mapToAppApiErrorTypes() {
        HttpErrorType.entries.forEach { type ->
            client.logHttpError(HttpErrorEvent(HttpMethod.POST, "tatum-tech/signin", null, 5, type))
        }

        assertEquals(
            listOf(ApiErrorTypes.HTTP, ApiErrorTypes.IO, ApiErrorTypes.PARSE, ApiErrorTypes.UNKNOWN),
            reported.map { it.errorType }
        )
        assertTrue(reported.all { it.statusCode == null && it.method == "POST" })
    }

    @Test
    fun successfulTraffic_isNotReported() {
        client.logHttpRequest(HttpRequestEvent(HttpMethod.GET, "tatum-tech/upcomingEvents"))
        client.logHttpResponse(HttpResponseEvent(HttpMethod.GET, "tatum-tech/upcomingEvents", 200, 80))

        assertTrue(reported.isEmpty())
    }
}
