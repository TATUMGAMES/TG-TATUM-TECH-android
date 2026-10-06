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
package com.tatumgames.tatumtech.framework.android.http.client

import com.tatumgames.tatumtech.framework.android.constants.Constants
import com.tatumgames.tatumtech.framework.android.constants.Constants.TAG
import com.tatumgames.tatumtech.framework.android.http.analytics.AnalyticsClient
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpErrorEvent
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpErrorType
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpRequestEvent
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpResponseEvent
import com.tatumgames.tatumtech.framework.android.http.config.BaseClientConfiguration
import com.tatumgames.tatumtech.framework.android.http.executor.HttpInterceptor
import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequest
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.executor.HttpResponse
import com.tatumgames.tatumtech.framework.android.http.executor.OkHttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.logging.HttpTrafficLogger
import com.tatumgames.tatumtech.framework.android.http.logging.PrettyHttpTrafficLogger
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ApiResponse
import com.tatumgames.tatumtech.framework.android.http.response.DefaultErrorParser
import com.tatumgames.tatumtech.framework.android.http.response.EmptyStateInfo
import com.tatumgames.tatumtech.framework.android.http.response.ErrorParser
import com.tatumgames.tatumtech.framework.android.http.response.HttpStatusCode
import com.tatumgames.tatumtech.framework.android.http.response.ResponseMetadata
import com.tatumgames.tatumtech.framework.android.http.serialization.GsonJsonSerializer
import com.tatumgames.tatumtech.framework.android.http.serialization.JsonSerializer
import com.tatumgames.tatumtech.framework.android.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.coroutines.cancellation.CancellationException

/**
 * Generic HTTP behavior for application API clients: URL resolution, default headers,
 * serialization, execution, response mapping, error conversion, and analytics.
 *
 * Subclasses expose one typed `suspend` method per endpoint and own every endpoint path and
 * model. Calls never throw for HTTP, network, or parsing problems (they return
 * [ApiResponse.Failure]); only coroutine cancellation propagates.
 *
 * Calls are main-safe and thread-agnostic: request building, serialization, response mapping,
 * error parsing, and analytics run on [workDispatcher], the executor performs I/O on its own
 * threads, and the result is returned on the calling coroutine's dispatcher. Delivering results
 * to the main thread is the caller's concern.
 *
 * @param configuration Initial configuration; replace it later with [updateConfiguration].
 * @param executor Performs the exchange. Its timeouts are fixed when it is created.
 * @param analyticsClient Optional HTTP activity sink.
 * @param interceptors Request rewriters applied after default headers, in order.
 * @param errorParser Extracts server error details from non-2xx bodies.
 * @param workDispatcher Runs everything except the exchange itself.
 * @param trafficLogger Receives every request and response, whatever the executor, but only in
 * debug builds of the framework while [BaseClientConfiguration.debugMode] is on; never in
 * release builds. `null` disables it.
 */
abstract class BaseApiClient<T : BaseClientConfiguration>(
    configuration: T,
    protected val executor: HttpRequestExecutor = OkHttpRequestExecutor(configuration.connectTimeout),
    protected val serializer: JsonSerializer = GsonJsonSerializer(),
    protected val analyticsClient: AnalyticsClient? = null,
    private val interceptors: List<HttpInterceptor> = emptyList(),
    protected val errorParser: ErrorParser = DefaultErrorParser,
    private val workDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val trafficLogger: HttpTrafficLogger? = if (Constants.DEBUG) PrettyHttpTrafficLogger() else null
) {

    @Volatile
    var configuration: T = configuration
        private set

    /**
     * Replaces the configuration for subsequent requests (e.g. after sign-in or token refresh).
     * In-flight requests keep the configuration they started with.
     */
    fun updateConfiguration(configuration: T) {
        this.configuration = configuration
    }

    /**
     * Header that carries [BaseClientConfiguration.apiKey].
     */
    protected open val apiKeyHeaderName: String = DEFAULT_API_KEY_HEADER

    /**
     * Headers added to every request. Per-call headers passed to [execute] override these.
     *
     * @param authenticated Whether the endpoint requires the access token.
     */
    protected open fun defaultHeaders(configuration: T, authenticated: Boolean): Map<String, String> =
        buildMap {
            put(HEADER_ACCEPT, ACCEPT_JSON)
            configuration.apiKey?.let { put(apiKeyHeaderName, it) }
            if (authenticated) {
                val token = configuration.jwtAccessToken
                    ?: throw IllegalStateException("Endpoint requires an access token but none is configured")
                put(HEADER_AUTHORIZATION, token)
            }
        }

    protected suspend inline fun <reified R> get(
        path: String,
        queryParameters: Map<String, String?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        authenticated: Boolean = false
    ): ApiResponse<R> = execute(
        HttpMethod.GET, path, typeReference<R>(), null, queryParameters, headers, authenticated
    )

    protected suspend inline fun <reified R> post(
        path: String,
        body: Any? = null,
        queryParameters: Map<String, String?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        authenticated: Boolean = false
    ): ApiResponse<R> = execute(
        HttpMethod.POST, path, typeReference<R>(), body, queryParameters, headers, authenticated
    )

    protected suspend inline fun <reified R> put(
        path: String,
        body: Any? = null,
        queryParameters: Map<String, String?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        authenticated: Boolean = false
    ): ApiResponse<R> = execute(
        HttpMethod.PUT, path, typeReference<R>(), body, queryParameters, headers, authenticated
    )

    protected suspend inline fun <reified R> patch(
        path: String,
        body: Any? = null,
        queryParameters: Map<String, String?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        authenticated: Boolean = false
    ): ApiResponse<R> = execute(
        HttpMethod.PATCH, path, typeReference<R>(), body, queryParameters, headers, authenticated
    )

    protected suspend inline fun <reified R> delete(
        path: String,
        queryParameters: Map<String, String?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        authenticated: Boolean = false
    ): ApiResponse<R> = execute(
        HttpMethod.DELETE, path, typeReference<R>(), null, queryParameters, headers, authenticated
    )

    /**
     * Captures a full generic type such as `List<Item>` for deserialization.
     */
    protected inline fun <reified R> typeReference(): Type = object : TypeReference<R>() {}.type

    /**
     * Percent-encodes a value used as one path segment, e.g. an id in `items/{id}`.
     */
    protected fun encodePathSegment(value: String): String = UrlResolver.encodePathSegment(value)

    /**
     * Executes one request and maps the outcome.
     *
     * Use [EmptyStateInfo] or [Unit] as [responseType] when the body is irrelevant.
     *
     * @param path Relative to [BaseClientConfiguration.baseUrl], or an absolute URL.
     * @param body Serialized to JSON when non-null.
     * @param queryParameters Entries with `null` values are omitted.
     */
    protected suspend fun <R> execute(
        method: HttpMethod,
        path: String,
        responseType: Type,
        body: Any? = null,
        queryParameters: Map<String, String?> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        authenticated: Boolean = false
    ): ApiResponse<R> = withContext(workDispatcher) {
        executeOnWorkDispatcher(method, path, responseType, body, queryParameters, headers, authenticated)
    }

    private suspend fun <R> executeOnWorkDispatcher(
        method: HttpMethod,
        path: String,
        responseType: Type,
        body: Any?,
        queryParameters: Map<String, String?>,
        headers: Map<String, String>,
        authenticated: Boolean
    ): ApiResponse<R> {
        val started = System.nanoTime()
        val analyticsPath = path.trim().substringBefore('?')
        val config = configuration

        val request = try {
            buildRequest(config, method, path, body, queryParameters, headers, authenticated)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return failure(method, analyticsPath, null, HttpErrorType.UNEXPECTED, started) {
                ApiError.Unexpected(e, metadata = null)
            }
        }

        notifyAnalytics { logHttpRequest(HttpRequestEvent(method, analyticsPath)) }
        logTraffic(config) { logRequest(request) }
        val sent = System.nanoTime()

        val response = try {
            executor.execute(request)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            logTraffic(config) { logFailure(request, e, elapsedMs(sent)) }
            return failure(method, analyticsPath, null, HttpErrorType.NETWORK, started) {
                ApiError.Network(e, metadata(method, request.url, analyticsPath, started))
            }
        } catch (e: Exception) {
            logTraffic(config) { logFailure(request, e, elapsedMs(sent)) }
            return failure(method, analyticsPath, null, HttpErrorType.UNEXPECTED, started) {
                ApiError.Unexpected(e, metadata(method, request.url, analyticsPath, started))
            }
        }
        logTraffic(config) { logResponse(request, response, elapsedMs(sent)) }

        return mapResponse(method, analyticsPath, response, responseType, started)
    }

    private fun buildRequest(
        config: T,
        method: HttpMethod,
        path: String,
        body: Any?,
        queryParameters: Map<String, String?>,
        headers: Map<String, String>,
        authenticated: Boolean
    ): HttpRequest {
        val initial = HttpRequest(
            method = method,
            url = UrlResolver.resolve(config.baseUrl, path),
            headers = defaultHeaders(config, authenticated) + headers,
            queryParameters = queryParameters.mapNotNull { (key, value) ->
                value?.let { key to it }
            }.toMap(),
            body = body?.let { serializer.toJson(it) }
        )
        return interceptors.fold(initial) { request, interceptor -> interceptor.intercept(request) }
    }

    private fun <R> mapResponse(
        method: HttpMethod,
        path: String,
        response: HttpResponse,
        responseType: Type,
        started: Long
    ): ApiResponse<R> {
        val status = HttpStatusCode.fromCode(response.statusCode)
        val metadata = metadata(method, response.url, path, started)

        if (!status.isSuccess) {
            return failure(method, path, status.code, HttpErrorType.HTTP, started) {
                ApiError.Http(status, errorParser.safeParse(response.body, status), response.body, metadata)
            }
        }

        val data: R = try {
            deserialize(response.body, responseType, status)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return failure(method, path, status.code, HttpErrorType.SERIALIZATION, started) {
                ApiError.Serialization(e, status, response.body, metadata)
            }
        }

        notifyAnalytics {
            logHttpResponse(HttpResponseEvent(method, path, status.code, metadata.durationMs))
        }
        return ApiResponse.Success(data, status, metadata)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <R> deserialize(body: String?, type: Type, status: HttpStatusCode): R = when {
        type == EmptyStateInfo::class.java -> EmptyStateInfo(status) as R
        type == Unit::class.java -> Unit as R
        body.isNullOrBlank() -> throw IllegalStateException("Empty response body for $status")
        else -> serializer.fromJson<R>(body, type)
            ?: throw IllegalStateException("Response body deserialized to null")
    }

    private inline fun failure(
        method: HttpMethod,
        path: String,
        statusCode: Int?,
        errorType: HttpErrorType,
        started: Long,
        error: () -> ApiError
    ): ApiResponse.Failure {
        val apiError = error()
        val durationMs = apiError.metadata?.durationMs ?: elapsedMs(started)
        notifyAnalytics {
            logHttpError(HttpErrorEvent(method, path, statusCode, durationMs, errorType))
        }
        return ApiResponse.Failure(apiError)
    }

    private fun metadata(method: HttpMethod, url: String, path: String, started: Long) =
        ResponseMetadata(method = method, url = url, path = path, durationMs = elapsedMs(started))

    private fun elapsedMs(started: Long): Long = (System.nanoTime() - started) / 1_000_000

    private inline fun notifyAnalytics(block: AnalyticsClient.() -> Unit) {
        val client = analyticsClient ?: return
        try {
            client.block()
        } catch (e: Exception) {
            // Analytics must never change the outcome of a request.
        }
    }

    /**
     * Release builds return before [block] runs, so no request or response is ever formatted.
     */
    private inline fun logTraffic(config: T, block: HttpTrafficLogger.() -> Unit) {
        if (!Constants.DEBUG || !config.debugMode) return
        val logger = trafficLogger ?: return
        try {
            logger.block()
        } catch (e: Exception) {
            // Logging must never change the outcome of a request.
        }
    }

    private fun ErrorParser.safeParse(body: String?, status: HttpStatusCode) = try {
        parse(body, status)
    } catch (e: Exception) {
        e.printStackTrace()
        Logger.e(TAG, e.message)
        emptyList()
    }

    companion object {
        const val HEADER_ACCEPT = "Accept"
        const val HEADER_AUTHORIZATION = "Authorization"
        const val ACCEPT_JSON = "application/json"
        const val DEFAULT_API_KEY_HEADER = "x-api-key"
    }
}

/**
 * Captures the full generic type argument of a subclass, e.g. `List<Item>`.
 */
abstract class TypeReference<T> protected constructor() {
    val type: Type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
}
