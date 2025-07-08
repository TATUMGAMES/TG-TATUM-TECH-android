package com.tatumgames.tatumtech.android.database.repository

import com.tatumgames.tatumtech.android.database.dao.EventRegistrationDao
import com.tatumgames.tatumtech.android.database.entity.EventRegistrationEntity
import com.tatumgames.tatumtech.android.database.interfaces.EventRegistrationRepository

class EventRegistrationDatabaseRepository(
    private val dao: EventRegistrationDao
) : EventRegistrationRepository {

    override suspend fun getRegisteredEvents(): List<String> {
        return dao.getAll().map { it.eventId }
    }

    override suspend fun registerEvent(eventId: String) {
        dao.insert(EventRegistrationEntity(eventId))
    }

    override suspend fun unregisterEvent(eventId: String) {
        dao.delete(EventRegistrationEntity(eventId))
    }
} 
