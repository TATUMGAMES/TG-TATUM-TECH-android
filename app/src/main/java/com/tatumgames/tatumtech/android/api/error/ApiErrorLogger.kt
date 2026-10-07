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
package com.tatumgames.tatumtech.android.api.error

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.tatumgames.tatumtech.android.api.TatumTechClientConfiguration
import com.tatumgames.tatumtech.android.constants.Constants.TAG
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.logger.Logger

/**
 * Writes one structured warning per failed Tatum Tech API call (debug builds only, through
 * [Logger]), e.g.:
 *
 * ```
 * [API ERROR]
 * Environment: STAGE
 * Method: POST
 * Endpoint: /tatum-tech/signup
 * HTTP Status: 200
 * API Status: 406
 * Server Code: PASSWORDS_DO_NOT_MATCH
 * Error Type: BAD_REQUEST
 * ```
 *
 * Only the path is logged, never the query string, headers, or request body. Response bodies are
 * summarized with credential-like JSON values masked.
 */
object ApiErrorLogger {

    const val MAX_BODY_CHARS = 500
    const val REDACTED = "[redacted]"

    private val gson = Gson()
    private val SENSITIVE_KEY_SUFFIXES = listOf("password", "token", "secret", "apikey", "authorization", "cookie")
    private val HTML_TITLE = Regex("<title>(.*?)</title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

    fun log(error: ApiError, configuration: TatumTechClientConfiguration) {
        Logger.w(TAG, describe(error, environmentLabel(configuration)))
    }

    fun environmentLabel(configuration: TatumTechClientConfiguration): String =
        configuration.environment?.name ?: "CUSTOM (${configuration.baseUrl})"

    fun describe(error: ApiError, environment: String): String = buildString {
        val classified = ApiErrorClassifier.classify(error)
        appendLine("[API ERROR]")
        appendLine("Environment: $environment")
        error.metadata?.let {
            appendLine("Method: ${it.method}")
            appendLine("Endpoint: /${it.path.trimStart('/')}")
        }
        when (error) {
            is ApiError.Http -> {
                appendLine("HTTP Status: ${error.responseStatusCode ?: error.statusCode.code}")
                if (error.responseStatusCode != null) appendLine("API Status: ${error.statusCode.code}")
            }
            is ApiError.Serialization -> appendLine("HTTP Status: ${error.statusCode.code}")
            is ApiError.Network, is ApiError.Unexpected -> appendLine("HTTP Status: none (no response)")
        }
        classified.serverCode?.let { appendLine("Server Code: $it") }
        appendLine("Error Type: ${classified.kind}")
        cause(error)?.let { appendLine("Exception: ${it.javaClass.name}: ${it.message.orEmpty()}") }
        error.metadata?.let { appendLine("Duration: ${it.durationMs} ms") }
        appendLine("Request ID: not provided by the server")
        rawBody(error)?.let { append("Response: ${summarizeBody(it)}") }
    }.trimEnd()

    /** A short, credential-free description of a response body. */
    fun summarizeBody(body: String): String {
        val trimmed = body.trim()
        return when {
            trimmed.isEmpty() -> "(empty)"
            trimmed.startsWith("{") || trimmed.startsWith("[") -> runCatching {
                truncate(gson.toJson(redact(JsonParser.parseString(trimmed))))
            }.getOrElse { truncate(singleLine(trimmed)) }
            trimmed.startsWith("<") -> {
                val title = HTML_TITLE.find(trimmed)?.groupValues?.get(1)?.trim()
                "HTML page${title?.let { " \"$it\"" }.orEmpty()} (${trimmed.length} characters)"
            }
            else -> truncate(singleLine(trimmed))
        }
    }

    fun isSensitiveKey(key: String): Boolean {
        val normalized = key.lowercase()
        return SENSITIVE_KEY_SUFFIXES.any { normalized.endsWith(it) }
    }

    private fun redact(element: JsonElement): JsonElement = when (element) {
        is JsonObject -> JsonObject().also { copy ->
            element.entrySet().forEach { (key, value) ->
                copy.add(key, if (isSensitiveKey(key)) JsonPrimitive(REDACTED) else redact(value))
            }
        }
        is JsonArray -> JsonArray().also { copy -> element.forEach { copy.add(redact(it)) } }
        else -> element
    }

    private fun cause(error: ApiError): Throwable? = when (error) {
        is ApiError.Network -> error.cause
        is ApiError.Serialization -> error.cause
        is ApiError.Unexpected -> error.cause
        is ApiError.Http -> null
    }

    private fun rawBody(error: ApiError): String? = when (error) {
        is ApiError.Http -> error.rawBody
        is ApiError.Serialization -> error.rawBody
        else -> null
    }

    private fun singleLine(text: String) = text.replace(Regex("\\s+"), " ")

    private fun truncate(text: String) =
        if (text.length <= MAX_BODY_CHARS) text else text.take(MAX_BODY_CHARS) + "… (${text.length} characters)"
}
