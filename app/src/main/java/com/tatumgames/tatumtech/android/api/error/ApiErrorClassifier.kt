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

import com.tatumgames.tatumtech.framework.android.http.response.ApiError

/**
 * What went wrong with an API call, independent of how the server or transport reported it.
 *
 * @param isTransient Trying the same request again later may succeed.
 */
enum class ApiErrorKind(val isTransient: Boolean) {
    NETWORK_UNAVAILABLE(true),
    TIMEOUT(true),
    BAD_REQUEST(false),
    UNAUTHORIZED(false),
    FORBIDDEN(false),
    NOT_FOUND(false),
    CONFLICT(false),
    RATE_LIMITED(true),
    SERVER_ERROR(true),

    /** A response arrived but could not be read (malformed JSON, missing fields, HTML page). */
    INVALID_RESPONSE(false),
    UNKNOWN(false)
}

/**
 * Codes the Tatum Tech API sends in `status.statusMessage`. They are machine identifiers, never
 * shown to users; the UI maps them to its own copy.
 */
enum class TatumTechServerCode(val kind: ApiErrorKind) {
    USER_ALREADY_EXISTS(ApiErrorKind.CONFLICT),
    INVALID_EMAIL_FORMAT(ApiErrorKind.BAD_REQUEST),
    INVALID_PASSWORD_FORMAT(ApiErrorKind.BAD_REQUEST),
    PASSWORDS_DO_NOT_MATCH(ApiErrorKind.BAD_REQUEST),
    WRONG_EMAIL_OR_PASSWORD(ApiErrorKind.UNAUTHORIZED),
    REFRESH_TOKEN_DOES_NOT_EXIST(ApiErrorKind.UNAUTHORIZED),
    UNAUTHORIZED(ApiErrorKind.UNAUTHORIZED),
    EVENT_NOT_FOUND(ApiErrorKind.NOT_FOUND);

    companion object {
        fun from(code: String?): TatumTechServerCode? =
            entries.firstOrNull { it.name.equals(code?.trim(), ignoreCase = true) }
    }
}

/**
 * @param statusCode Status the API reported (in the body for Tatum Tech envelopes), when known.
 * @param serverCode Machine code such as `USER_ALREADY_EXISTS`, when the server sent one.
 * @param userMessage Server text that passed [SafeServerMessage]; `null` otherwise.
 */
data class ClassifiedApiError(
    val kind: ApiErrorKind,
    val statusCode: Int? = null,
    val serverCode: String? = null,
    val userMessage: String? = null
) {
    val knownServerCode: TatumTechServerCode? get() = TatumTechServerCode.from(serverCode)
}

/**
 * The single place that interprets HTTP statuses, server codes, and transport failures.
 * Screens never inspect status codes themselves.
 */
object ApiErrorClassifier {

    private val MACHINE_CODE = Regex("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)+$|^[A-Z]{3,}$")

    fun classify(error: ApiError): ClassifiedApiError = when (error) {
        is ApiError.Network -> ClassifiedApiError(
            if (error.isTimeout) ApiErrorKind.TIMEOUT else ApiErrorKind.NETWORK_UNAVAILABLE
        )
        is ApiError.Http -> classifyHttp(error)
        is ApiError.Serialization -> ClassifiedApiError(ApiErrorKind.INVALID_RESPONSE, error.statusCode.code)
        is ApiError.Unexpected -> ClassifiedApiError(ApiErrorKind.UNKNOWN)
    }

    /** Category for a status code alone, used when the server sent no recognized code. */
    fun kindForStatus(statusCode: Int): ApiErrorKind = when (statusCode) {
        401 -> ApiErrorKind.UNAUTHORIZED
        403 -> ApiErrorKind.FORBIDDEN
        404, 410 -> ApiErrorKind.NOT_FOUND
        408 -> ApiErrorKind.TIMEOUT
        409 -> ApiErrorKind.CONFLICT
        429 -> ApiErrorKind.RATE_LIMITED
        in 400..499 -> ApiErrorKind.BAD_REQUEST
        in 500..599 -> ApiErrorKind.SERVER_ERROR
        else -> ApiErrorKind.UNKNOWN
    }

    /** `USER_ALREADY_EXISTS`-style identifiers, as opposed to sentences meant for people. */
    fun isMachineCode(text: String): Boolean = MACHINE_CODE.matches(text.trim())

    private fun classifyHttp(error: ApiError.Http): ClassifiedApiError {
        val texts = error.errors.flatMap { listOfNotNull(it.code, it.message) }
            .map(String::trim)
            .filter(String::isNotEmpty)
        val serverCode = texts.firstOrNull(::isMachineCode)
        val userMessage = error.errors.firstNotNullOfOrNull { item ->
            item.message?.trim()?.takeIf { !isMachineCode(it) && SafeServerMessage.isSafe(it) }
        }
        val statusCode = error.statusCode.code
        val kind = TatumTechServerCode.from(serverCode)?.kind ?: kindForStatus(statusCode)
        return ClassifiedApiError(kind, statusCode, serverCode, userMessage)
    }
}

/**
 * Decides whether server-provided text may be shown to users. Anything that looks technical
 * (markup, URLs, stack traces, database or runtime errors) or is too long is rejected, and the
 * UI shows its own copy instead.
 */
object SafeServerMessage {

    const val MAX_LENGTH = 200

    private val TECHNICAL = Regex(
        "exception|stack ?trace|traceback|\\bsql\\b|syntax error|undefined|null ?pointer|" +
            "\\bnull\\b|errno|fatal|segmentation|\\bat [\\w$]+\\.[\\w$]+|line \\d+|" +
            "localhost|\\b\\d{1,3}(\\.\\d{1,3}){3}\\b",
        RegexOption.IGNORE_CASE
    )

    fun isSafe(text: String): Boolean {
        val message = text.trim()
        if (message.length !in 3..MAX_LENGTH || !message.contains(' ')) return false
        if (message.any { it in "<>{}[]\\`\n\r\t|" }) return false
        if (message.contains("://") || message.contains("www.", ignoreCase = true)) return false
        return !TECHNICAL.containsMatchIn(message)
    }
}
