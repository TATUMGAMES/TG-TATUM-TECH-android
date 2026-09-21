/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package com.tatumgames.tatumtech.android.database.repository

import com.tatumgames.tatumtech.android.database.dao.QuizProgressDao
import com.tatumgames.tatumtech.android.database.entity.QuizProgressEntity

/**
 * Repository for [QuizProgressEntity] (in-progress and completed session snapshots per bucket).
 */
class QuizProgressDatabaseRepository(
    private val quizProgressDao: QuizProgressDao
) {
    /** Loads progress by composite id, or null if none. */
    suspend fun getById(id: String): QuizProgressEntity? = quizProgressDao.getById(id)

    /** Inserts or replaces the row for this session. */
    suspend fun insertOrReplace(entity: QuizProgressEntity): Long =
        quizProgressDao.insertOrReplace(entity)

    /** Removes persisted progress for a bucket (e.g. after “try again”). */
    suspend fun deleteById(id: String) = quizProgressDao.deleteById(id)

    /** All completed quiz sessions (for idempotent timeline backfill). */
    suspend fun getAllCompleted(): List<QuizProgressEntity> = quizProgressDao.getAllCompleted()
}
