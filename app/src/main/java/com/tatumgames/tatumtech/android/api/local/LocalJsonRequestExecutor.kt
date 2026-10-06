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
package com.tatumgames.tatumtech.android.api.local

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.tatumgames.tatumtech.android.api.models.TatumTechPartnerCategory
import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequest
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.executor.HttpResponse
import com.tatumgames.tatumtech.framework.android.http.response.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URI

/**
 * Reads a bundled JSON file by name.
 */
fun interface LocalJsonAssetSource {
    @Throws(IOException::class)
    fun read(fileName: String): String
}

/** Reads files from the app's `assets/` folder. */
class AndroidAssetJsonSource(context: Context) : LocalJsonAssetSource {
    private val assets = context.applicationContext.assets

    override fun read(fileName: String): String =
        assets.open(fileName).bufferedReader(Charsets.UTF_8).use { it.readText() }
}

/**
 * Answers Tatum Tech API requests from the app's bundled JSON instead of the network, wrapping
 * the data in the same `{"status": ..., "data": ...}` envelope the API returns so that
 * [com.tatumgames.tatumtech.android.api.TatumTechApiClient] parses both modes identically.
 *
 * Served from local data:
 * - `GET tatum-tech/upcomingEvents` — `upcoming_events.json`
 * - `GET tatum-tech/events/{eventId}` — matching event; 404 if absent
 * - `GET tatum-tech/events/{eventId}/speakers` — that event's `virtualSpeakers`; 404 if absent
 * - `GET tatum-tech/partners[?category=]` — `partners.json`, categories mapped to API values
 * - `GET tatum-tech/partners/{partnerId}` — matching partner; 404 if absent
 *
 * Every other request (sign-in, sign-up, token refresh, password reset, sign-out, profile
 * update) has no local data and fails with **501 Not Implemented**, surfaced to callers as
 * `ApiError.Http`, rather than returning invented data.
 */
class LocalJsonRequestExecutor(
    private val assets: LocalJsonAssetSource
) : HttpRequestExecutor {

    override suspend fun execute(request: HttpRequest): HttpResponse = withContext(Dispatchers.IO) {
        val route = routeOf(request.url)
        try {
            respond(request, route)
        } catch (e: IOException) {
            error(request, HttpStatusCode.INTERNAL_SERVER_ERROR, "Local JSON could not be read: ${e.message}")
        } catch (e: JsonParseException) {
            error(request, HttpStatusCode.INTERNAL_SERVER_ERROR, "Local JSON is malformed: ${e.message}")
        } catch (e: IllegalStateException) {
            error(request, HttpStatusCode.INTERNAL_SERVER_ERROR, "Local JSON has an unexpected shape: ${e.message}")
        }
    }

    private fun respond(request: HttpRequest, route: List<String>): HttpResponse {
        if (request.method != HttpMethod.GET) return notImplemented(request, route)
        return when {
            route == listOf(UPCOMING_EVENTS) ->
                success(request, "events", events().toJsonArray())

            route.size == 2 && route[0] == EVENTS ->
                findById(events(), route[1])
                    ?.let { success(request, "event", it) }
                    ?: notFound(request, "Event '${route[1]}'")

            route.size == 3 && route[0] == EVENTS && route[2] == SPEAKERS ->
                findById(events(), route[1])
                    ?.let { success(request, "speakers", it.get("virtualSpeakers") ?: JsonArray()) }
                    ?: notFound(request, "Event '${route[1]}'")

            route == listOf(PARTNERS) -> {
                val category = request.queryParameters[QUERY_CATEGORY]
                val partners = partners()
                    .filter { category == null || it.stringOrNull("category").equals(category, ignoreCase = true) }
                success(request, "partners", partners.toJsonArray())
            }

            route.size == 2 && route[0] == PARTNERS ->
                findById(partners(), route[1])
                    ?.let { success(request, "partner", it) }
                    ?: notFound(request, "Partner '${route[1]}'")

            else -> notImplemented(request, route)
        }
    }

    private fun events(): List<JsonObject> = readArray(EVENTS_FILE)

    /** Partners with their local category labels ("Game Studio Partners") mapped to API values. */
    private fun partners(): List<JsonObject> = readArray(PARTNERS_FILE).onEach { partner ->
        val apiValue = LOCAL_PARTNER_CATEGORIES[partner.stringOrNull("category")]
        if (apiValue != null) partner.addProperty("category", apiValue.apiValue)
    }

    private fun readArray(fileName: String): List<JsonObject> =
        JsonParser.parseString(assets.read(fileName)).asJsonArray.map { it.asJsonObject }

    private fun findById(items: List<JsonObject>, id: String): JsonObject? =
        items.firstOrNull { it.stringOrNull("id") == id }

    private fun List<JsonElement>.toJsonArray() = JsonArray(size).also { array -> forEach(array::add) }

    private fun JsonObject.stringOrNull(name: String): String? =
        get(name)?.takeIf { it.isJsonPrimitive }?.asString

    private fun success(request: HttpRequest, field: String, value: JsonElement): HttpResponse {
        val data = JsonObject().apply { add(field, value) }
        return response(request, HttpStatusCode.OK, data)
    }

    private fun notFound(request: HttpRequest, what: String) =
        error(request, HttpStatusCode.NOT_FOUND, "$what was not found in local JSON")

    private fun notImplemented(request: HttpRequest, route: List<String>) = error(
        request,
        HttpStatusCode.NOT_IMPLEMENTED,
        "${request.method} tatum-tech/${route.joinToString("/")} has no local JSON data; " +
            "use the NETWORK data source"
    )

    private fun error(request: HttpRequest, status: HttpStatusCode, message: String) =
        response(request, status, data = null, message = message)

    private fun response(
        request: HttpRequest,
        status: HttpStatusCode,
        data: JsonElement?,
        message: String = status.reasonPhrase
    ): HttpResponse {
        val envelope = JsonObject().apply {
            add("status", JsonObject().apply {
                addProperty("statusCode", status.code)
                addProperty("statusMessage", message)
            })
            if (data != null) add("data", data)
        }
        return HttpResponse(
            statusCode = status.code,
            url = request.url,
            headers = mapOf("content-type" to HttpRequest.CONTENT_TYPE_JSON),
            body = envelope.toString()
        )
    }

    /** Decoded path segments after `tatum-tech/`, or every segment for other paths. */
    private fun routeOf(url: String): List<String> {
        val segments = URI(url).path.orEmpty().split('/').filter { it.isNotEmpty() }
        val root = segments.indexOf(API_ROOT)
        return if (root >= 0) segments.drop(root + 1) else segments
    }

    internal companion object {
        const val EVENTS_FILE = "upcoming_events.json"
        const val PARTNERS_FILE = "partners.json"

        private const val API_ROOT = "tatum-tech"
        private const val UPCOMING_EVENTS = "upcomingEvents"
        private const val EVENTS = "events"
        private const val SPEAKERS = "speakers"
        private const val PARTNERS = "partners"
        private const val QUERY_CATEGORY = "category"

        /** Category labels used in `partners.json`. */
        val LOCAL_PARTNER_CATEGORIES: Map<String, TatumTechPartnerCategory> = mapOf(
            "Community Partners" to TatumTechPartnerCategory.COMMUNITY,
            "Corporate Partners" to TatumTechPartnerCategory.CORPORATE,
            "Education Partners" to TatumTechPartnerCategory.EDUCATION,
            "Game Studio Partners" to TatumTechPartnerCategory.GAME_STUDIOS,
            "Government Partners" to TatumTechPartnerCategory.GOVERNMENT,
            "Technology Partners" to TatumTechPartnerCategory.TECHNOLOGY
        )
    }
}
