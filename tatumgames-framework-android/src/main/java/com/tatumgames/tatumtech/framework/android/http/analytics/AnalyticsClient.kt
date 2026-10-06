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
package com.tatumgames.tatumtech.framework.android.http.analytics

import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod

/**
 * Receives HTTP activity from API clients. Applications adapt this to their analytics provider
 * (any vendor, or nothing); the framework never knows which.
 *
 * Events carry the request path, never the query string, headers, or bodies, so they contain no
 * tokens or user input. Paths may still include resource ids; sanitize them in the implementation
 * if your provider must not receive them.
 *
 * Implementations are called on the API client's work dispatcher (a background thread by
 * default) and should return quickly. Exceptions
 * they throw are swallowed so analytics can never break a request.
 */
interface AnalyticsClient {

    fun logHttpRequest(event: HttpRequestEvent) {}

    fun logHttpResponse(event: HttpResponseEvent) {}

    fun logHttpError(event: HttpErrorEvent) {}
}

data class HttpRequestEvent(
    val method: HttpMethod,
    val path: String
)

/**
 * A 2xx response that was parsed successfully.
 */
data class HttpResponseEvent(
    val method: HttpMethod,
    val path: String,
    val statusCode: Int,
    val durationMs: Long
)

/**
 * Any failure. [statusCode] is `null` when no response was received.
 */
data class HttpErrorEvent(
    val method: HttpMethod,
    val path: String,
    val statusCode: Int?,
    val durationMs: Long,
    val errorType: HttpErrorType
)

enum class HttpErrorType {
    // Non-2xx status
    HTTP,

    // No response (connectivity, DNS, TLS, timeout)
    NETWORK,

    // 2xx whose body could not be parsed
    SERIALIZATION,

    // Request could not be built or failed unexpectedly
    UNEXPECTED
}
