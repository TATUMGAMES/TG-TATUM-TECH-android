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

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Extracts [ErrorItem]s from a non-2xx response body. Supply your own when an API uses an
 * error envelope that [DefaultErrorParser] does not understand.
 */
fun interface ErrorParser {

    /**
     * Must not throw error; return an empty list when nothing can be extracted.
     * */
    fun parse(body: String?, statusCode: HttpStatusCode): List<ErrorItem>
}

/**
 * Understands the common shapes:
 * - `{"errors": [{"code": .., "message": .., "field": ..}]}` (items may also be plain strings)
 * - `{"error": {"code": .., "message": ..}}` or `{"error": ".."}`
 * - `{"message": "..", "code": ..}`
 * - `{"status": {"statusCode": .., "statusMessage": ".."}}`
 */
object DefaultErrorParser : ErrorParser {

    override fun parse(body: String?, statusCode: HttpStatusCode): List<ErrorItem> {
        if (body.isNullOrBlank()) return emptyList()
        val root = try {
            JsonParser.parseString(body)
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
        if (!root.isJsonObject) return emptyList()
        val json = root.asJsonObject

        json.get("errors")?.takeIf { it.isJsonArray }?.asJsonArray?.let { array ->
            return array.mapNotNull { it.toErrorItem() }
        }
        json.get("error")?.toErrorItem()?.let { return listOf(it) }
        json.toErrorItem()?.let { return listOf(it) }
        json.get("status")?.takeIf { it.isJsonObject }?.asJsonObject?.let { status ->
            val message = status.string("statusMessage") ?: status.string("message")
            val code = status.string("statusCode") ?: status.string("code")
            if (message != null || code != null) return listOf(ErrorItem(code = code, message = message))
        }
        return emptyList()
    }

    private fun JsonElement.toErrorItem(): ErrorItem? = when {
        isJsonPrimitive -> asString.takeIf { it.isNotBlank() }?.let { ErrorItem(message = it) }
        isJsonObject -> asJsonObject.let { obj ->
            val message = obj.string("message")
            val code = obj.string("code")
            if (message == null && code == null) null
            else ErrorItem(code = code, message = message, field = obj.string("field"))
        }
        else -> null
    }

    private fun JsonObject.string(name: String): String? =
        get(name)?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
}
