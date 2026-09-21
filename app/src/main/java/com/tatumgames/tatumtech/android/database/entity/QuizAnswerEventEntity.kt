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
import com.tatumgames.tatumtech.android.database.constants.DbConstants.TABLE_QUIZ_ANSWER_EVENTS

/**
 * One recorded answer for daily limit counting and stats.
 *
 * @property id Auto-generated row id.
 * @property quizRoute Quiz domain (matches [QuizProgressEntity.quizRoute]).
 * @property language Same convention as progress: empty string if none.
 * @property level Difficulty level.
 * @property questionId Question identifier.
 * @property answerChosen Option the user selected.
 * @property isCorrect Whether the answer matched the question’s correct answer.
 * @property timestamp Epoch millis when the answer was submitted.
 */
@Entity(tableName = TABLE_QUIZ_ANSWER_EVENTS)
data class QuizAnswerEventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    val quizRoute: String,
    val language: String,
    val level: String,
    val questionId: String,
    val answerChosen: String,
    val isCorrect: Boolean,
    val timestamp: Long
)
