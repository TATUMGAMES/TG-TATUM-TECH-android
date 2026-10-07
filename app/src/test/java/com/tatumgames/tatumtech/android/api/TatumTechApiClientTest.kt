package com.tatumgames.tatumtech.android.api

import com.google.gson.JsonParser
import com.tatumgames.tatumtech.android.api.models.TatumTechPartnerCategory
import com.tatumgames.tatumtech.framework.android.http.executor.HttpMethod
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequest
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.executor.HttpResponse
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ApiResponse
import com.tatumgames.tatumtech.framework.android.http.response.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class TatumTechApiClientTest {

    private class FakeExecutor(
        var statusCode: Int = 200,
        var body: String? = """{"status":{"statusCode":200,"statusMessage":"OK"},"data":{}}""",
        var failure: IOException? = null
    ) : HttpRequestExecutor {
        val requests = mutableListOf<HttpRequest>()

        override suspend fun execute(request: HttpRequest): HttpResponse {
            requests += request
            failure?.let { throw it }
            return HttpResponse(statusCode, request.url, body = body)
        }

        val last: HttpRequest get() = requests.last()
    }

    private val executor = FakeExecutor()

    private fun client(accessToken: String? = null) = TatumTechApiClient(
        TatumTechClientConfiguration.Builder()
            .setEnvironment(TatumTechEnvironment.STAGE)
            .setJwtAccessToken(accessToken)
            .build(),
        executor = executor
    )

    private fun url(path: String) = "${TatumTechEnvironment.STAGE.baseUrl}/$path"

    private fun json(body: String?) = JsonParser.parseString(body).asJsonObject

    private fun <T> ApiResponse<T>.success(): T {
        assertTrue("Expected success but was $this", this is ApiResponse.Success)
        return (this as ApiResponse.Success).data
    }

    private fun ApiResponse<*>.error(): ApiError {
        assertTrue("Expected failure but was $this", this is ApiResponse.Failure)
        return (this as ApiResponse.Failure).error
    }

    // region Request paths, methods, bodies

    @Test
    fun `email sign in posts credentials without authorization`() = runBlocking {
        client(accessToken = "abc").signIn("a@b.co", "pw", "device-1")

        with(executor.last) {
            assertEquals(HttpMethod.POST, method)
            assertEquals(url("tatum-tech/signin"), url)
            assertNull(headers["Authorization"])
            val body = json(body)
            assertEquals("a@b.co", body["email"].asString)
            assertEquals("pw", body["password"].asString)
            assertEquals("device-1", body["deviceId"].asString)
            assertEquals(3, body.size())
        }
    }

    @Test
    fun `google sign in posts id token to the sign in endpoint`() = runBlocking {
        client().signInWithGoogle("google-token", "device-1")

        with(executor.last) {
            assertEquals(url("tatum-tech/signin"), url)
            val body = json(body)
            assertEquals("google-token", body["googleIdToken"].asString)
            assertEquals("device-1", body["deviceId"].asString)
            assertFalse(body.has("email"))
        }
    }

    @Test
    fun `sign up posts all fields`() = runBlocking {
        client().signUp("a@b.co", "pw", "pw2", "device-1")

        with(executor.last) {
            assertEquals(HttpMethod.POST, method)
            assertEquals(url("tatum-tech/signup"), url)
            assertEquals("application/json", headers["Accept"])
            assertTrue(contentType.startsWith("application/json"))
            val body = json(body)
            assertEquals("a@b.co", body["email"].asString)
            assertEquals("pw", body["password"].asString)
            assertEquals("pw2", body["confirmPassword"].asString)
            assertEquals("device-1", body["deviceId"].asString)
            assertEquals(4, body.size())
        }
    }

    @Test
    fun `refresh token sends previous access token as bearer when given`() = runBlocking {
        client().refreshToken("refresh-1", "device-1", previousAccessToken = "old-access")

        with(executor.last) {
            assertEquals(url("tatum-tech/refreshToken"), url)
            assertEquals("Bearer old-access", headers["Authorization"])
            val body = json(body)
            assertEquals("refresh-1", body["refreshToken"].asString)
            assertEquals("device-1", body["deviceId"].asString)
        }
    }

    @Test
    fun `refresh token omits authorization without previous token`() = runBlocking {
        client().refreshToken("refresh-1", "device-1")

        assertNull(executor.last.headers["Authorization"])
    }

    @Test
    fun `forgot password posts email and accepts empty data`() = runBlocking {
        val response = client().forgotPassword("a@b.co")

        assertTrue(response.isSuccess)
        assertEquals(url("tatum-tech/forgotPassword"), executor.last.url)
        assertEquals("a@b.co", json(executor.last.body)["email"].asString)
    }

    @Test
    fun `reset password omits optional fields when null`() = runBlocking {
        client().resetPassword("verify-1", "pw")

        with(executor.last) {
            assertEquals(url("tatum-tech/resetPassword"), url)
            val body = json(body)
            assertEquals("verify-1", body["verifyToken"].asString)
            assertEquals("pw", body["password"].asString)
            assertFalse(body.has("confirmPassword"))
            assertFalse(body.has("email"))
        }
    }

    @Test
    fun `reset password includes optional fields when given`() = runBlocking {
        client().resetPassword("verify-1", "pw", confirmPassword = "pw", email = "a@b.co")

        val body = json(executor.last.body)
        assertEquals("pw", body["confirmPassword"].asString)
        assertEquals("a@b.co", body["email"].asString)
    }

    @Test
    fun `sign out sends bearer token`() = runBlocking {
        client(accessToken = "access-1").signOut()

        with(executor.last) {
            assertEquals(HttpMethod.POST, method)
            assertEquals(url("tatum-tech/signout"), url)
            assertEquals("Bearer access-1", headers["Authorization"])
        }
    }

    @Test
    fun `sign out without token fails before sending`() = runBlocking {
        val error = client().signOut().error()

        assertTrue(error is ApiError.Unexpected)
        assertTrue(executor.requests.isEmpty())
    }

    @Test
    fun `update user profile sends bearer token and names`() = runBlocking {
        client(accessToken = "Bearer access-1").updateUserProfile("Ada", "Lovelace")

        with(executor.last) {
            assertEquals(url("tatum-tech/updateUserProfile"), url)
            assertEquals("Bearer access-1", headers["Authorization"])
            val body = json(body)
            assertEquals("Ada", body["firstName"].asString)
            assertEquals("Lovelace", body["lastName"].asString)
        }
    }

    @Test
    fun `read endpoints use GET without authorization`() = runBlocking {
        executor.body = """{"data":{"events":[],"event":{"id":"e"},"speakers":[],"partners":[],"partner":{"id":"p"}}}"""
        val client = client(accessToken = "access-1")

        client.getUpcomingEvents()
        client.getEventDetails("e1")
        client.getEventSpeakers("e1")
        client.getPartners()
        client.getPartnerDetails("p1")

        assertEquals(
            listOf(
                url("tatum-tech/upcomingEvents"),
                url("tatum-tech/events/e1"),
                url("tatum-tech/events/e1/speakers"),
                url("tatum-tech/partners"),
                url("tatum-tech/partners/p1")
            ),
            executor.requests.map { it.url }
        )
        executor.requests.forEach {
            assertEquals(HttpMethod.GET, it.method)
            assertNull(it.headers["Authorization"])
        }
    }

    @Test
    fun `path ids are encoded`() = runBlocking {
        executor.body = """{"data":{"event":{"id":"x"}}}"""

        client().getEventDetails("a/b c")

        assertEquals(url("tatum-tech/events/a%2Fb%20c"), executor.last.url)
    }

    @Test
    fun `partners category is sent as api value`() = runBlocking {
        executor.body = """{"data":{"partners":[]}}"""

        client().getPartners(TatumTechPartnerCategory.GAME_STUDIOS)

        assertEquals(mapOf("category" to "Game Studios"), executor.last.queryParameters)
    }

    @Test
    fun `partners without category sends no query`() = runBlocking {
        executor.body = """{"data":{"partners":[]}}"""

        client().getPartners()

        assertTrue(executor.last.queryParameters.isEmpty())
    }

    // endregion

    // region Response parsing

    @Test
    fun `sign in response is deserialized`() = runBlocking {
        executor.body = """
            {
              "status": {"statusCode": 200, "statusMessage": "OK"},
              "data": {
                "accessToken": "access-1",
                "refreshToken": "refresh-1",
                "expiresIn": 86400,
                "tokenType": "Bearer",
                "user": {
                  "id": "user-1",
                  "anonymousId": "anon-1",
                  "username": "ada",
                  "firstName": "Ada",
                  "lastName": "Lovelace",
                  "email": "ada@example.com",
                  "createdAt": "2026-01-01T00:00:00Z"
                }
              }
            }
        """.trimIndent()

        val session = client().signIn("ada@example.com", "pw", "device-1").success()

        assertEquals("access-1", session.accessToken)
        assertEquals("refresh-1", session.refreshToken)
        assertEquals(86_400L, session.expiresIn)
        assertEquals("Bearer", session.tokenType)
        with(session.user!!) {
            assertEquals("user-1", id)
            assertEquals("anon-1", anonymousId)
            assertEquals("ada", username)
            assertEquals("Ada", firstName)
            assertEquals("Lovelace", lastName)
            assertEquals("ada@example.com", email)
            assertEquals("2026-01-01T00:00:00Z", createdAt)
        }
    }

    @Test
    fun `upcoming events are deserialized with speakers and numeric ids`() = runBlocking {
        executor.body = """
            {"data":{"events":[{
              "id": 42, "name": "Meetup", "host": "Tatum Games", "date": "2026-11-01T18:00:00-05:00",
              "durationHours": 2, "location": "Chicago", "featuredImage": "https://img", "lumaUrl": "https://lu.ma/x",
              "virtualSpeakers": [{"id": "s1", "name": "Ada", "sortOrder": 1}]
            }]}}
        """.trimIndent()

        val events = client().getUpcomingEvents().success()

        assertEquals(1, events.size)
        with(events.single()) {
            assertEquals("42", id)
            assertEquals("Meetup", name)
            assertEquals(2, durationHours)
            assertEquals("https://lu.ma/x", lumaUrl)
            assertEquals("Ada", virtualSpeakers.single().name)
        }
    }

    @Test
    fun `speakers are deserialized with all fields`() = runBlocking {
        executor.body = """
            {"data":{"speakers":[{
              "id": "s1", "name": "Ada", "companyName": "Analytical", "profileImage": "https://img",
              "description": "Bio", "speakingTopic": "Engines", "speakingSchedule": "Day 1",
              "startTime": "18:00", "endTime": "18:30", "timeZone": "America/Chicago",
              "meetUrl": "https://meet", "sortOrder": 3
            }]}}
        """.trimIndent()

        val speaker = client().getEventSpeakers("e1").success().single()

        assertEquals("Analytical", speaker.companyName)
        assertEquals("Engines", speaker.speakingTopic)
        assertEquals("Day 1", speaker.speakingSchedule)
        assertEquals("18:00", speaker.startTime)
        assertEquals("18:30", speaker.endTime)
        assertEquals("America/Chicago", speaker.timeZone)
        assertEquals("https://meet", speaker.meetUrl)
        assertEquals(3, speaker.sortOrder)
    }

    @Test
    fun `partner details are deserialized and missing lists default to empty`() = runBlocking {
        executor.body = """
            {"data":{"partner":{"id":"p1","name":"Studio","category":"Game Studios",
              "socialLinks":{"discord":"https://discord"}}}}
        """.trimIndent()

        val partner = client().getPartnerDetails("p1").success()

        assertEquals("Studio", partner.name)
        assertEquals("Game Studios", partner.category)
        assertEquals("https://discord", partner.socialLinks?.discord)
        assertTrue(partner.contacts.isEmpty())
        assertTrue(partner.additionalLinks.isEmpty())
    }

    @Test
    fun `success keeps status code and metadata`() = runBlocking {
        executor.body = """{"data":{"events":[]}}"""

        val response = client().getUpcomingEvents() as ApiResponse.Success

        assertEquals(HttpStatusCode.OK, response.statusCode)
        assertEquals("tatum-tech/upcomingEvents", response.metadata.path)
    }

    // endregion

    // region Errors

    @Test
    fun `http error exposes status and server message`() = runBlocking {
        executor.statusCode = 401
        executor.body = """{"status":{"statusCode":401,"statusMessage":"Invalid credentials"}}"""

        val error = client().signIn("a@b.co", "bad", "device-1").error()

        assertTrue(error is ApiError.Http)
        assertEquals(HttpStatusCode.UNAUTHORIZED, (error as ApiError.Http).statusCode)
        assertEquals("Invalid credentials", error.message)
    }

    @Test
    fun `network failure is reported as network error`() = runBlocking {
        executor.failure = IOException("offline")

        val error = client().getUpcomingEvents().error()

        assertTrue(error is ApiError.Network)
    }

    @Test
    fun `missing data is reported as serialization error`() = runBlocking {
        executor.body = """{"status":{"statusCode":200,"statusMessage":"OK"}}"""

        val error = client().signIn("a@b.co", "pw", "device-1").error()

        assertTrue(error is ApiError.Serialization)
    }

    @Test
    fun `missing nested detail is reported as serialization error`() = runBlocking {
        executor.body = """{"data":{}}"""

        val error = client().getEventDetails("e1").error()

        assertTrue(error is ApiError.Serialization)
    }

    @Test
    fun `envelope error inside an http 200 becomes an http error with the api status`() = runBlocking {
        executor.body = """{"status":{"statusCode":400,"statusMessage":"USER_ALREADY_EXISTS"},"data":{}}"""

        val error = client().signUp("a@b.co", "pw123456", "pw123456", "device-1").error()

        assertTrue(error is ApiError.Http)
        with(error as ApiError.Http) {
            assertEquals(400, statusCode.code)
            assertEquals(200, responseStatusCode)
            assertEquals("USER_ALREADY_EXISTS", errors.single().code)
            assertEquals("tatum-tech/signup", metadata?.path)
        }
    }

    @Test
    fun `envelope errors are honored by endpoints without data`() = runBlocking {
        executor.body = """{"status":{"statusCode":401,"statusMessage":"UNAUTHORIZED"},"data":{}}"""
        val client = client(accessToken = "access-1")

        listOf(
            client.forgotPassword("a@b.co"),
            client.resetPassword("verify-1", "pw"),
            client.signOut(),
            client.updateUserProfile("Ada", "Lovelace")
        ).forEach { response ->
            val error = response.error()
            assertTrue("$error", error is ApiError.Http)
            assertEquals(401, (error as ApiError.Http).statusCode.code)
        }
    }

    @Test
    fun `envelope success statuses pass through`() = runBlocking {
        executor.body = """{"status":{"statusCode":201,"statusMessage":"CREATE_SUCCESS"},"data":{"accessToken":"a"}}"""

        val session = client().signUp("a@b.co", "pw123456", "pw123456", "device-1").success()

        assertEquals("a", session.accessToken)
    }

    @Test
    fun `html 404 page from a missing deployment is an http 404`() = runBlocking {
        executor.statusCode = 404
        executor.body = "<html><head><title>404 Page Not Found</title></head><body>Not found</body></html>"

        val error = client().signUp("a@b.co", "pw123456", "pw123456", "device-1").error()

        assertTrue(error is ApiError.Http)
        assertEquals(404, (error as ApiError.Http).statusCode.code)
    }

    @Test
    fun `malformed body is reported as serialization error`() = runBlocking {
        executor.body = "not json"

        val error = client().getPartners().error()

        assertTrue(error is ApiError.Serialization)
    }

    // endregion
}
