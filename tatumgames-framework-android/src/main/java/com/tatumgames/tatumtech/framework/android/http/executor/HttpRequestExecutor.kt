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
package com.tatumgames.tatumtech.framework.android.http.executor

import java.io.IOException

enum class HttpMethod {
    GET, POST, PUT, PATCH, DELETE
}

/**
 * Library-neutral HTTP request.
 *
 * @param url Absolute URL without query string; query values go in [queryParameters] unencoded.
 * @param body Serialized request body, sent with [contentType] when non-null.
 */
data class HttpRequest(
    val method: HttpMethod,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val queryParameters: Map<String, String> = emptyMap(),
    val body: String? = null,
    val contentType: String = CONTENT_TYPE_JSON
) {
    companion object {
        const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
    }
}

/**
 * Library-neutral HTTP response.
 *
 * @param url Final URL that was requested, including the encoded query string.
 * @param headers Header names are lower-cased.
 */
data class HttpResponse(
    val statusCode: Int,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

/**
 * Performs a single HTTP exchange. Implementations hide the underlying HTTP library, must be
 * safe to call from any thread or coroutine, and must honor coroutine cancellation.
 */
interface HttpRequestExecutor {

    /**
     * @return The response for any status code, including non-2xx.
     * @throws IOException when no response was received (connectivity, timeout, TLS, ...).
     */
    suspend fun execute(request: HttpRequest): HttpResponse
}

/**
 * Rewrites a request before execution, e.g. to add headers. Applied in registration order.
 */
fun interface HttpInterceptor {
    fun intercept(request: HttpRequest): HttpRequest
}
