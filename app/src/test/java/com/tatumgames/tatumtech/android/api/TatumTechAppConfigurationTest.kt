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
    fun `environment names are parsed leniently and unknown names are rejected`() {
        assertEquals(TatumTechEnvironment.STAGE, TatumTechEnvironment.fromName(" stage "))
        assertEquals(TatumTechEnvironment.PRODUCTION, TatumTechEnvironment.fromName("PRODUCTION"))
        assertNull(TatumTechEnvironment.fromName("bogus"))
        assertNull(TatumTechEnvironment.fromName(null))
    }

    @Test
    fun `debug builds use stage unless production is requested explicitly`() {
        assertEquals(TatumTechEnvironment.STAGE, TatumTechAppConfiguration.resolveEnvironment("STAGE", debugBuild = true))
        assertEquals(TatumTechEnvironment.STAGE, TatumTechAppConfiguration.resolveEnvironment("", debugBuild = true))
        assertEquals(TatumTechEnvironment.STAGE, TatumTechAppConfiguration.resolveEnvironment("bogus", debugBuild = true))
        assertEquals(
            TatumTechEnvironment.PRODUCTION,
            TatumTechAppConfiguration.resolveEnvironment("PRODUCTION", debugBuild = true)
        )
    }

    @Test
    fun `release builds always use production`() {
        listOf("STAGE", "PRODUCTION", "", "bogus").forEach {
            assertEquals(TatumTechEnvironment.PRODUCTION, TatumTechAppConfiguration.resolveEnvironment(it, debugBuild = false))
        }
        val release = TatumTechAppConfiguration.create("STAGE", "NETWORK", "", 0, debugMode = false)
        assertEquals("https://tg-api-new.uc.r.appspot.com", release.baseUrl)
    }

    @Test
    fun `debug build config resolves to the stage api`() {
        val config = TatumTechAppConfiguration.fromBuildConfig()

        assertEquals(BuildConfig.DEBUG, config.debugMode)
        assertEquals(TatumTechDataSourceMode.fromName(BuildConfig.TATUM_TECH_DATA_SOURCE), config.dataSourceMode)
        if (BuildConfig.DEBUG && !BuildConfig.TATUM_TECH_ENVIRONMENT_OVERRIDDEN) {
            assertEquals(TatumTechEnvironment.STAGE, config.environment)
            assertEquals("https://tg-api-new-stage.uc.r.appspot.com", config.baseUrl)
        }
        if (!BuildConfig.DEBUG) assertEquals(TatumTechEnvironment.PRODUCTION, config.environment)
    }

    @Test
    fun `startup log names the environment and why it was chosen`() {
        val stage = TatumTechAppConfiguration.create("STAGE", "NETWORK", "", 0, debugMode = true)
        val description = TatumTechApiProvider.describeEnvironment(stage, overridden = false)

        assertTrue(description, description.contains("STAGE"))
        assertTrue(description, description.contains("https://tg-api-new-stage.uc.r.appspot.com"))
        assertTrue(description, description.contains("debug build default"))
        assertTrue(
            TatumTechApiProvider.describeEnvironment(stage, overridden = true).contains("tatumTech.environment")
        )
    }
}
