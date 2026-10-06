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
package com.tatumgames.tatumtech.framework.android.http.response

import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod
import java.io.IOException

/**
 * Where and how long a request went, captured for both outcomes.
 *
 * @param url Final request URL including query string.
 * @param path Path the client asked for, without base URL or query (safe for analytics).
 */
data class ResponseMetadata(
    val method: HttpMethod,
    val url: String,
    val path: String,
    val durationMs: Long
)

/**
 * Result of an API call: typed data on 2xx, otherwise an [ApiError].
 */
sealed class ApiResponse<out T> {

    data class Success<out T>(
        val data: T,
        val statusCode: HttpStatusCode,
        val metadata: ResponseMetadata
    ) : ApiResponse<T>()

    data class Failure(val error: ApiError) : ApiResponse<Nothing>()

    val isSuccess: Boolean get() = this is Success

    fun getOrNull(): T? = (this as? Success)?.data

    fun errorOrNull(): ApiError? = (this as? Failure)?.error

    inline fun <R> map(transform: (T) -> R): ApiResponse<R> = when (this) {
        is Success -> Success(transform(data), statusCode, metadata)
        is Failure -> this
    }
}

/**
 * Why an API call failed. HTTP failures are distinguishable from transport, parsing, and
 * client-side failures so callers can react differently (retry, re-authenticate, report).
 */
sealed class ApiError {

    abstract val message: String

    /**
     * Present whenever a request was actually attempted.
     */
    abstract val metadata: ResponseMetadata?

    /**
     * The server answered with a non-2xx status.
     */
    data class Http(
        val statusCode: HttpStatusCode,
        val errors: List<ErrorItem>,
        val rawBody: String?,
        override val metadata: ResponseMetadata
    ) : ApiError() {
        override val message: String
            get() = errors.firstNotNullOfOrNull { it.message } ?: statusCode.toString()
    }

    /**
     * No usable response: connectivity, DNS, TLS, or timeout.
     */
    data class Network(
        val cause: IOException,
        override val metadata: ResponseMetadata
    ) : ApiError() {
        override val message: String get() = cause.message ?: "Network error"
    }

    /**
     * A 2xx response whose body could not be mapped to the expected type.
     */
    data class Serialization(
        val cause: Throwable,
        val statusCode: HttpStatusCode,
        val rawBody: String?,
        override val metadata: ResponseMetadata
    ) : ApiError() {
        override val message: String get() = cause.message ?: "Could not parse response"
    }

    /**
     * The request could not be built or failed unexpectedly (e.g. missing base URL).
     */
    data class Unexpected(
        val cause: Throwable,
        override val metadata: ResponseMetadata?
    ) : ApiError() {
        override val message: String get() = cause.message ?: cause::class.java.simpleName
    }
}

/**
 * One error reported by the server. All fields are optional because APIs differ in shape.
 */
data class ErrorItem(
    val code: String? = null,
    val message: String? = null,
    val field: String? = null
)

/**
 * Successful response that carries no data the caller needs (e.g. 204, or `"data": {}`).
 */
data class EmptyStateInfo(
    val statusCode: HttpStatusCode
)
