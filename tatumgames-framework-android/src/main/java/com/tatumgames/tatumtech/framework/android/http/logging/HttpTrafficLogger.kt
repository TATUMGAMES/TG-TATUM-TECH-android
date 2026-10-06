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
package com.tatumgames.tatumtech.framework.android.http.logging

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequest
import com.tatumgames.tatumtech.framework.android.http.executor.HttpResponse
import com.tatumgames.tatumtech.framework.android.http.response.HttpStatusCode
import com.tatumgames.tatumtech.framework.android.logger.Logger
import java.net.URLEncoder

/**
 * Development-time sink for every HTTP exchange made by a
 * [com.tatumgames.tatumtech.framework.android.http.client.BaseApiClient].
 *
 * The client calls it only in debug builds of the framework whose configuration has
 * `debugMode` enabled, around whichever executor it uses (network or local JSON), so
 * implementations never need their own production checks.
 */
interface HttpTrafficLogger {

    fun logRequest(request: HttpRequest)

    fun logResponse(request: HttpRequest, response: HttpResponse, durationMs: Long)

    /** No response was received (connectivity, timeout, TLS, ...). */
    fun logFailure(request: HttpRequest, error: Throwable, durationMs: Long)
}

/**
 * Logs requests under [requestTag] and responses under [responseTag] with pretty-printed JSON,
 * e.g. filter Logcat with `tag:RQ | tag:RS`.
 *
 * Credentials are masked even in debug: headers named in [redactedHeaders] and JSON values whose
 * key matches [isSensitiveJsonKey], at any depth. Non-JSON bodies are logged as-is.
 *
 * @param maxBodyChars Longer bodies are truncated, keeping Logcat usable for large lists.
 * @param sink Receives each Logcat-sized chunk; defaults to [Logger.d].
 */
class PrettyHttpTrafficLogger(
    private val requestTag: String = TAG_REQUEST,
    private val responseTag: String = TAG_RESPONSE,
    redactedHeaders: Set<String> = DEFAULT_REDACTED_HEADERS,
    private val isSensitiveJsonKey: (String) -> Boolean = ::isDefaultSensitiveJsonKey,
    private val maxBodyChars: Int = DEFAULT_MAX_BODY_CHARS,
    private val sink: (tag: String, message: String) -> Unit = Logger::d
) : HttpTrafficLogger {

    private val redactedHeaders = redactedHeaders.map { it.lowercase() }.toSet()
    private val gson = GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create()

    override fun logRequest(request: HttpRequest) {
        emit(requestTag, buildString {
            appendLine("--> ${request.method} ${displayUrl(request)}")
            appendHeaders(request.headers)
            if (request.body != null) appendLine("Content-Type: ${request.contentType}")
            appendBody(request.body)
            append("--> END ${request.method}")
        })
    }

    override fun logResponse(request: HttpRequest, response: HttpResponse, durationMs: Long) {
        val status = HttpStatusCode.fromCode(response.statusCode)
        emit(responseTag, buildString {
            appendLine("<-- $status ${request.method} ${response.url} ($durationMs ms)")
            appendHeaders(response.headers)
            appendBody(response.body)
            append("<-- END ${request.method}")
        })
    }

    override fun logFailure(request: HttpRequest, error: Throwable, durationMs: Long) {
        emit(
            responseTag,
            "<-- FAILED ${request.method} ${displayUrl(request)} ($durationMs ms): " +
                "${error.javaClass.simpleName}: ${error.message}"
        )
    }

    private fun StringBuilder.appendHeaders(headers: Map<String, String>) {
        headers.toSortedMap(String.CASE_INSENSITIVE_ORDER).forEach { (name, value) ->
            appendLine("$name: ${if (name.lowercase() in redactedHeaders) REDACTED else value}")
        }
    }

    private fun StringBuilder.appendBody(body: String?) {
        when {
            body == null -> Unit
            body.isEmpty() -> appendLine("(empty body)")
            else -> appendLine(truncate(prettyBody(body)))
        }
    }

    /** Pretty JSON with sensitive values masked; anything that is not a JSON document as-is. */
    internal fun prettyBody(body: String): String {
        val trimmed = body.trim()
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return body
        return try {
            gson.toJson(redact(JsonParser.parseString(trimmed)))
        } catch (e: RuntimeException) {
            body
        }
    }

    private fun redact(element: JsonElement): JsonElement = when (element) {
        is JsonObject -> JsonObject().also { copy ->
            element.entrySet().forEach { (key, value) ->
                copy.add(key, if (isSensitiveJsonKey(key)) JsonPrimitive(REDACTED) else redact(value))
            }
        }
        is JsonArray -> JsonArray().also { copy -> element.forEach { copy.add(redact(it)) } }
        else -> element
    }

    private fun truncate(text: String): String =
        if (text.length <= maxBodyChars) {
            text
        } else {
            "${text.take(maxBodyChars)}\n… (${text.length - maxBodyChars} more characters)"
        }

    /** Query values are shown readable (encoded only as needed), as the executor will send them. */
    private fun displayUrl(request: HttpRequest): String {
        if (request.queryParameters.isEmpty()) return request.url
        val query = request.queryParameters.entries.joinToString("&") { (name, value) ->
            "${encode(name)}=${encode(value)}"
        }
        return "${request.url}?$query"
    }

    private fun encode(value: String) = URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    /**
     * Logcat drops anything past ~4 KB per entry, so messages are split on line boundaries.
     * Emitting under a lock keeps one exchange's chunks together when requests run in parallel.
     */
    private fun emit(tag: String, message: String) = synchronized(this) {
        chunk(message).forEach { sink(tag, it) }
    }

    internal companion object {
        const val TAG_REQUEST = "RQ"
        const val TAG_RESPONSE = "RS"
        const val REDACTED = "██ redacted ██"
        const val DEFAULT_MAX_BODY_CHARS = 50_000
        const val MAX_CHUNK_CHARS = 3_500

        val DEFAULT_REDACTED_HEADERS = setOf(
            "authorization", "proxy-authorization", "x-api-key", "cookie", "set-cookie"
        )

        private val SENSITIVE_KEY_SUFFIXES = listOf("password", "token", "secret")

        /** `password`, `confirmPassword`, `refreshToken`, `googleIdToken`, `clientSecret`, ... */
        fun isDefaultSensitiveJsonKey(key: String): Boolean {
            val normalized = key.lowercase()
            return normalized == "apikey" || SENSITIVE_KEY_SUFFIXES.any { normalized.endsWith(it) }
        }

        fun chunk(message: String): List<String> {
            if (message.length <= MAX_CHUNK_CHARS) return listOf(message)
            val chunks = mutableListOf<String>()
            val current = StringBuilder()
            message.lineSequence().flatMap { it.chunked(MAX_CHUNK_CHARS).ifEmpty { listOf("") } }
                .forEach { line ->
                    if (current.isNotEmpty() && current.length + 1 + line.length > MAX_CHUNK_CHARS) {
                        chunks += current.toString()
                        current.clear()
                    }
                    if (current.isNotEmpty()) current.append('\n')
                    current.append(line)
                }
            if (current.isNotEmpty()) chunks += current.toString()
            return chunks
        }
    }
}
