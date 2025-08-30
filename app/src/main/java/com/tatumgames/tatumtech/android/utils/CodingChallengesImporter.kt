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
    
    /**
     * Import coding challenges from JSON assets if the database is empty.
     * 
     * @param context Application context for accessing assets and database.
     */
    suspend fun importFromAssetsIfDbEmpty(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getInstance(context)
                val questionRepository = CodingQuestionDatabaseRepository(database.codingQuestionDao())
                
                val questionCount = questionRepository.getQuestionCount()
                
                if (questionCount == 0) {
                    Log.d(TAG, "Database is empty, importing questions from assets...")
                    
                    val assetFiles = listOf(
                        "coding_challenges_javascript_beginner.json",
                        "coding_challenges_javascript_intermediate.json",
                        "coding_challenges_kotlin_beginner.json",
                        "coding_challenges_kotlin_intermediate.json",
                        "coding_challenges_python_beginner.json",
                        "coding_challenges_python_intermediate.json"
                    )
                    
                    var totalImported = 0
                    
                    for (file in assetFiles) {
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
                                        platform = question.platform
                                    )
                                }
                                
                                questionRepository.insertQuestions(entities)
                                totalImported += entities.size
                                
                                Log.d(TAG, "Imported ${entities.size} questions from $file")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error importing from $file: ${e.message}")
                        }
                    }
                    
                    Log.d(TAG, "Import completed. Total questions imported: $totalImported")
                } else {
                    Log.d(TAG, "Database already contains $questionCount questions, skipping import")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during import process: ${e.message}")
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
                    id = raw["question_id"] as? String ?: "",
                    createdAt = raw["created_at"] as? String ?: "",
                    language = raw["language"] as? String ?: "",
                    level = raw["level"] as? String ?: "",
                    question = raw["question"] as? String ?: "",
                    options = (raw["options"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    correctAnswer = raw["correct_answer"] as? String ?: "",
                    explanation = raw["explanation"] as? String ?: "",
                    platform = raw["platform"] as? String ?: ""
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
            val options = gson.fromJson<List<String>>(entity.options, object : TypeToken<List<String>>() {}.type)
            CodingChallenges(
                id = entity.questionId,
                createdAt = entity.createdAt,
                language = entity.language,
                level = entity.level,
                question = entity.question,
                options = options ?: emptyList(),
                correctAnswer = entity.correctAnswer,
                explanation = entity.explanation,
                platform = entity.platform
            )
        }
    }
}
