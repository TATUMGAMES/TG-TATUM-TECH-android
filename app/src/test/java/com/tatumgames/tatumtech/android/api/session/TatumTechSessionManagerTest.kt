package com.tatumgames.tatumtech.android.api.session

import com.google.gson.JsonParser
import com.tatumgames.tatumtech.android.api.TatumTechApiClient
import com.tatumgames.tatumtech.android.api.TatumTechClientConfiguration
import com.tatumgames.tatumtech.android.api.TatumTechEnvironment
import com.tatumgames.tatumtech.android.api.models.TatumTechUser
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequest
import com.tatumgames.tatumtech.framework.android.http.executor.HttpRequestExecutor
import com.tatumgames.tatumtech.framework.android.http.executor.HttpResponse
import com.tatumgames.tatumtech.framework.android.http.response.ApiError
import com.tatumgames.tatumtech.framework.android.http.response.ApiResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class TatumTechSessionManagerTest {

    private class InMemoryStore : TatumTechSessionStore {
        var stored: TatumTechSession? = null
        var failSaves = false

        override fun load() = stored
        override fun save(session: TatumTechSession) {
            if (failSaves) throw IOException("disk full")
            stored = session
        }
        override fun clear() {
            stored = null
        }
        override fun deviceId() = DEVICE_ID
    }

    /** Answers by endpoint path with queued responses; `null` body means a network failure. */
    private class ScriptedExecutor : HttpRequestExecutor {
        val requests = mutableListOf<HttpRequest>()
        private val scripts = mutableMapOf<String, ArrayDeque<Pair<Int, String?>>>()
        var latencyMs = 0L

        fun on(path: String, status: Int, body: String?) {
            scripts.getOrPut(path) { ArrayDeque() }.addLast(status to body)
        }

        fun requestsTo(path: String) = requests.filter { it.url.endsWith(path) }

        override suspend fun execute(request: HttpRequest): HttpResponse {
            requests += request
            if (latencyMs > 0) delay(latencyMs)
            val path = request.url.substringAfter("appspot.com/")
            val (status, body) = scripts[path]?.removeFirstOrNull() ?: error("Unscripted request to $path")
            body ?: throw IOException("offline")
            return HttpResponse(status, request.url, body = body)
        }
    }

    private var now = 1_000_000L
    private val store = InMemoryStore()
    private val executor = ScriptedExecutor()
    private val client = TatumTechApiClient(
        TatumTechClientConfiguration.Builder().setEnvironment(TatumTechEnvironment.STAGE).build(),
        executor = executor
    )
    private val manager = TatumTechSessionManager({ client }, store, clock = { now })

    private fun authBody(access: String, refresh: String? = "refresh-1", expiresIn: Long = 86_400) = """
        {"status":{"statusCode":200,"statusMessage":"OK"},"data":{
          "accessToken":"$access",
          ${refresh?.let { "\"refreshToken\":\"$it\"," } ?: ""}
          "expiresIn":$expiresIn,"tokenType":"Bearer",
          "user":{"id":"user-1","email":"ada@example.com"}}}
    """.trimIndent()

    private val ok = """{"status":{"statusCode":200,"statusMessage":"OK"},"data":{}}"""

    private fun error(status: Int, message: String) =
        """{"status":{"statusCode":$status,"statusMessage":"$message"}}"""

    private fun storedSession(expiresInMs: Long = 24 * 60 * 60 * 1_000L) = TatumTechSession(
        accessToken = "access-old",
        refreshToken = "refresh-old",
        expiresAtMillis = now + expiresInMs,
        authMethod = TatumTechAuthMethod.EMAIL,
        user = TatumTechUser(id = "user-1")
    )

    private fun signedInWith(session: TatumTechSession) {
        store.stored = session
        manager.restore()
    }

    // region Sign-in

    @Test
    fun `sign in stores the session and applies its tokens to the client`() = runBlocking {
        executor.on("tatum-tech/signin", 200, authBody("access-1"))

        val result = manager.signIn("ada@example.com", "pw")

        assertTrue(result is ApiResponse.Success)
        val session = store.stored!!
        assertEquals("access-1", session.accessToken)
        assertEquals("refresh-1", session.refreshToken)
        assertEquals(now + 86_400_000L, session.expiresAtMillis)
        assertEquals(TatumTechAuthMethod.EMAIL, session.authMethod)
        assertEquals("user-1", session.user?.id)
        assertTrue(manager.isSignedIn)
        with(client.configuration) {
            assertEquals("Bearer access-1", jwtAccessToken)
            assertEquals("refresh-1", refreshToken)
            assertEquals(now + 86_400_000L, tokenExpiration)
        }
        assertEquals(DEVICE_ID, json(executor.requests.single().body)["deviceId"].asString)
    }

    @Test
    fun `failed sign in stores nothing`() = runBlocking {
        executor.on("tatum-tech/signin", 401, error(401, "Invalid credentials"))

        val result = manager.signIn("ada@example.com", "bad")

        assertEquals("Invalid credentials", (result as ApiResponse.Failure).error.message)
        assertNull(store.stored)
        assertFalse(manager.isSignedIn)
        assertNull(client.configuration.jwtAccessToken)
    }

    @Test
    fun `auth response without access token is rejected`() = runBlocking {
        executor.on("tatum-tech/signin", 200, """{"data":{"refreshToken":"r"}}""")

        val result = manager.signIn("ada@example.com", "pw")

        assertTrue((result as ApiResponse.Failure).error is ApiError.Serialization)
        assertNull(store.stored)
    }

    @Test
    fun `session that cannot be saved is not used`() = runBlocking {
        store.failSaves = true
        executor.on("tatum-tech/signin", 200, authBody("access-1"))

        val result = manager.signIn("ada@example.com", "pw")

        assertTrue((result as ApiResponse.Failure).error is ApiError.Unexpected)
        assertFalse(manager.isSignedIn)
        assertNull(client.configuration.jwtAccessToken)
    }

    @Test
    fun `sign up and google sign in record their method`() = runBlocking {
        executor.on("tatum-tech/signup", 200, authBody("access-email"))
        executor.on("tatum-tech/signin", 200, authBody("access-google"))

        manager.signUp("ada@example.com", "pw", "pw")
        assertEquals(TatumTechAuthMethod.EMAIL, store.stored?.authMethod)

        manager.signInWithGoogle("google-id-token")
        assertEquals(TatumTechAuthMethod.GOOGLE, store.stored?.authMethod)
        assertEquals("access-google", store.stored?.accessToken)
        val googleBody = json(executor.requestsTo("tatum-tech/signin").single().body)
        assertEquals("google-id-token", googleBody["googleIdToken"].asString)
        assertEquals(DEVICE_ID, googleBody["deviceId"].asString)
    }

    @Test
    fun `restore applies the stored session to the client`() {
        signedInWith(storedSession())

        assertTrue(manager.isSignedIn)
        assertEquals("Bearer access-old", client.configuration.jwtAccessToken)
        assertEquals("refresh-old", client.configuration.refreshToken)
    }

    @Test
    fun `restore without a stored session leaves the client signed out`() {
        manager.restore()

        assertFalse(manager.isSignedIn)
        assertNull(client.configuration.jwtAccessToken)
    }

    // endregion

    // region Refresh

    @Test
    fun `no refresh while the token is far from expiry`() = runBlocking {
        signedInWith(storedSession())

        assertEquals(TatumTechSessionManager.RefreshResult.NotNeeded, manager.refreshIfNeeded())
        assertTrue(executor.requests.isEmpty())
    }

    @Test
    fun `no refresh without a session`() = runBlocking {
        assertEquals(TatumTechSessionManager.RefreshResult.NoSession, manager.refreshIfNeeded())
    }

    @Test
    fun `token near expiry is refreshed and keeps the refresh token if none is returned`() = runBlocking {
        signedInWith(storedSession(expiresInMs = TatumTechSessionManager.REFRESH_WINDOW_MS - 1))
        executor.on("tatum-tech/refreshToken", 200, authBody("access-new", refresh = null))

        assertEquals(TatumTechSessionManager.RefreshResult.Refreshed, manager.refreshIfNeeded())

        with(executor.requests.single()) {
            assertEquals("Bearer access-old", headers["Authorization"])
            assertEquals("refresh-old", json(body)["refreshToken"].asString)
            assertEquals(DEVICE_ID, json(body)["deviceId"].asString)
        }
        assertEquals("access-new", store.stored?.accessToken)
        assertEquals("refresh-old", store.stored?.refreshToken)
        assertEquals(now + 86_400_000L, store.stored?.expiresAtMillis)
        assertEquals("Bearer access-new", client.configuration.jwtAccessToken)
    }

    @Test
    fun `expired token is refreshed`() = runBlocking {
        signedInWith(storedSession(expiresInMs = -1))
        executor.on("tatum-tech/refreshToken", 200, authBody("access-new", refresh = "refresh-new"))

        assertEquals(TatumTechSessionManager.RefreshResult.Refreshed, manager.refreshIfNeeded())
        assertEquals("refresh-new", store.stored?.refreshToken)
    }

    @Test
    fun `rejected refresh token signs out`() = runBlocking {
        signedInWith(storedSession(expiresInMs = -1))
        executor.on("tatum-tech/refreshToken", 401, error(401, "Refresh token expired"))

        assertEquals(TatumTechSessionManager.RefreshResult.SignedOut, manager.refreshIfNeeded())
        assertNull(store.stored)
        assertFalse(manager.isSignedIn)
        assertNull(client.configuration.jwtAccessToken)
    }

    @Test
    fun `offline refresh keeps the session`() = runBlocking {
        signedInWith(storedSession(expiresInMs = -1))
        executor.on("tatum-tech/refreshToken", 0, null)

        val result = manager.refreshIfNeeded()

        assertTrue((result as TatumTechSessionManager.RefreshResult.Failed).error is ApiError.Network)
        assertEquals("access-old", store.stored?.accessToken)
        assertTrue(manager.isSignedIn)
    }

    @Test
    fun `concurrent callers share one refresh`() = runBlocking {
        signedInWith(storedSession(expiresInMs = -1))
        executor.latencyMs = 50
        executor.on("tatum-tech/refreshToken", 200, authBody("access-new"))

        List(5) { async { manager.refreshIfNeeded() } }.awaitAll()

        assertEquals(1, executor.requestsTo("tatum-tech/refreshToken").size)
        assertEquals("access-new", store.stored?.accessToken)
    }

    @Test
    fun `authenticated call refreshes and retries once after 401`() = runBlocking {
        signedInWith(storedSession())
        executor.on("tatum-tech/updateUserProfile", 401, error(401, "Token revoked"))
        executor.on("tatum-tech/refreshToken", 200, authBody("access-new"))
        executor.on("tatum-tech/updateUserProfile", 200, ok)

        val result = manager.authenticated { updateUserProfile("Ada", "Lovelace") }

        assertTrue(result is ApiResponse.Success)
        val profileCalls = executor.requestsTo("tatum-tech/updateUserProfile")
        assertEquals(listOf("Bearer access-old", "Bearer access-new"), profileCalls.map { it.headers["Authorization"] })
    }

    @Test
    fun `authenticated call does not retry when refresh fails`() = runBlocking {
        signedInWith(storedSession())
        executor.on("tatum-tech/updateUserProfile", 401, error(401, "Token revoked"))
        executor.on("tatum-tech/refreshToken", 401, error(401, "Refresh token expired"))

        val result = manager.authenticated { updateUserProfile("Ada", "Lovelace") }

        assertTrue(result is ApiResponse.Failure)
        assertEquals(1, executor.requestsTo("tatum-tech/updateUserProfile").size)
        assertFalse(manager.isSignedIn)
    }

    @Test
    fun `refresh rejected inside an http 200 envelope signs out`() = runBlocking {
        signedInWith(storedSession(expiresInMs = -1))
        executor.on("tatum-tech/refreshToken", 200, error(419, "REFRESH_TOKEN_DOES_NOT_EXIST"))

        assertEquals(TatumTechSessionManager.RefreshResult.SignedOut, manager.refreshIfNeeded())
        assertNull(store.stored)
        assertFalse(manager.isSignedIn)
    }

    @Test
    fun `authenticated call retries after a 401 reported inside an http 200 envelope`() = runBlocking {
        signedInWith(storedSession())
        executor.on("tatum-tech/updateUserProfile", 200, error(401, "UNAUTHORIZED"))
        executor.on("tatum-tech/refreshToken", 200, authBody("access-new"))
        executor.on("tatum-tech/updateUserProfile", 200, ok)

        val result = manager.authenticated { updateUserProfile("Ada", "Lovelace") }

        assertTrue(result is ApiResponse.Success)
        assertEquals(2, executor.requestsTo("tatum-tech/updateUserProfile").size)
    }

    // endregion

    // region Sign-out

    @Test
    fun `sign out calls the api with the bearer token and clears the session`() = runBlocking {
        signedInWith(storedSession())
        executor.on("tatum-tech/signout", 200, ok)

        manager.signOut()

        assertEquals("Bearer access-old", executor.requestsTo("tatum-tech/signout").single().headers["Authorization"])
        assertNull(store.stored)
        assertFalse(manager.isSignedIn)
        assertNull(client.configuration.jwtAccessToken)
        assertNull(client.configuration.refreshToken)
    }

    @Test
    fun `sign out clears the session even when the api fails`() = runBlocking {
        signedInWith(storedSession())
        executor.on("tatum-tech/signout", 500, error(500, "Server error"))

        manager.signOut()

        assertNull(store.stored)
        assertFalse(manager.isSignedIn)
    }

    @Test
    fun `sign out without a session sends nothing`() = runBlocking {
        manager.signOut()

        assertTrue(executor.requests.isEmpty())
    }

    @Test
    fun `confirmed sign out posts an empty body with the bearer token and clears the session`() = runBlocking {
        signedInWith(storedSession())
        executor.on("tatum-tech/signout", 200, ok)

        val failure = manager.signOutOrFail()

        assertNull(failure)
        with(executor.requestsTo("tatum-tech/signout").single()) {
            assertEquals("Bearer access-old", headers["Authorization"])
            assertEquals("{}", body)
        }
        assertNull(store.stored)
        assertFalse(manager.isSignedIn)
        assertNull(client.configuration.jwtAccessToken)
        assertNull(client.configuration.refreshToken)
    }

    @Test
    fun `failed sign out keeps the session and returns the error`() = runBlocking {
        signedInWith(storedSession())
        executor.on("tatum-tech/signout", 500, error(500, "Server error"))

        val failure = manager.signOutOrFail()

        assertEquals(500, (failure as ApiError.Http).statusCode.code)
        assertTrue(manager.isSignedIn)
        assertEquals("access-old", store.stored?.accessToken)
        assertEquals("Bearer access-old", client.configuration.jwtAccessToken)
    }

    @Test
    fun `sign out rejected inside an http 200 envelope keeps the session`() = runBlocking {
        signedInWith(storedSession())
        executor.on("tatum-tech/signout", 200, error(500, "Server error"))

        val failure = manager.signOutOrFail()

        assertTrue(failure is ApiError.Http)
        assertTrue(manager.isSignedIn)
    }

    @Test
    fun `offline sign out keeps the session`() = runBlocking {
        signedInWith(storedSession())
        executor.on("tatum-tech/signout", 0, null)

        val failure = manager.signOutOrFail()

        assertTrue(failure is ApiError.Network)
        assertTrue(manager.isSignedIn)
    }

    @Test
    fun `sign out of a session the server already rejected counts as signed out`() = runBlocking {
        signedInWith(storedSession())
        executor.on("tatum-tech/signout", 401, error(401, "Token revoked"))
        executor.on("tatum-tech/refreshToken", 401, error(401, "Refresh token expired"))

        val failure = manager.signOutOrFail()

        assertNull(failure)
        assertFalse(manager.isSignedIn)
        assertNull(store.stored)
    }

    @Test
    fun `confirmed sign out without a session sends nothing`() = runBlocking {
        assertNull(manager.signOutOrFail())

        assertTrue(executor.requests.isEmpty())
    }

    // endregion

    @Test
    fun `session toString hides tokens`() {
        val text = storedSession().toString()

        assertFalse(text.contains("access-old"))
        assertFalse(text.contains("refresh-old"))
    }

    private fun json(body: String?) = JsonParser.parseString(body).asJsonObject

    private companion object {
        const val DEVICE_ID = "device-123"
    }
}
