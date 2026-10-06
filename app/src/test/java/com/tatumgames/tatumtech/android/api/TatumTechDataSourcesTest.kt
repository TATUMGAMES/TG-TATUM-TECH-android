package com.tatumgames.tatumtech.android.api

import com.tatumgames.tatumtech.android.api.local.LocalJsonAssetSource
import com.tatumgames.tatumtech.android.api.local.LocalJsonRequestExecutor
import com.tatumgames.tatumtech.android.api.models.TatumTechEvent
import com.tatumgames.tatumtech.android.api.models.TatumTechPartner
import com.tatumgames.tatumtech.android.api.models.TatumTechPartnerCategory
import com.tatumgames.tatumtech.android.api.models.TatumTechSpeaker
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequest
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.executor.HttpResponse
import com.tatumgames.tatumtech.framework.android.http.executor.OkHttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ApiResponse
import com.tatumgames.tatumtech.framework.android.http.response.HttpStatusCode
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException

class TatumTechDataSourcesTest {

    private val bundledAssets = LocalJsonAssetSource { File("src/main/assets/$it").readText(Charsets.UTF_8) }

    private fun configuration(mode: TatumTechDataSourceMode) = TatumTechClientConfiguration.Builder()
        .setEnvironment(TatumTechEnvironment.STAGE)
        .setDataSourceMode(mode)
        .build()

    private fun localClient(assets: LocalJsonAssetSource = bundledAssets) =
        TatumTechDataSources.createClient(configuration(TatumTechDataSourceMode.LOCAL_JSON), assets)

    private fun <T> ApiResponse<T>.success(): T {
        assertTrue("Expected success but was $this", this is ApiResponse.Success)
        return (this as ApiResponse.Success).data
    }

    private fun ApiResponse<*>.httpError(): ApiError.Http {
        assertTrue("Expected HTTP failure but was $this", (this as? ApiResponse.Failure)?.error is ApiError.Http)
        return (this as ApiResponse.Failure).error as ApiError.Http
    }

    // region Mode selection

    @Test
    fun `network mode selects the http executor`() {
        val executor = TatumTechDataSources.createExecutor(
            configuration(TatumTechDataSourceMode.NETWORK),
            localJsonSource = { error("network mode must not read local JSON") }
        )

        assertTrue(executor is OkHttpRequestExecutor)
    }

    @Test
    fun `local json mode selects the local executor`() {
        val executor = TatumTechDataSources.createExecutor(
            configuration(TatumTechDataSourceMode.LOCAL_JSON),
            bundledAssets
        )

        assertTrue(executor is LocalJsonRequestExecutor)
    }

    @Test
    fun `both modes produce the same client type`() {
        val network = TatumTechDataSources.createClient(configuration(TatumTechDataSourceMode.NETWORK), bundledAssets)
        val local = localClient()

        assertSame(TatumTechApiClient::class.java, network.javaClass)
        assertSame(network.javaClass, local.javaClass)
    }

    // endregion

    // region Same calls, same models in both modes

    /** Stand-in for a screen: written once against the client, unaware of the data source. */
    private suspend fun loadEventsAndPartners(client: TatumTechApiClient): Pair<List<TatumTechEvent>, List<TatumTechPartner>> {
        val events = client.getUpcomingEvents().success()
        val partners = client.getPartners(TatumTechPartnerCategory.GAME_STUDIOS).success()
        return events to partners
    }

    /** Serves the bundled data in the API's own envelope, as the live backend is expected to. */
    private class FakeNetworkExecutor(private val assets: LocalJsonAssetSource) : HttpRequestExecutor {
        val requests = mutableListOf<HttpRequest>()

        override suspend fun execute(request: HttpRequest): HttpResponse {
            requests += request
            val data = when {
                request.url.endsWith("tatum-tech/upcomingEvents") ->
                    """{"events":${assets.read(LocalJsonRequestExecutor.EVENTS_FILE)}}"""
                request.url.endsWith("tatum-tech/partners") -> {
                    val category = request.queryParameters["category"]
                    val partners = JsonParser.parseString(assets.read(LocalJsonRequestExecutor.PARTNERS_FILE)).asJsonArray
                    partners.forEach { p ->
                        val local = p.asJsonObject["category"].asString
                        LocalJsonRequestExecutor.LOCAL_PARTNER_CATEGORIES[local]
                            ?.let { p.asJsonObject.addProperty("category", it.apiValue) }
                    }
                    val filtered = partners.filter { category == null || it.asJsonObject["category"].asString == category }
                    """{"partners":${filtered}}"""
                }
                else -> error("Unexpected ${request.url}")
            }
            return HttpResponse(200, request.url, body = """{"status":{"statusCode":200},"data":$data}""")
        }
    }

    @Test
    fun `the same screen code yields equal models from network and local json`() = runBlocking {
        val network = FakeNetworkExecutor(bundledAssets)
        val networkClient = TatumTechApiClient(configuration(TatumTechDataSourceMode.NETWORK), executor = network)

        val fromNetwork = loadEventsAndPartners(networkClient)
        val fromLocal = loadEventsAndPartners(localClient())

        assertEquals(2, network.requests.size)
        assertTrue(fromLocal.first.isNotEmpty())
        assertTrue(fromLocal.second.isNotEmpty())
        assertEquals(fromNetwork, fromLocal)
    }

    // endregion

    // region Local JSON data flow

    @Test
    fun `upcoming events come from upcoming_events json`() = runBlocking {
        val events = localClient().getUpcomingEvents().success()

        val first = events.first()
        assertEquals("1", first.id)
        assertEquals("Tatum Tech Fall 2026", first.name)
        assertEquals("https://luma.com/14agrzue", first.lumaUrl)
        assertTrue(first.virtualSpeakers.isNotEmpty())
    }

    @Test
    fun `event details return the matching event`() = runBlocking {
        val event = localClient().getEventDetails("1").success()

        assertEquals("1", event.id)
        assertEquals("Tatum Games", event.host)
        assertEquals(5, event.durationHours)
    }

    @Test
    fun `event speakers return that event's speakers`() = runBlocking {
        val client = localClient()
        val event = client.getEventDetails("1").success()

        val speakers: List<TatumTechSpeaker> = client.getEventSpeakers("1").success()

        assertEquals(event.virtualSpeakers, speakers)
        with(speakers.first { it.id == "jeff-bogensberger" }) {
            assertEquals("Jeff Bogensberger", name)
            assertEquals("America/Los_Angeles", timeZone)
            assertEquals(1, sortOrder)
        }
    }

    @Test
    fun `partners use api category values`() = runBlocking {
        val partners = localClient().getPartners().success()

        val apiValues = TatumTechPartnerCategory.entries.map { it.apiValue }.toSet()
        assertTrue(partners.isNotEmpty())
        partners.forEach { assertTrue("${it.id} has ${it.category}", it.category in apiValues) }
    }

    @Test
    fun `partners category filter returns only that category`() = runBlocking {
        val client = localClient()
        val all = client.getPartners().success()

        val studios = client.getPartners(TatumTechPartnerCategory.GAME_STUDIOS).success()

        assertTrue(studios.isNotEmpty())
        assertTrue(studios.all { it.category == "Game Studios" })
        assertEquals(all.count { it.category == "Game Studios" }, studios.size)
    }

    @Test
    fun `partner details return the matching partner`() = runBlocking {
        val partner = localClient().getPartnerDetails("community_better_youth").success()

        assertEquals("Better Youth", partner.name)
        assertEquals("Community", partner.category)
        assertEquals("https://www.betteryouth.org/donate", partner.donationUrl)
        assertEquals("Syd Stewart", partner.contacts.single().name)
    }

    @Test
    fun `every partner category in partners json is mapped`() {
        val labels = JsonParser.parseString(bundledAssets.read(LocalJsonRequestExecutor.PARTNERS_FILE))
            .asJsonArray.map { it.asJsonObject["category"].asString }.toSet()

        assertEquals(emptySet<String>(), labels - LocalJsonRequestExecutor.LOCAL_PARTNER_CATEGORIES.keys)
    }

    // endregion

    // region Local JSON gaps are explicit failures

    @Test
    fun `unknown ids are not found`() = runBlocking {
        val client = localClient()

        assertEquals(HttpStatusCode.NOT_FOUND, client.getEventDetails("missing").httpError().statusCode)
        assertEquals(HttpStatusCode.NOT_FOUND, client.getEventSpeakers("missing").httpError().statusCode)
        assertEquals(HttpStatusCode.NOT_FOUND, client.getPartnerDetails("missing").httpError().statusCode)
    }

    @Test
    fun `endpoints without local data fail with not implemented`() = runBlocking {
        val client = localClient()

        val responses = listOf(
            client.signIn("a@b.co", "pw", "device"),
            client.signInWithGoogle("token", "device"),
            client.signUp("a@b.co", "pw", "pw", "device"),
            client.refreshToken("refresh", "device"),
            client.forgotPassword("a@b.co"),
            client.resetPassword("verify", "pw")
        )

        responses.forEach { response ->
            val error = response.httpError()
            assertEquals(HttpStatusCode.NOT_IMPLEMENTED, error.statusCode)
            assertTrue(error.message, error.message.contains("no local JSON data"))
        }
    }

    @Test
    fun `unreadable local json is a server error, not empty data`() = runBlocking {
        val response = localClient(assets = { throw IOException("missing asset") }).getUpcomingEvents()

        val error = response.httpError()
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR, error.statusCode)
        assertFalse(response.isSuccess)
        assertNotNull(error.message)
    }

    // endregion
}
