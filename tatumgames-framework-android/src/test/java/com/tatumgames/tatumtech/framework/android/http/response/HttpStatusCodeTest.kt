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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpStatusCodeTest {

    @Test
    fun knownCodes_haveFriendlyNames() {
        assertEquals("OK", HttpStatusCode.fromCode(200).reasonPhrase)
        assertEquals("Not Found", HttpStatusCode.fromCode(404).reasonPhrase)
        assertEquals("Too Many Requests", HttpStatusCode.fromCode(429).reasonPhrase)
        assertEquals("Service Unavailable", HttpStatusCode.fromCode(503).reasonPhrase)
        assertTrue(HttpStatusCode.fromCode(401).isKnown)
    }

    @Test
    fun fromCode_returnsSharedConstantForKnownCodes() {
        assertSame(HttpStatusCode.UNAUTHORIZED, HttpStatusCode.fromCode(401))
    }

    @Test
    fun unknownCodes_remainRepresentable() {
        val status = HttpStatusCode.fromCode(599)

        assertEquals(599, status.code)
        assertEquals("Unknown", status.reasonPhrase)
        assertFalse(status.isKnown)
        assertTrue(status.isServerError)
    }

    @Test
    fun success_isExactly2xx() {
        assertTrue(HttpStatusCode.fromCode(200).isSuccess)
        assertTrue(HttpStatusCode.fromCode(204).isSuccess)
        assertTrue(HttpStatusCode.fromCode(299).isSuccess)
        assertFalse(HttpStatusCode.fromCode(199).isSuccess)
        assertFalse(HttpStatusCode.fromCode(300).isSuccess)
        assertFalse(HttpStatusCode.fromCode(404).isSuccess)
        assertFalse(HttpStatusCode.fromCode(500).isSuccess)
    }

    @Test
    fun errorRanges_areClassified() {
        assertTrue(HttpStatusCode.BAD_REQUEST.isClientError)
        assertFalse(HttpStatusCode.BAD_REQUEST.isServerError)
        assertTrue(HttpStatusCode.BAD_GATEWAY.isServerError)
        assertFalse(HttpStatusCode.OK.isClientError)
    }

    @Test
    fun equality_isByCode() {
        assertEquals(HttpStatusCode.fromCode(777), HttpStatusCode.fromCode(777))
        assertEquals(HttpStatusCode.fromCode(777).hashCode(), 777)
    }

    @Test
    fun toString_includesCodeAndName() {
        assertEquals("404 Not Found", HttpStatusCode.NOT_FOUND.toString())
    }
}
