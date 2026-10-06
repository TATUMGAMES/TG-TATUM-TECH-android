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

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * [HttpRequestExecutor] backed by OkHttp. OkHttp types never appear in this class's public API.
 *
 * Calls run on OkHttp's dispatcher threads, so [execute] never blocks the caller's thread.
 * Reuse one instance per API client so connections are pooled. Cancelling the calling coroutine
 * cancels the call, and every response is closed whether or not its body was read.
 */
class OkHttpRequestExecutor internal constructor(
    private val client: OkHttpClient
) : HttpRequestExecutor {

    /**
     * @param connectTimeoutMillis `null` uses [DEFAULT_CONNECT_TIMEOUT_MS].
     */
    constructor(
        connectTimeoutMillis: Long? = null,
        readTimeoutMillis: Long = DEFAULT_READ_TIMEOUT_MS,
        writeTimeoutMillis: Long = DEFAULT_WRITE_TIMEOUT_MS
    ) : this(
        OkHttpClient.Builder()
            .connectTimeout(connectTimeoutMillis ?: DEFAULT_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeoutMillis, TimeUnit.MILLISECONDS)
            .build()
    )

    internal val connectTimeoutMillis: Long get() = client.connectTimeoutMillis.toLong()

    override suspend fun execute(request: HttpRequest): HttpResponse {
        val call = client.newCall(request.toOkHttpRequest())
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        // Cancelled while headers were arriving: release the connection unread.
                        if (!continuation.isActive) return
                        // Any failure must resume the caller, or its coroutine would hang.
                        val result = try {
                            it.toHttpResponse()
                        } catch (e: Exception) {
                            if (continuation.isActive) continuation.resumeWithException(e)
                            return
                        }
                        if (continuation.isActive) continuation.resume(result)
                    }
                }
            })
        }
    }

    private fun HttpRequest.toOkHttpRequest(): Request {
        val parsed = url.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid request URL: $url")
        val httpUrl = parsed.newBuilder().apply {
            queryParameters.forEach { (name, value) -> addQueryParameter(name, value) }
        }.build()

        val requestBody = body?.toRequestBody(contentType.toMediaTypeOrNull())
            ?: if (method.requiresBody()) ByteArray(0).toRequestBody(null) else null

        return Request.Builder()
            .url(httpUrl)
            .apply { headers.forEach { (name, value) -> header(name, value) } }
            .method(method.name, requestBody)
            .build()
    }

    private fun Response.toHttpResponse() = HttpResponse(
        statusCode = code,
        url = request.url.toString(),
        headers = headers.toMultimap().mapKeys { it.key.lowercase() }
            .mapValues { it.value.joinToString(",") },
        body = body?.string()
    )

    private fun HttpMethod.requiresBody(): Boolean =
        this == HttpMethod.POST || this == HttpMethod.PUT || this == HttpMethod.PATCH

    companion object {
        const val DEFAULT_CONNECT_TIMEOUT_MS = 15_000L
        const val DEFAULT_READ_TIMEOUT_MS = 30_000L
        const val DEFAULT_WRITE_TIMEOUT_MS = 30_000L
    }
}
