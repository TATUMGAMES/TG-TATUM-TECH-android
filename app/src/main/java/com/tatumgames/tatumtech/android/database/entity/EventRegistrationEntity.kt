package com.tatumgames.tatumtech.android.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_registrations")
data class EventRegistrationEntity(
    @PrimaryKey val eventId: String
) 