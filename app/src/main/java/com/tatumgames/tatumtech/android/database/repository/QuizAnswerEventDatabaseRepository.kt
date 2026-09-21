/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package com.tatumgames.tatumtech.android.database.repository

import com.tatumgames.tatumtech.android.database.dao.QuizAnswerEventDao
import com.tatumgames.tatumtech.android.database.entity.QuizAnswerEventEntity

/**
 * Repository for [QuizAnswerEventEntity]: append-only answers used for daily limits and stats.
 */
class QuizAnswerEventDatabaseRepository(
    private val quizAnswerEventDao: QuizAnswerEventDao
) {
    /** Persists one answered question for the given bucket and timestamp. */
    suspend fun insert(event: QuizAnswerEventEntity): Long = quizAnswerEventDao.insert(event)

    /**
     * Counts how many answers were recorded today for exactly one
     * `(quizRoute, language, level)` bucket (local calendar day start in [startOfDayMillis]).
     */
    suspend fun countTodayForBucket(
        startOfDayMillis: Long,
        quizRoute: String,
        language: String,
        level: String
    ): Int = quizAnswerEventDao.countTodayForBucket(
        startOfDayMillis,
        quizRoute,
        language,
        level
    )

    /** All events, newest first (e.g. stats and streaks). */
    suspend fun getAllOrderByTimestampDesc(): List<QuizAnswerEventEntity> =
        quizAnswerEventDao.getAllOrderByTimestampDesc()
}
