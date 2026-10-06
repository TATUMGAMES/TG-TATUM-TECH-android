package com.tatumgames.tatumtech.android.api.session

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tatumgames.tatumtech.android.api.models.TatumTechUser
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeystoreSessionStoreTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var store: KeystoreSessionStore

    private val session = TatumTechSession(
        accessToken = "access-secret",
        refreshToken = "refresh-secret",
        expiresAtMillis = 1_234_567L,
        authMethod = TatumTechAuthMethod.GOOGLE,
        user = TatumTechUser(id = "user-1", email = "ada@example.com")
    )

    @Before
    fun setUp() {
        store = KeystoreSessionStore(context)
        store.clear()
    }

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun savedSessionIsRestoredByANewInstance() {
        store.save(session)

        assertEquals(session, KeystoreSessionStore(context).load())
    }

    @Test
    fun tokensAreNotStoredInPlainText() {
        store.save(session)

        val raw = context.getSharedPreferences("tatum_tech_session", Context.MODE_PRIVATE).all.values.joinToString()
        assertFalse(raw.contains("access-secret"))
        assertFalse(raw.contains("refresh-secret"))
    }

    @Test
    fun clearRemovesTheSession() {
        store.save(session)
        store.clear()

        assertNull(store.load())
    }

    @Test
    fun corruptedSessionIsDiscarded() {
        context.getSharedPreferences("tatum_tech_session", Context.MODE_PRIVATE)
            .edit().putString("session", "not-encrypted").commit()

        assertNull(store.load())
        assertNull(store.load())
    }

    @Test
    fun deviceIdIsStable() {
        assertEquals(store.deviceId(), KeystoreSessionStore(context).deviceId())
    }
}
