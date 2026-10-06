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
package com.tatumgames.tatumtech.framework.android.http.executor

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class OkHttpRequestExecutorTest {

    private lateinit var server: MockWebServer
    private val executor = OkHttpRequestExecutor(connectTimeoutMillis = 2_000L, readTimeoutMillis = 2_000L)

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun url(path: String) = server.url(path).toString()

    @Test
    fun get_sendsHeadersAndEncodedQuery() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok":true}"""))

        val response = executor.execute(
            HttpRequest(
                method = HttpMethod.GET,
                url = url("/partners"),
                headers = mapOf("Accept" to "application/json", "x-api-key" to "k"),
                queryParameters = mapOf("category" to "Game Studios")
            )
        )

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/partners?category=Game%20Studios", recorded.path)
        assertEquals("k", recorded.getHeader("x-api-key"))
        assertEquals(200, response.statusCode)
        assertEquals("""{"ok":true}""", response.body)
        assertTrue(response.url.endsWith("/partners?category=Game%20Studios"))
    }

    @Test
    fun post_sendsJsonBody() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody("{}"))

        executor.execute(HttpRequest(HttpMethod.POST, url("/items"), body = """{"a":1}"""))

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("""{"a":1}""", recorded.body.readUtf8())
        assertTrue(recorded.getHeader("Content-Type")!!.startsWith("application/json"))
    }

    @Test
    fun postWithoutBody_sendsEmptyBody() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))

        val response = executor.execute(HttpRequest(HttpMethod.POST, url("/signout")))

        assertEquals(0L, server.takeRequest().bodySize)
        assertEquals(204, response.statusCode)
    }

    @Test
    fun non2xx_isReturnedNotThrown() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setHeader("X-Reason", "gone").setBody("missing"))

        val response = executor.execute(HttpRequest(HttpMethod.GET, url("/x")))

        assertEquals(404, response.statusCode)
        assertEquals("missing", response.body)
        assertEquals("gone", response.headers["x-reason"])
    }

    @Test
    fun emptyBody_isEmptyString() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200))

        val response = executor.execute(HttpRequest(HttpMethod.DELETE, url("/x")))

        assertEquals("", response.body)
        assertEquals("DELETE", server.takeRequest().method)
    }

    @Test(expected = IOException::class)
    fun droppedConnection_throwsIOException() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        runBlocking { executor.execute(HttpRequest(HttpMethod.GET, url("/x"))) }
    }

    @Test(expected = IOException::class)
    fun slowResponse_timesOutAsIOException() {
        val fastTimeout = OkHttpRequestExecutor(connectTimeoutMillis = 500L, readTimeoutMillis = 200L)
        server.enqueue(MockResponse().setBody("late").setHeadersDelay(2, java.util.concurrent.TimeUnit.SECONDS))

        runBlocking { fastTimeout.execute(HttpRequest(HttpMethod.GET, url("/slow"))) }
    }

    @Test
    fun connectTimeout_usesConfiguredOrDefault() {
        assertEquals(7_000L, OkHttpRequestExecutor(connectTimeoutMillis = 7_000L).connectTimeoutMillis)
        assertEquals(
            OkHttpRequestExecutor.DEFAULT_CONNECT_TIMEOUT_MS,
            OkHttpRequestExecutor(connectTimeoutMillis = null).connectTimeoutMillis
        )
    }

    @Test
    fun getRequest_hasNoBody() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200))

        executor.execute(HttpRequest(HttpMethod.GET, url("/x")))

        assertNull(server.takeRequest().getHeader("Content-Type"))
    }
}
