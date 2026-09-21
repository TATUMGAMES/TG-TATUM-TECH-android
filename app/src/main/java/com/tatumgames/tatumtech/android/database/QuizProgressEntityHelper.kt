/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package com.tatumgames.tatumtech.android.database

/**
 * Builds stable ids and normalized language for quiz progress and answer events.
 */
object QuizProgressEntityHelper {

    /**
     * Normalizes nullable language to the value stored in Room (empty string means “no language”).
     */
    fun normalizedLanguage(language: String?): String = language.orEmpty()

    /**
     * Primary key for [com.tatumgames.tatumtech.android.database.entity.QuizProgressEntity].
     */
    fun makeProgressId(quizRoute: String, language: String?, level: String): String =
        "$quizRoute|${normalizedLanguage(language)}|$level"
}
