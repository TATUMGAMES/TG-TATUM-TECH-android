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
package com.tatumgames.tatumtech.framework.android.http.serialization

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.StringReader
import java.lang.reflect.Type

/**
 * Converts request and response bodies. Swap the implementation to change JSON libraries
 * without touching API clients.
 */
interface JsonSerializer {

    fun toJson(value: Any): String

    /**
     * @param type Target type; may be parameterized (e.g. `List<Item>`).
     * @throws Exception when [json] is malformed or does not match [type].
     */
    fun <T> fromJson(json: String, type: Type): T?
}

/**
 * [JsonSerializer] backed by Gson. Thread-safe.
 */
class GsonJsonSerializer : JsonSerializer {

    private val gson: Gson

    constructor() {
        gson = Gson()
    }

    internal constructor(gson: Gson) {
        this.gson = gson
    }

    override fun toJson(value: Any): String = gson.toJson(value)

    @Suppress("UNCHECKED_CAST")
    override fun <T> fromJson(json: String, type: Type): T? {
        // Gson.fromJson always reads leniently; a strict reader rejects malformed bodies.
        val reader = JsonReader(StringReader(json)).apply { isLenient = false }
        val value = gson.getAdapter(TypeToken.get(type)).read(reader)
        if (reader.peek() != JsonToken.END_DOCUMENT) {
            throw JsonSyntaxException("Unexpected content after JSON document")
        }
        return value as T?
    }
}
