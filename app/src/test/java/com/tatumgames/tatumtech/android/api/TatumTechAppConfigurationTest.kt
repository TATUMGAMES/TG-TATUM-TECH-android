package com.tatumgames.tatumtech.android.api

import com.tatumgames.tatumtech.android.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TatumTechAppConfigurationTest {

    @Test
    fun `stage environment with local json and api key`() {
        val config = TatumTechAppConfiguration.create(
            environment = "STAGE",
            dataSource = "LOCAL_JSON",
            apiKey = "key-123",
            connectTimeoutMs = 5_000,
            debugMode = true
        )

        assertEquals(TatumTechEnvironment.STAGE, config.environment)
        assertEquals("https://tg-api-new-stage.uc.r.appspot.com", config.baseUrl)
        assertEquals(TatumTechDataSourceMode.LOCAL_JSON, config.dataSourceMode)
        assertEquals("key-123", config.apiKey)
        assertEquals(5_000L, config.connectTimeout)
        assertTrue(config.debugMode)
    }

    @Test
    fun `blank key and zero timeout mean unset`() {
        val config = TatumTechAppConfiguration.create(
            environment = "PRODUCTION",
            dataSource = "NETWORK",
            apiKey = " ",
            connectTimeoutMs = 0,
            debugMode = false
        )

        assertEquals("https://tg-api-new.uc.r.appspot.com", config.baseUrl)
        assertEquals(TatumTechDataSourceMode.NETWORK, config.dataSourceMode)
        assertNull(config.apiKey)
        assertNull(config.connectTimeout)
        assertNull(config.jwtAccessToken)
    }

    @Test
    fun `unknown environment falls back to production`() {
        assertEquals(TatumTechEnvironment.PRODUCTION, TatumTechEnvironment.fromName("bogus"))
        assertEquals(TatumTechEnvironment.STAGE, TatumTechEnvironment.fromName(" stage "))
    }

    @Test
    fun `build config defaults to the production network api`() {
        val config = TatumTechAppConfiguration.fromBuildConfig()

        assertEquals(TatumTechEnvironment.fromName(BuildConfig.TATUM_TECH_ENVIRONMENT), config.environment)
        assertEquals(TatumTechDataSourceMode.fromName(BuildConfig.TATUM_TECH_DATA_SOURCE), config.dataSourceMode)
        assertEquals(BuildConfig.DEBUG, config.debugMode)
    }
}
