package com.tatumgames.tatumtech.android.api

import com.tatumgames.tatumtech.android.api.session.TatumTechAuthMethod
import com.tatumgames.tatumtech.android.api.session.TatumTechSession
import com.tatumgames.tatumtech.android.api.session.TatumTechSessionManager
import com.tatumgames.tatumtech.android.api.session.TatumTechSessionStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class TatumTechConfigurationAndProviderTest {

    @Before
    @After
    fun resetProvider() {
        TatumTechApiProvider.reset()
    }

    private fun configuration(environment: TatumTechEnvironment = TatumTechEnvironment.PRODUCTION) =
        TatumTechClientConfiguration.Builder().setEnvironment(environment).build()

    /** Same path as the public initializer, minus the Android asset source. */
    private fun initialize(configuration: TatumTechClientConfiguration) =
        TatumTechApiProvider.initialize(clientFactory = {
            TatumTechDataSources.createClient(configuration, localJsonSource = { error("unused") })
        })

    // region Configuration

    @Test
    fun `environments use the documented hosts`() {
        assertEquals("https://tg-api-new.uc.r.appspot.com", TatumTechEnvironment.PRODUCTION.baseUrl)
        assertEquals("https://tg-api-new-stage.uc.r.appspot.com", TatumTechEnvironment.STAGE.baseUrl)
    }

    @Test
    fun `environment sets base url`() {
        val config = configuration(TatumTechEnvironment.STAGE)

        assertEquals(TatumTechEnvironment.STAGE.baseUrl, config.baseUrl)
        assertEquals(TatumTechEnvironment.STAGE, config.environment)
    }

    @Test
    fun `custom base url has no environment`() {
        val config = TatumTechClientConfiguration.Builder()
            .setBaseUrl("https://localhost:8080/")
            .build()

        assertEquals("https://localhost:8080", config.baseUrl)
        assertNull(config.environment)
    }

    @Test
    fun `builder carries common settings`() {
        val config = TatumTechClientConfiguration.Builder()
            .setEnvironment(TatumTechEnvironment.PRODUCTION)
            .setJwtAccessToken("token")
            .setRefreshToken("refresh")
            .setHttpClientTimeout(5_000)
            .setDebugMode(true)
            .build()

        assertEquals("Bearer token", config.jwtAccessToken)
        assertEquals("refresh", config.refreshToken)
        assertEquals(5_000L, config.connectTimeout)
        assertTrue(config.debugMode)
        assertNull(config.apiKey)
    }

    @Test
    fun `from copies an existing configuration`() {
        val original = TatumTechClientConfiguration.Builder()
            .setEnvironment(TatumTechEnvironment.STAGE)
            .setJwtAccessToken("token")
            .build()

        val copy = TatumTechClientConfiguration.Builder().from(original).setJwtAccessToken(null).build()

        assertEquals(TatumTechEnvironment.STAGE, copy.environment)
        assertNull(copy.jwtAccessToken)
    }

    @Test
    fun `data source defaults to network independent of debug mode`() {
        val debug = TatumTechClientConfiguration.Builder().setDebugMode(true).build()

        assertEquals(TatumTechDataSourceMode.NETWORK, debug.dataSourceMode)
    }

    @Test
    fun `local json mode can be chosen without debug mode and is copied by from`() {
        val local = TatumTechClientConfiguration.Builder()
            .setDebugMode(false)
            .setDataSourceMode(TatumTechDataSourceMode.LOCAL_JSON)
            .build()

        assertFalse(local.debugMode)
        assertEquals(TatumTechDataSourceMode.LOCAL_JSON, local.dataSourceMode)
        assertEquals(
            TatumTechDataSourceMode.LOCAL_JSON,
            TatumTechClientConfiguration.Builder().from(local).build().dataSourceMode
        )
    }

    @Test
    fun `data source mode parses build config values`() {
        assertEquals(TatumTechDataSourceMode.LOCAL_JSON, TatumTechDataSourceMode.fromName("LOCAL_JSON"))
        assertEquals(TatumTechDataSourceMode.LOCAL_JSON, TatumTechDataSourceMode.fromName(" local_json "))
        assertEquals(TatumTechDataSourceMode.NETWORK, TatumTechDataSourceMode.fromName("NETWORK"))
        assertEquals(TatumTechDataSourceMode.NETWORK, TatumTechDataSourceMode.fromName(null))
        assertEquals(TatumTechDataSourceMode.NETWORK, TatumTechDataSourceMode.fromName("bogus"))
    }

    // endregion

    // region Provider

    @Test
    fun `getInstance before initialize fails with clear message`() {
        assertFalse(TatumTechApiProvider.isInitialized)
        try {
            TatumTechApiProvider.getInstance()
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("initialize()"))
        }
    }

    @Test
    fun `initialize creates a single shared client`() {
        val client = initialize(configuration())

        assertTrue(TatumTechApiProvider.isInitialized)
        assertSame(client, TatumTechApiProvider.getInstance())
        assertSame(client, TatumTechApiProvider.getInstance())
        assertEquals(TatumTechEnvironment.PRODUCTION, client.configuration.environment)
    }

    @Test
    fun `initialize again returns the existing client`() {
        val first = initialize(configuration(TatumTechEnvironment.PRODUCTION))
        val second = initialize(configuration(TatumTechEnvironment.STAGE))

        assertSame(first, second)
        assertEquals(TatumTechEnvironment.PRODUCTION, second.configuration.environment)
    }

    @Test
    fun `getSessionManager before initialize fails with clear message`() {
        try {
            TatumTechApiProvider.getSessionManager()
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("initialize()"))
        }
    }

    @Test
    fun `initialize restores the stored session into the shared client`() {
        val stored = TatumTechSession("access-1", "refresh-1", null, TatumTechAuthMethod.GOOGLE)
        val store = object : TatumTechSessionStore {
            override fun load() = stored
            override fun save(session: TatumTechSession) = Unit
            override fun clear() = Unit
            override fun deviceId() = "device"
        }

        val client = TatumTechApiProvider.initialize(
            clientFactory = { TatumTechApiClient(configuration()) },
            sessionFactory = { TatumTechSessionManager(TatumTechApiProvider::getInstance, store) }
        )

        assertTrue(TatumTechApiProvider.getSessionManager().isSignedIn)
        assertEquals("Bearer access-1", client.configuration.jwtAccessToken)
    }

    @Test
    fun `concurrent initialize creates exactly one client`() {
        var created = 0
        val threads = List(8) {
            Thread {
                TatumTechApiProvider.initialize(clientFactory = {
                    synchronized(this) { created++ }
                    TatumTechApiClient(configuration())
                })
            }
        }
        threads.forEach(Thread::start)
        threads.forEach(Thread::join)

        assertEquals(1, created)
    }

    // endregion
}
