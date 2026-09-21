/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package com.tatumgames.tatumtech.android.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_QUIZ_PROGRESS
import com.tatumgames.tatumtech.android.database.entity.QuizProgressEntity

/**
 * Persistence for in-progress and completed quiz sessions per `(quizRoute, language, level)` bucket.
 */
@Dao
interface QuizProgressDao {

    /** @param id Composite key from [com.tatumgames.tatumtech.android.database.QuizProgressEntityHelper.makeProgressId]. */
    @Query("SELECT * FROM $TABLE_QUIZ_PROGRESS WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): QuizProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: QuizProgressEntity): Long

    @Query("DELETE FROM $TABLE_QUIZ_PROGRESS WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM $TABLE_QUIZ_PROGRESS WHERE isCompleted = 1")
    suspend fun getAllCompleted(): List<QuizProgressEntity>
}
