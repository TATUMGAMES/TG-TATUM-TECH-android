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

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlResolverTest {

    private val base = "https://api.example.com"

    @Test
    fun joinsWithSingleSlash() {
        assertEquals("$base/items", UrlResolver.resolve(base, "items"))
        assertEquals("$base/items", UrlResolver.resolve(base, "/items"))
        assertEquals("$base/items", UrlResolver.resolve("$base/", "/items"))
    }

    @Test
    fun keepsNestedPaths() {
        assertEquals("$base/v1/items/7/children", UrlResolver.resolve("$base/v1", "items/7/children"))
    }

    @Test
    fun emptyPath_returnsBase() {
        assertEquals(base, UrlResolver.resolve(base, ""))
        assertEquals(base, UrlResolver.resolve(base, "/"))
    }

    @Test
    fun absolutePath_ignoresBase() {
        assertEquals("https://other.example.com/x", UrlResolver.resolve(base, "https://other.example.com/x"))
        assertEquals("http://other.example.com/x", UrlResolver.resolve(null, "http://other.example.com/x"))
    }

    @Test(expected = IllegalStateException::class)
    fun relativePathWithoutBase_fails() {
        UrlResolver.resolve(null, "items")
    }

    @Test
    fun encodePathSegment_escapesReservedCharacters() {
        assertEquals("a%20b%2Fc%3Fd", UrlResolver.encodePathSegment("a b/c?d"))
        assertEquals("simple-id_1", UrlResolver.encodePathSegment("simple-id_1"))
    }
}
