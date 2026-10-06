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

import com.tatumgames.tatumtech.framework.android.http.analytics.AnalyticsClient
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpErrorEvent
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpErrorType
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpRequestEvent
import com.tatumgames.tatumtech.framework.android.http.analytics.HttpResponseEvent
import com.tatumgames.tatumtech.framework.android.http.config.CommonClientConfiguration
import com.tatumgames.tatumtech.framework.android.http.executor.HttpInterceptor
import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequest
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.executor.HttpResponse
import com.tatumgames.tatumtech.framework.android.http.logging.HttpTrafficLogger
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ApiResponse
import com.tatumgames.tatumtech.framework.android.http.response.EmptyStateInfo
import com.tatumgames.tatumtech.framework.android.http.response.ErrorItem
import com.tatumgames.tatumtech.framework.android.http.response.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.concurrent.Executors

class BaseApiClientTest {

    private data class Item(val id: String, val name: String)

    private data class Envelope<T>(val data: T)

    private class FakeExecutor(
        private val handler: (HttpRequest) -> HttpResponse
    ) : HttpRequestExecutor {
        val requests = mutableListOf<HttpRequest>()

        override suspend fun execute(request: HttpRequest): HttpResponse {
            requests += request
            return handler(request)
        }
    }

    private class RecordingAnalytics : AnalyticsClient {
        val requests = mutableListOf<HttpRequestEvent>()
        val responses = mutableListOf<HttpResponseEvent>()
        val errors = mutableListOf<HttpErrorEvent>()

        override fun logHttpRequest(event: HttpRequestEvent) {
            requests += event
        }

        override fun logHttpResponse(event: HttpResponseEvent) {
            responses += event
        }

        override fun logHttpError(event: HttpErrorEvent) {
            errors += event
        }
    }

    private class TestClient(
        configuration: CommonClientConfiguration,
        executor: HttpRequestExecutor,
        analytics: AnalyticsClient? = null,
        interceptors: List<HttpInterceptor> = emptyList(),
        workDispatcher: CoroutineDispatcher = Dispatchers.Default,
        trafficLogger: HttpTrafficLogger? = null
    ) : BaseApiClient<CommonClientConfiguration>(
        configuration = configuration,
        executor = executor,
        analyticsClient = analytics,
        interceptors = interceptors,
        workDispatcher = workDispatcher,
        trafficLogger = trafficLogger
    ) {
        suspend fun item(id: String) = get<Item>("items/${encodePathSegment(id)}")

        suspend fun items(category: String?) =
            get<List<Item>>("items", queryParameters = mapOf("category" to category, "limit" to "10"))

        suspend fun wrappedItems() = get<Envelope<List<Item>>>("wrapped")

        suspend fun create(item: Item) = post<Item>("items", body = item, authenticated = true)

        suspend fun signOut() = post<EmptyStateInfo>("signout", authenticated = true)

        suspend fun ping() = get<Unit>("ping")

        suspend fun withHeader(value: String) =
            get<Unit>("ping", headers = mapOf(HEADER_AUTHORIZATION to value))
    }

    private val baseConfig = CommonClientConfiguration.Builder()
        .setBaseUrl("https://api.example.com/")
        .setApiKey("key-1")
        .setJwtAccessToken("token-1")
        .build()

    private fun json(code: Int, body: String?, url: String = "https://api.example.com/x") =
        HttpResponse(statusCode = code, url = url, body = body)

    // region success

    @Test
    fun success_deserializesObject() = runTest {
        val executor = FakeExecutor { json(200, """{"id":"1","name":"One"}""") }

        val response = TestClient(baseConfig, executor).item("1")

        assertTrue(response is ApiResponse.Success)
        response as ApiResponse.Success
        assertEquals(Item("1", "One"), response.data)
        assertEquals(HttpStatusCode.OK, response.statusCode)
    }

    @Test
    fun success_deserializesGenericCollectionsAndEnvelopes() = runTest {
        val list = FakeExecutor { json(200, """[{"id":"1","name":"A"},{"id":"2","name":"B"}]""") }
        val wrapped = FakeExecutor { json(200, """{"data":[{"id":"3","name":"C"}]}""") }

        assertEquals(
            listOf(Item("1", "A"), Item("2", "B")),
            TestClient(baseConfig, list).items(null).getOrNull()
        )
        assertEquals(
            listOf(Item("3", "C")),
            TestClient(baseConfig, wrapped).wrappedItems().getOrNull()?.data
        )
    }

    @Test
    fun anySuccessStatus_isSuccess() = runTest {
        val executor = FakeExecutor { json(201, """{"id":"9","name":"New"}""") }

        val response = TestClient(baseConfig, executor).create(Item("9", "New"))

        assertEquals(HttpStatusCode.CREATED, (response as ApiResponse.Success).statusCode)
    }

    @Test
    fun emptyStateInfo_ignoresBody() = runTest {
        val noContent = FakeExecutor { json(204, null) }
        val emptyData = FakeExecutor { json(200, """{"status":{"statusCode":200},"data":{}}""") }

        assertEquals(
            EmptyStateInfo(HttpStatusCode.NO_CONTENT),
            TestClient(baseConfig, noContent).signOut().getOrNull()
        )
        assertEquals(
            EmptyStateInfo(HttpStatusCode.OK),
            TestClient(baseConfig, emptyData).signOut().getOrNull()
        )
    }

    @Test
    fun unitResponse_ignoresBody() = runTest {
        val executor = FakeExecutor { json(200, "") }

        assertEquals(Unit, TestClient(baseConfig, executor).ping().getOrNull())
    }

    @Test
    fun success_capturesMetadata() = runTest {
        val executor = FakeExecutor { json(200, """{"id":"1","name":"One"}""", url = "https://api.example.com/items/1") }

        val metadata = (TestClient(baseConfig, executor).item("1") as ApiResponse.Success).metadata

        assertEquals(HttpMethod.GET, metadata.method)
        assertEquals("https://api.example.com/items/1", metadata.url)
        assertEquals("items/1", metadata.path)
        assertTrue(metadata.durationMs >= 0)
    }

    // endregion

    // region failures

    @Test
    fun non2xx_isHttpErrorWithParsedItems() = runTest {
        val executor = FakeExecutor {
            json(422, """{"errors":[{"code":"E1","message":"Name required","field":"name"}]}""")
        }

        val error = TestClient(baseConfig, executor).item("1").errorOrNull()

        assertTrue(error is ApiError.Http)
        error as ApiError.Http
        assertEquals(HttpStatusCode.UNPROCESSABLE_ENTITY, error.statusCode)
        assertEquals(listOf(ErrorItem("E1", "Name required", "name")), error.errors)
        assertEquals("Name required", error.message)
        assertTrue(error.rawBody!!.contains("E1"))
    }

    @Test
    fun non2xxWithoutBody_usesStatusAsMessage() = runTest {
        val executor = FakeExecutor { json(503, null) }

        val error = TestClient(baseConfig, executor).item("1").errorOrNull() as ApiError.Http

        assertTrue(error.errors.isEmpty())
        assertEquals("503 Service Unavailable", error.message)
    }

    @Test
    fun unknownStatus_isStillRepresented() = runTest {
        val executor = FakeExecutor { json(599, null) }

        val error = TestClient(baseConfig, executor).item("1").errorOrNull() as ApiError.Http

        assertEquals(599, error.statusCode.code)
        assertFalse(error.statusCode.isKnown)
    }

    @Test
    fun ioException_isNetworkError() = runTest {
        val executor = FakeExecutor { throw IOException("timeout") }

        val error = TestClient(baseConfig, executor).item("1").errorOrNull()

        assertTrue(error is ApiError.Network)
        assertEquals("timeout", error!!.message)
        assertEquals("https://api.example.com/items/1", error.metadata?.url)
    }

    @Test
    fun malformedBody_isSerializationError() = runTest {
        val executor = FakeExecutor { json(200, """{"id":"1",""") }

        val error = TestClient(baseConfig, executor).item("1").errorOrNull()

        assertTrue(error is ApiError.Serialization)
        assertEquals(HttpStatusCode.OK, (error as ApiError.Serialization).statusCode)
    }

    @Test
    fun emptyBodyForTypedResponse_isSerializationError() = runTest {
        val executor = FakeExecutor { json(200, "  ") }

        assertTrue(TestClient(baseConfig, executor).item("1").errorOrNull() is ApiError.Serialization)
    }

    @Test
    fun nullJson_isSerializationError() = runTest {
        val executor = FakeExecutor { json(200, "null") }

        assertTrue(TestClient(baseConfig, executor).item("1").errorOrNull() is ApiError.Serialization)
    }

    @Test
    fun unexpectedExecutorException_isUnexpectedError() = runTest {
        val executor = FakeExecutor { throw IllegalArgumentException("boom") }

        assertTrue(TestClient(baseConfig, executor).item("1").errorOrNull() is ApiError.Unexpected)
    }

    @Test
    fun missingBaseUrl_failsWithoutExecuting() = runTest {
        val executor = FakeExecutor { json(200, "{}") }
        val config = CommonClientConfiguration.Builder().build()

        val error = TestClient(config, executor).item("1").errorOrNull()

        assertTrue(error is ApiError.Unexpected)
        assertNull(error!!.metadata)
        assertTrue(executor.requests.isEmpty())
    }

    @Test
    fun authenticatedCallWithoutToken_failsWithoutExecuting() = runTest {
        val executor = FakeExecutor { json(200, "{}") }
        val config = CommonClientConfiguration.Builder().setBaseUrl("https://api.example.com").build()

        val error = TestClient(config, executor).signOut().errorOrNull()

        assertTrue(error is ApiError.Unexpected)
        assertTrue(executor.requests.isEmpty())
    }

    // endregion

    // region request construction

    @Test
    fun request_resolvesUrlAndEncodesPathSegments() = runTest {
        val executor = FakeExecutor { json(200, """{"id":"a b","name":"x"}""") }

        TestClient(baseConfig, executor).item("a b/c")

        assertEquals("https://api.example.com/items/a%20b%2Fc", executor.requests.single().url)
    }

    @Test
    fun request_dropsNullQueryParameters() = runTest {
        val executor = FakeExecutor { json(200, "[]") }
        val client = TestClient(baseConfig, executor)

        client.items(null)
        client.items("Community")

        assertEquals(mapOf("limit" to "10"), executor.requests[0].queryParameters)
        assertEquals(mapOf("category" to "Community", "limit" to "10"), executor.requests[1].queryParameters)
    }

    @Test
    fun request_addsDefaultHeaders() = runTest {
        val executor = FakeExecutor { json(200, """{"id":"1","name":"One"}""") }

        TestClient(baseConfig, executor).item("1")

        val headers = executor.requests.single().headers
        assertEquals("application/json", headers["Accept"])
        assertEquals("key-1", headers["x-api-key"])
        assertNull(headers["Authorization"])
    }

    @Test
    fun authenticatedRequest_sendsBearerToken() = runTest {
        val executor = FakeExecutor { json(201, """{"id":"1","name":"One"}""") }

        TestClient(baseConfig, executor).create(Item("1", "One"))

        assertEquals("Bearer token-1", executor.requests.single().headers["Authorization"])
    }

    @Test
    fun perCallHeaders_overrideDefaults() = runTest {
        val executor = FakeExecutor { json(200, "") }

        TestClient(baseConfig, executor).withHeader("Bearer previous")

        assertEquals("Bearer previous", executor.requests.single().headers["Authorization"])
    }

    @Test
    fun postBody_isSerializedToJson() = runTest {
        val executor = FakeExecutor { json(201, """{"id":"1","name":"One"}""") }

        TestClient(baseConfig, executor).create(Item("1", "One"))

        val request = executor.requests.single()
        assertEquals(HttpMethod.POST, request.method)
        assertEquals("""{"id":"1","name":"One"}""", request.body)
    }

    @Test
    fun interceptors_runInOrderAfterDefaults() = runTest {
        val executor = FakeExecutor { json(200, """{"id":"1","name":"One"}""") }
        val first = HttpInterceptor { it.copy(headers = it.headers + ("X-Trace" to "1")) }
        val second = HttpInterceptor {
            it.copy(headers = it.headers + ("X-Trace" to it.headers["X-Trace"] + "2"))
        }

        TestClient(baseConfig, executor, interceptors = listOf(first, second)).item("1")

        val headers = executor.requests.single().headers
        assertEquals("12", headers["X-Trace"])
        assertEquals("key-1", headers["x-api-key"])
    }

    @Test
    fun updateConfiguration_appliesToNextRequest() = runTest {
        val executor = FakeExecutor { json(201, """{"id":"1","name":"One"}""") }
        val client = TestClient(baseConfig, executor)

        client.updateConfiguration(
            CommonClientConfiguration.Builder().from(baseConfig).setJwtAccessToken("token-2").build()
        )
        client.create(Item("1", "One"))

        assertEquals("Bearer token-2", executor.requests.single().headers["Authorization"])
        assertEquals("Bearer token-2", client.configuration.jwtAccessToken)
    }

    // endregion

    // region analytics

    @Test
    fun analytics_receivesRequestAndResponse() = runTest {
        val analytics = RecordingAnalytics()
        val executor = FakeExecutor { json(200, """{"id":"1","name":"One"}""") }

        TestClient(baseConfig, executor, analytics).item("1")

        assertEquals(listOf(HttpRequestEvent(HttpMethod.GET, "items/1")), analytics.requests)
        assertEquals(200, analytics.responses.single().statusCode)
        assertTrue(analytics.errors.isEmpty())
    }

    @Test
    fun analytics_classifiesErrors() = runTest {
        val analytics = RecordingAnalytics()

        TestClient(baseConfig, FakeExecutor { json(404, null) }, analytics).item("1")
        TestClient(baseConfig, FakeExecutor { throw IOException() }, analytics).item("1")
        TestClient(baseConfig, FakeExecutor { json(200, "{") }, analytics).item("1")

        assertEquals(
            listOf(HttpErrorType.HTTP, HttpErrorType.NETWORK, HttpErrorType.SERIALIZATION),
            analytics.errors.map { it.errorType }
        )
        assertEquals(listOf(404, null, 200), analytics.errors.map { it.statusCode })
        assertTrue(analytics.responses.isEmpty())
    }

    @Test
    fun analytics_neverSeesQueryString() = runTest {
        val analytics = RecordingAnalytics()

        TestClient(baseConfig, FakeExecutor { json(200, "[]") }, analytics).items("Community")

        assertEquals("items", analytics.requests.single().path)
    }

    @Test
    fun throwingAnalytics_doesNotAffectResult() = runTest {
        val analytics = object : AnalyticsClient {
            override fun logHttpRequest(event: HttpRequestEvent) = throw IllegalStateException()
            override fun logHttpResponse(event: HttpResponseEvent) = throw IllegalStateException()
        }
        val executor = FakeExecutor { json(200, """{"id":"1","name":"One"}""") }

        assertTrue(TestClient(baseConfig, executor, analytics).item("1").isSuccess)
    }

    // endregion

    // region threading

    @Test
    fun work_runsOnWorkDispatcher_andResultReturnsToCaller() = runTest {
        val workThreadName = "http-work"
        val workDispatcher = Executors.newSingleThreadExecutor { Thread(it, workThreadName) }
            .asCoroutineDispatcher()
        val threads = mutableListOf<String>()
        val analytics = object : AnalyticsClient {
            override fun logHttpResponse(event: HttpResponseEvent) {
                threads += "analytics:" + Thread.currentThread().name
            }
        }
        val executor = FakeExecutor {
            threads += "executor:" + Thread.currentThread().name
            json(200, """{"id":"1","name":"One"}""")
        }
        val callerThread = Thread.currentThread().name

        try {
            val response = TestClient(baseConfig, executor, analytics, workDispatcher = workDispatcher).item("1")

            assertTrue(response.isSuccess)
            assertEquals(listOf("executor:$workThreadName", "analytics:$workThreadName"), threads)
            assertEquals(callerThread, Thread.currentThread().name)
        } finally {
            workDispatcher.close()
        }
    }

    // endregion

    // region traffic logging

    private class RecordingTrafficLogger : HttpTrafficLogger {
        val events = mutableListOf<String>()

        override fun logRequest(request: HttpRequest) {
            events += "RQ ${request.method} ${request.url}"
        }

        override fun logResponse(request: HttpRequest, response: HttpResponse, durationMs: Long) {
            events += "RS ${response.statusCode}"
        }

        override fun logFailure(request: HttpRequest, error: Throwable, durationMs: Long) {
            events += "RS failed ${error.message}"
        }
    }

    private val debugConfig = CommonClientConfiguration.Builder().from(baseConfig).setDebugMode(true).build()

    @Test
    fun trafficLogger_seesRequestsResponsesAndFailures_inDebugMode() = runTest {
        val logger = RecordingTrafficLogger()

        TestClient(debugConfig, FakeExecutor { json(404, "{}") }, trafficLogger = logger).item("1")
        TestClient(debugConfig, FakeExecutor { throw IOException("offline") }, trafficLogger = logger).item("2")

        assertEquals(
            listOf(
                "RQ GET https://api.example.com/items/1",
                "RS 404",
                "RQ GET https://api.example.com/items/2",
                "RS failed offline"
            ),
            logger.events
        )
    }

    @Test
    fun trafficLogger_isSilentWhenDebugModeIsOff() = runTest {
        val logger = RecordingTrafficLogger()

        TestClient(baseConfig, FakeExecutor { json(200, "[]") }, trafficLogger = logger).items(null)

        assertTrue(logger.events.isEmpty())
    }

    @Test
    fun trafficLogger_failureNeverChangesTheResult() = runTest {
        val throwing = object : HttpTrafficLogger {
            override fun logRequest(request: HttpRequest) = throw IllegalStateException("log")
            override fun logResponse(request: HttpRequest, response: HttpResponse, durationMs: Long) =
                throw IllegalStateException("log")
            override fun logFailure(request: HttpRequest, error: Throwable, durationMs: Long) =
                throw IllegalStateException("log")
        }
        val executor = FakeExecutor { json(200, """{"id":"1","name":"One"}""") }

        val response = TestClient(debugConfig, executor, trafficLogger = throwing).item("1")

        assertEquals("One", response.getOrNull()?.name)
    }

    // endregion

    @Test
    fun map_transformsSuccessAndPreservesFailure() = runTest {
        val ok = TestClient(baseConfig, FakeExecutor { json(200, """{"id":"1","name":"One"}""") }).item("1")
        val failed = TestClient(baseConfig, FakeExecutor { json(500, null) }).item("1")

        assertEquals("One", ok.map { it.name }.getOrNull())
        assertTrue(failed.map { it.name }.errorOrNull() is ApiError.Http)
    }
}
