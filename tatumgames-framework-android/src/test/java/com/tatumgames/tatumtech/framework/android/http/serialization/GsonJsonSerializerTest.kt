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

import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Test

class GsonJsonSerializerTest {

    private data class Item(val id: String, val count: Int)

    private val serializer = GsonJsonSerializer()

    @Test
    fun roundTripsObjects() {
        val json = serializer.toJson(Item("a", 2))

        assertEquals(Item("a", 2), serializer.fromJson<Item>(json, Item::class.java))
    }

    @Test
    fun readsParameterizedTypes() {
        val type = object : TypeToken<List<Item>>() {}.type

        val items = serializer.fromJson<List<Item>>("""[{"id":"a","count":1},{"id":"b","count":2}]""", type)

        assertEquals(listOf(Item("a", 1), Item("b", 2)), items)
    }

    @Test(expected = Exception::class)
    fun rejectsMalformedJson() {
        serializer.fromJson<Item>("""{"id":"a",""", Item::class.java)
    }

    @Test(expected = Exception::class)
    fun rejectsLenientSyntax() {
        serializer.fromJson<Item>("""{id:a,count:1}""", Item::class.java)
    }

    @Test(expected = Exception::class)
    fun rejectsTrailingContent() {
        serializer.fromJson<Item>("""{"id":"a","count":1} garbage""", Item::class.java)
    }
}
