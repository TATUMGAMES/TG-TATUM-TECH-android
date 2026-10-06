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

/**
 * HTTP status code with a friendly name. Codes not listed in [Companion] stay representable
 * through [fromCode] with [isKnown] = `false`.
 */
class HttpStatusCode private constructor(
    val code: Int,
    val reasonPhrase: String,
    val isKnown: Boolean
) {

    /** 2xx. */
    val isSuccess: Boolean get() = code in 200..299
    val isClientError: Boolean get() = code in 400..499
    val isServerError: Boolean get() = code in 500..599

    override fun equals(other: Any?): Boolean = other is HttpStatusCode && other.code == code

    override fun hashCode(): Int = code

    override fun toString(): String = "$code $reasonPhrase"

    companion object {
        // Must be declared before the constants below, which register themselves here.
        private val knownCodes = mutableMapOf<Int, HttpStatusCode>()

        private fun known(code: Int, reasonPhrase: String) =
            HttpStatusCode(code, reasonPhrase, isKnown = true).also { knownCodes[code] = it }

        val OK = known(200, "OK")
        val CREATED = known(201, "Created")
        val ACCEPTED = known(202, "Accepted")
        val NO_CONTENT = known(204, "No Content")
        val MOVED_PERMANENTLY = known(301, "Moved Permanently")
        val FOUND = known(302, "Found")
        val NOT_MODIFIED = known(304, "Not Modified")
        val BAD_REQUEST = known(400, "Bad Request")
        val UNAUTHORIZED = known(401, "Unauthorized")
        val FORBIDDEN = known(403, "Forbidden")
        val NOT_FOUND = known(404, "Not Found")
        val METHOD_NOT_ALLOWED = known(405, "Method Not Allowed")
        val REQUEST_TIMEOUT = known(408, "Request Timeout")
        val CONFLICT = known(409, "Conflict")
        val GONE = known(410, "Gone")
        val PAYLOAD_TOO_LARGE = known(413, "Payload Too Large")
        val UNSUPPORTED_MEDIA_TYPE = known(415, "Unsupported Media Type")
        val UNPROCESSABLE_ENTITY = known(422, "Unprocessable Entity")
        val TOO_MANY_REQUESTS = known(429, "Too Many Requests")
        val INTERNAL_SERVER_ERROR = known(500, "Internal Server Error")
        val NOT_IMPLEMENTED = known(501, "Not Implemented")
        val BAD_GATEWAY = known(502, "Bad Gateway")
        val SERVICE_UNAVAILABLE = known(503, "Service Unavailable")
        val GATEWAY_TIMEOUT = known(504, "Gateway Timeout")

        fun fromCode(code: Int): HttpStatusCode =
            knownCodes[code] ?: HttpStatusCode(code, "Unknown", isKnown = false)
    }
}
