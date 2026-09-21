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
package com.tatumgames.tatumtech.android.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.CodingQuestionEntity
import com.tatumgames.tatumtech.android.database.repository.CodingQuestionDatabaseRepository
import com.tatumgames.tatumtech.android.ui.components.screens.coding.models.CodingChallenges
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility class for importing coding challenges from JSON assets into the database.
 * 
 * This class handles the initial loading of coding challenge questions from JSON files
 * stored in the assets directory and imports them into the local Room database.
 */
object CodingChallengesImporter {

    private const val TAG = "CodingChallengesImporter"

    private val CODING_QUESTION_ASSET_FILES = listOf(
        "coding_challenges_javascript_beginner.json",
        "coding_challenges_javascript_intermediate.json",
        "coding_challenges_javascript_advanced.json",
        "coding_challenges_kotlin_beginner.json",
        "coding_challenges_kotlin_intermediate.json",
        "coding_challenges_kotlin_advanced.json",
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
    )

    /**
     * Idempotent sync: (re-)imports all coding question JSON assets with REPLACE semantics.
     * Safe to call on every home entry so new tracks (AI/LLM, LeetCode, mock interview) appear
     * even when the DB was populated from an older asset set.
     */
    suspend fun syncCodingQuestionsFromAssets(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getInstance(context)
                val questionRepository =
                    CodingQuestionDatabaseRepository(database.codingQuestionDao())

                Log.d(
                    TAG,
                    "Syncing coding questions from assets (${CODING_QUESTION_ASSET_FILES.size} files)..."
                )

                var totalUpserted = 0
                for (file in CODING_QUESTION_ASSET_FILES) {
                    try {
                        val questions = loadQuestionsFromAsset(context, file)
                        if (questions.isNotEmpty()) {
                            val entities = questions.map { question ->
                                CodingQuestionEntity(
                                    questionId = question.id,
                                    createdAt = question.createdAt,
                                    language = question.language,
                                    level = question.level,
                                    question = question.question,
                                    options = Gson().toJson(question.options),
                                    correctAnswer = question.correctAnswer,
                                    explanation = question.explanation,
                                    platform = question.platform,
                                    pattern = question.pattern,
                                    codeSnippet = question.codeSnippet
                                )
                            }
                            questionRepository.insertQuestions(entities)
                            totalUpserted += entities.size
                            Log.d(TAG, "Synced ${entities.size} questions from $file")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing from $file: ${e.message}")
                    }
                }
                Log.d(TAG, "Sync completed. Total question rows upserted: $totalUpserted")
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync: ${e.message}")
            }
        }
    }

    /**
     * Import coding challenges from JSON assets only when the table is empty (legacy entry point).
     */
    suspend fun importFromAssetsIfDbEmpty(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getInstance(context)
                val questionRepository =
                    CodingQuestionDatabaseRepository(database.codingQuestionDao())
                val questionCount = questionRepository.getQuestionCount()
                if (questionCount == 0) {
                    Log.d(TAG, "Database is empty, running full asset sync...")
                    syncCodingQuestionsFromAssets(context)
                } else {
                    Log.d(
                        TAG,
                        "Database already contains $questionCount questions; importFromAssetsIfDbEmpty skipped"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during importFromAssetsIfDbEmpty: ${e.message}")
            }
        }
    }

    /**
     * Load questions from a JSON asset file.
     * 
     * @param context Application context for accessing assets.
     * @param fileName Name of the JSON file in assets.
     * @return List of coding challenges loaded from the file.
     */
    private fun mergeExplanationAndStrategy(raw: Map<String, Any>): String {
        val exp = (raw["explanation"] as? String)?.trim().orEmpty()
        val strat = (raw["strategy"] as? String)?.trim().orEmpty()
        return when {
            exp.isNotEmpty() && strat.isNotEmpty() && exp != strat -> "$exp\n\n$strat"
            exp.isNotEmpty() -> exp
            strat.isNotEmpty() -> strat
            else -> ""
        }
    }

    private fun appendHintsAndComplexity(raw: Map<String, Any>, base: String): String {
        val parts = mutableListOf<String>()
        if (base.isNotBlank()) parts.add(base)
        val hints = raw["hints"]
        if (hints is List<*>) {
            val lines = hints.mapNotNull { (it as? String)?.trim() }.filter { it.isNotEmpty() }
            if (lines.isNotEmpty()) {
                parts.add("Hints:\n" + lines.joinToString("\n") { "• $it" })
            }
        }
        val time = (raw["timeComplexity"] as? String)?.trim().orEmpty()
        val space = (raw["spaceComplexity"] as? String)?.trim().orEmpty()
        if (time.isNotEmpty() || space.isNotEmpty()) {
            val sb = StringBuilder()
            if (time.isNotEmpty()) sb.append("Time: ").append(time)
            if (time.isNotEmpty() && space.isNotEmpty()) sb.append('\n')
            if (space.isNotEmpty()) sb.append("Space: ").append(space)
            parts.add(sb.toString())
        }
        return parts.joinToString("\n\n")
    }

    private fun resolvePattern(raw: Map<String, Any>): String {
        val p = (raw["pattern"] as? String)?.trim().orEmpty()
        if (p.isNotEmpty()) return p
        return (raw["patternCategory"] as? String)?.trim().orEmpty()
    }

    private fun loadQuestionsFromAsset(context: Context, fileName: String): List<CodingChallenges> {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val gson = Gson()

            // Parse the raw JSON structure that matches the asset files
            val rawType = object : TypeToken<List<Map<String, Any>>>() {}.type
            val rawData: List<Map<String, Any>> = gson.fromJson(jsonString, rawType) ?: emptyList()

            // Convert to CodingChallenges objects with proper field mapping
            rawData.map { raw ->
                CodingChallenges(
                    id = raw["id"] as? String ?: "",
                    createdAt = raw["created_at"] as? String ?: "",
                    language = raw["language"] as? String ?: "",
                    level = raw["level"] as? String ?: "",
                    question = raw["question"] as? String ?: "",
                    options = (raw["options"] as? List<*>)?.mapNotNull { it as? String }
                        ?: emptyList(),
                    correctAnswer = raw["correctAnswer"] as? String ?: "",
                    explanation = appendHintsAndComplexity(raw, mergeExplanationAndStrategy(raw)),
                    platform = (raw["platform"] as? String)?.takeIf { it.isNotBlank() }
                        ?: "General",
                    pattern = resolvePattern(raw),
                    codeSnippet = (raw["code"] as? String)?.trim().orEmpty()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading $fileName: ${e.message}")
            emptyList()
        }
    }

    /**
     * Convert database entities back to CodingChallenges models.
     * 
     * @param entities List of database entities.
     * @return List of CodingChallenges models.
     */
    fun convertEntitiesToModels(entities: List<CodingQuestionEntity>): List<CodingChallenges> {
        val gson = Gson()
        return entities.map { entity ->
            val options = gson.fromJson<List<String>>(
                entity.options,
                object : TypeToken<List<String>>() {}.type
            )
            CodingChallenges(
                id = entity.questionId,
                createdAt = entity.createdAt,
                language = entity.language,
                level = entity.level,
                question = entity.question,
                options = options ?: emptyList(),
                correctAnswer = entity.correctAnswer,
                explanation = entity.explanation,
                platform = entity.platform,
                pattern = entity.pattern,
                codeSnippet = entity.codeSnippet
            )
        }
    }
}
