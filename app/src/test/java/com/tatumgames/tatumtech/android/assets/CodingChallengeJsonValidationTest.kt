/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tatumgames.tatumtech.android.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class CodingChallengeJsonValidationTest(
    private val fileName: String
) {

    @Test
    fun pool_isValidAndHasAtLeast100UniqueQuestions() {
        val file = File("src/main/assets", fileName)
        assertTrue("Missing $fileName", file.exists())
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val rows: List<Map<String, Any>> = Gson().fromJson(file.readText(), type)
        assertTrue("$fileName must have >= 100 questions", rows.size >= 100)

        val ids = rows.map { it["id"] as String }
        assertEquals("$fileName duplicate IDs", ids.size, ids.toSet().size)

        val questions = rows.map { (it["question"] as String).trim().lowercase() }
        assertEquals("$fileName duplicate questions", questions.size, questions.toSet().size)

        rows.forEach { row ->
            val id = row["id"] as String

            @Suppress("UNCHECKED_CAST")
            val options = row["options"] as List<String>
            val correct = row["correctAnswer"] as String
            assertTrue("$id options empty", options.isNotEmpty())
            assertEquals("$id duplicate options", options.size, options.toSet().size)
            assertTrue("$id correctAnswer missing from options", correct in options)
            assertTrue("$id language blank", (row["language"] as String).isNotBlank())
            assertTrue("$id level blank", (row["level"] as String).isNotBlank())
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun files(): List<Array<String>> = listOf(
            "coding_challenges_kotlin_beginner.json",
            "coding_challenges_kotlin_intermediate.json",
            "coding_challenges_kotlin_advanced.json",
            "coding_challenges_javascript_beginner.json",
            "coding_challenges_javascript_intermediate.json",
            "coding_challenges_javascript_advanced.json",
            "coding_challenges_python_beginner.json",
            "coding_challenges_python_intermediate.json",
            "coding_challenges_python_advanced.json",
            "coding_challenges_java_beginner.json",
            "coding_challenges_java_intermediate.json",
            "coding_challenges_java_advanced.json",
            "coding_challenges_csharp_beginner.json",
            "coding_challenges_csharp_intermediate.json",
            "coding_challenges_csharp_advanced.json",
            "coding_challenges_ai_llm_beginner.json",
            "coding_challenges_ai_llm_intermediate.json",
            "coding_challenges_ai_llm_advanced.json",
            "coding_challenges_leet_beginner.json",
            "coding_challenges_leet_intermediate.json",
            "coding_challenges_leet_advanced.json",
            "coding_challenges_mock_interview_beginner.json",
            "coding_challenges_mock_interview_intermediate.json",
            "coding_challenges_mock_interview_advanced.json"
        ).map { arrayOf(it) }
    }
}
