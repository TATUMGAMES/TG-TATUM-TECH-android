package com.tatumgames.tatumtech.android.data.content

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tatumgames.tatumtech.android.api.TatumTechApiClient
import com.tatumgames.tatumtech.android.api.TatumTechClientConfiguration
import com.tatumgames.tatumtech.android.api.TatumTechDataSourceMode
import com.tatumgames.tatumtech.android.api.TatumTechDataSources
import com.tatumgames.tatumtech.android.api.TatumTechEnvironment
import com.tatumgames.tatumtech.android.api.local.LocalJsonAssetSource
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import com.tatumgames.tatumtech.android.ui.components.screens.partners.PartnerCategoryFilters
import com.tatumgames.tatumtech.android.ui.models.Partner
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequest
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.executor.HttpResponse
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ApiResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException

class TatumTechContentRepositoryTest {

    private val bundledAssets = LocalJsonAssetSource { File("src/main/assets/$it").readText(Charsets.UTF_8) }

    private fun configuration(mode: TatumTechDataSourceMode) = TatumTechClientConfiguration.Builder()
        .setEnvironment(TatumTechEnvironment.STAGE)
        .setDataSourceMode(mode)
        .build()

    private val localRepository = TatumTechDataSources
        .createClient(configuration(TatumTechDataSourceMode.LOCAL_JSON), bundledAssets)
        .let { client -> TatumTechContentRepository { client } }

    private fun networkRepository(executor: HttpRequestExecutor): TatumTechContentRepository {
        val client = TatumTechApiClient(configuration(TatumTechDataSourceMode.NETWORK), executor = executor)
        return TatumTechContentRepository { client }
    }

    /** How the screens read these files before they used the API client. */
    private inline fun <reified T> legacyParse(fileName: String): T =
        Gson().fromJson(bundledAssets.read(fileName), object : TypeToken<T>() {}.type)

    private fun <T> ApiResponse<T>.success(): T {
        assertTrue("Expected success but was $this", this is ApiResponse.Success)
        return (this as ApiResponse.Success).data
    }

    // region Local JSON mode reproduces the previous screen data exactly

    @Test
    fun `upcoming events match the bundled JSON the screen used to read`() = runBlocking {
        @Suppress("DEPRECATION")
        val legacy = legacyParse<List<Event>>("upcoming_events.json")
            .map { it.copy(isRegistrationOpen = true) }

        val events = localRepository.getUpcomingEvents().success()

        assertTrue(events.isNotEmpty())
        assertEquals(legacy, events)
    }

    @Test
    fun `event speakers match the previous in-order speaker list`() = runBlocking {
        val legacyEvent = legacyParse<List<Event>>("upcoming_events.json").first()

        val speakers = localRepository.getEventSpeakers(legacyEvent.id).success()

        assertTrue(speakers.isNotEmpty())
        assertEquals(legacyEvent.speakersInOrder(), speakers)
    }

    @Test
    fun `partners match the bundled JSON including category labels`() = runBlocking {
        val legacy = legacyParse<List<Partner>>("partners.json")

        val partners = localRepository.getPartners().success()

        assertTrue(partners.isNotEmpty())
        assertEquals(legacy, partners)
        assertTrue(partners.all { it.category in PartnerCategoryFilters.jsonCategories })
    }

    @Test
    fun `unknown event has no speakers to show`() = runBlocking {
        val response = localRepository.getEventSpeakers("missing")

        assertTrue(response is ApiResponse.Failure)
        assertTrue(response.getOrNull().orEmpty().isEmpty())
    }

    // endregion

    // region Network mode feeds the same screen models

    private class FakeApi(private val body: String?) : HttpRequestExecutor {
        override suspend fun execute(request: HttpRequest): HttpResponse =
            HttpResponse(200, request.url, body = body ?: throw IOException("offline"))
    }

    @Test
    fun `api partner categories become the labels the partners screen filters on`() = runBlocking {
        val repository = networkRepository(
            FakeApi("""{"data":{"partners":[{"id":"p1","name":"Studio","category":"Game Studios","featured":true}]}}""")
        )

        val partner = repository.getPartners().success().single()

        assertEquals("Game Studio Partners", partner.category)
        assertEquals("Game Studios", PartnerCategoryFilters.shortLabelForCategory(partner.category))
        assertEquals(listOf(partner), PartnerCategoryFilters.filter(listOf(partner), "Game Studios"))
        assertEquals(null, partner.contacts)
    }

    @Test
    fun `missing optional api fields fall back to screen defaults`() = runBlocking {
        val repository = networkRepository(FakeApi("""{"data":{"events":[{"id":"evt-1"}]}}"""))

        val event = repository.getUpcomingEvents().success().single()

        assertEquals("evt-1", event.id)
        assertEquals("", event.name)
        assertEquals(0, event.durationHours)
        assertTrue(event.virtualSpeakers.isEmpty())
        assertTrue(!event.registerEnabled)
    }

    @Test
    fun `speakers from the api are put in speaking order`() = runBlocking {
        val repository = networkRepository(
            FakeApi(
                """{"data":{"speakers":[
                  {"id":"b","name":"B","speakingTopic":"t","sortOrder":2},
                  {"id":"a","name":"A","speakingTopic":"t","sortOrder":1}]}}"""
            )
        )

        val speakers = repository.getEventSpeakers("evt-1").success()

        assertEquals(listOf("a", "b"), speakers.map { it.id })
    }

    @Test
    fun `network failure surfaces as a failure the screens show as empty`() = runBlocking {
        val response = networkRepository(FakeApi(null)).getUpcomingEvents()

        assertTrue((response as ApiResponse.Failure).error is ApiError.Network)
        assertTrue(response.getOrNull().orEmpty().isEmpty())
    }

    // endregion

    @Test
    fun `unknown api category is kept as is`() {
        assertEquals("Esports", PartnerCategoryFilters.categoryForApiValue("Esports"))
        assertEquals("Community Partners", PartnerCategoryFilters.categoryForApiValue("community"))
        assertEquals("", PartnerCategoryFilters.categoryForApiValue(null))
    }
}
