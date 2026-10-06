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
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultErrorParserTest {

    private val status = HttpStatusCode.BAD_REQUEST

    private fun parse(body: String?) = DefaultErrorParser.parse(body, status)

    @Test
    fun errorsArray_ofObjects() {
        val items = parse(
            """{"errors":[{"code":"E1","message":"Email is required","field":"email"},{"message":"Too short"}]}"""
        )

        assertEquals(
            listOf(
                ErrorItem(code = "E1", message = "Email is required", field = "email"),
                ErrorItem(message = "Too short")
            ),
            items
        )
    }

    @Test
    fun errorsArray_ofStrings() {
        assertEquals(listOf(ErrorItem(message = "Bad")), parse("""{"errors":["Bad"]}"""))
    }

    @Test
    fun errorObject() {
        assertEquals(
            listOf(ErrorItem(code = "AUTH", message = "Expired")),
            parse("""{"error":{"code":"AUTH","message":"Expired"}}""")
        )
    }

    @Test
    fun errorString() {
        assertEquals(listOf(ErrorItem(message = "Nope")), parse("""{"error":"Nope"}"""))
    }

    @Test
    fun rootMessage() {
        assertEquals(
            listOf(ErrorItem(code = "42", message = "Invalid")),
            parse("""{"message":"Invalid","code":42}""")
        )
    }

    @Test
    fun statusEnvelope() {
        assertEquals(
            listOf(ErrorItem(code = "401", message = "UNAUTHORIZED")),
            parse("""{"status":{"statusCode":401,"statusMessage":"UNAUTHORIZED"},"data":{}}""")
        )
    }

    @Test
    fun unparseableOrEmpty_returnsEmptyList() {
        assertTrue(parse(null).isEmpty())
        assertTrue(parse("").isEmpty())
        assertTrue(parse("<html>502</html>").isEmpty())
        assertTrue(parse("[1,2]").isEmpty())
        assertTrue(parse("""{"unrelated":true}""").isEmpty())
    }
}
