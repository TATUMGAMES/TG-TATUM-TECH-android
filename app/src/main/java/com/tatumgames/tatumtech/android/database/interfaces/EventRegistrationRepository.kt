package com.tatumgames.tatumtech.android.database.interfaces

interface EventRegistrationRepository {
    suspend fun getRegisteredEvents(): List<String>
    suspend fun registerEvent(eventId: String)
    suspend fun unregisterEvent(eventId: String)
}
