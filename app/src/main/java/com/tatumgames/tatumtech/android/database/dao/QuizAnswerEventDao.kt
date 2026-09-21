/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package com.tatumgames.tatumtech.android.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_QUIZ_ANSWER_EVENTS
import com.tatumgames.tatumtech.android.database.entity.QuizAnswerEventEntity

/**
 * Append-only answer log for daily limits (per quizRoute + language + level) and analytics.
 */
@Dao
interface QuizAnswerEventDao {

    @Insert
    suspend fun insert(event: QuizAnswerEventEntity): Long

    /**
     * Counts answers today for one bucket only (no cross-level or language-wide aggregation).
     */
    @Query(
        """
        SELECT COUNT(*) FROM $TABLE_QUIZ_ANSWER_EVENTS
        WHERE timestamp >= :startOfDayMillis
        AND quizRoute = :quizRoute
        AND language = :language
        AND level = :level
        """
    )
    suspend fun countTodayForBucket(
        startOfDayMillis: Long,
        quizRoute: String,
        language: String,
        level: String
    ): Int

    @Query("SELECT * FROM $TABLE_QUIZ_ANSWER_EVENTS ORDER BY timestamp DESC")
    suspend fun getAllOrderByTimestampDesc(): List<QuizAnswerEventEntity>
}
