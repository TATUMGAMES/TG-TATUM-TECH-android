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
package com.tatumgames.tatumtech.framework.android.http.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BearerTokenTest {

    @Test
    fun rawToken_getsPrefix() {
        assertEquals("Bearer abc", BearerToken.normalize("abc"))
    }

    @Test
    fun prefixedToken_isNotDoublePrefixed() {
        assertEquals("Bearer abc", BearerToken.normalize("Bearer abc"))
    }

    @Test
    fun prefixIsCaseInsensitive() {
        assertEquals("Bearer abc", BearerToken.normalize("bearer abc"))
        assertEquals("Bearer abc", BearerToken.normalize("BEARER abc"))
    }

    @Test
    fun surroundingAndInnerWhitespaceIsTrimmed() {
        assertEquals("Bearer abc", BearerToken.normalize("  Bearer    abc  "))
    }

    @Test
    fun blankOrPrefixOnly_isNull() {
        assertNull(BearerToken.normalize(null))
        assertNull(BearerToken.normalize(""))
        assertNull(BearerToken.normalize("   "))
        assertNull(BearerToken.normalize("Bearer"))
        assertNull(BearerToken.normalize("Bearer   "))
    }

    @Test
    fun strip_returnsBareToken() {
        assertEquals("abc", BearerToken.strip("Bearer abc"))
        assertEquals("abc", BearerToken.strip("abc"))
        assertNull(BearerToken.strip("Bearer "))
    }

    @Test
    fun tokenStartingWithBearerLetters_isNotTruncated() {
        assertEquals("Bearer BearerXYZ", BearerToken.normalize("BearerXYZ"))
    }

    @Test
    fun normalize_isIdempotent() {
        val once = BearerToken.normalize("token")
        assertEquals(once, BearerToken.normalize(once))
    }
}
