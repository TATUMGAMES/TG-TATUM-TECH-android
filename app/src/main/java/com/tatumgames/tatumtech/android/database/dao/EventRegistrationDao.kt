package com.tatumgames.tatumtech.android.database.dao

import androidx.room.*
import com.tatumgames.tatumtech.android.database.entity.EventRegistrationEntity

@Dao
interface EventRegistrationDao {
    @Query("SELECT * FROM event_registrations")
    suspend fun getAll(): List<EventRegistrationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registration: EventRegistrationEntity)

    @Delete
    suspend fun delete(registration: EventRegistrationEntity)
} 