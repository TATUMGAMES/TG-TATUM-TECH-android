/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package com.tatumgames.tatumtech.android.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_QUIZ_PROGRESS

/**
 * Persisted state for one quiz bucket: [quizRoute] + [language] + [level].
 *
 * @property id Stable primary key: [quizRoute], [language], and [level] joined (see [QuizProgressEntityHelper.makeProgressId]).
 * @property quizRoute Navigation route segment identifying the quiz domain (Coding vs AI/LLM vs LeetCode, etc.).
 * @property language Display or logical language; empty string when the quiz has no language dimension.
 * @property level Difficulty (Beginner, Intermediate, Advanced).
 * @property currentIndex Index into the ordered session question list.
 * @property answersJson JSON map of questionId → chosen answer text.
 * @property questionIdsJson JSON array of question IDs for this session in order.
 * @property isCompleted True after the user finishes the last question in the session.
 * @property lastUpdated Epoch millis when this row was last written.
 */
@Entity(tableName = TABLE_QUIZ_PROGRESS)
data class QuizProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    val quizRoute: String,
    val language: String,
    val level: String,
    val currentIndex: Int,
    val answersJson: String,
    val questionIdsJson: String,
    val isCompleted: Boolean,
    val lastUpdated: Long
)
