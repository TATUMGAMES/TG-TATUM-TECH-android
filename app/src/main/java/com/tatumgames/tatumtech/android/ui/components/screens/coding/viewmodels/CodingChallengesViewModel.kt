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
package com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.CodingChallengeEntity
import com.tatumgames.tatumtech.android.database.repository.CodingChallengeDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.CodingQuestionDatabaseRepository
import com.tatumgames.tatumtech.android.ui.components.screens.coding.models.CodingChallenges
import com.tatumgames.tatumtech.android.utils.CodingChallengesImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class CodingChallengesViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val database = AppDatabase.Companion.getInstance(application)
    private val answerRepository = CodingChallengeDatabaseRepository(database.codingChallengeDao())
    private val questionRepository = CodingQuestionDatabaseRepository(database.codingQuestionDao())

    private val _questions = MutableStateFlow<List<CodingChallenges>>(emptyList())
    val questions: StateFlow<List<CodingChallenges>> = _questions

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _answers = MutableStateFlow<MutableMap<String, String>>(mutableMapOf())
    val answers: StateFlow<Map<String, String>> = _answers

    private val _showSummary = MutableStateFlow(false)
    val showSummary: StateFlow<Boolean> = _showSummary

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _todayAnswerCount = MutableStateFlow(0)
    val todayAnswerCount: StateFlow<Int> = _todayAnswerCount.asStateFlow()

    private val _correctAnswers = MutableStateFlow(0)
    val correctAnswers: StateFlow<Int> = _correctAnswers.asStateFlow()

    private val _showResults = MutableStateFlow(false)
    val showResults: StateFlow<Boolean> = _showResults.asStateFlow()

    private val _questionResults = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val questionResults: StateFlow<Map<String, Boolean>> = _questionResults.asStateFlow()

    fun setQuestions(list: List<CodingChallenges>) {
        _questions.value = list
        _currentIndex.value = 0
        _answers.value = mutableMapOf()
        _showSummary.value = false
    }

    /**
     * Load questions from the database with randomization.
     * 
     * @param language Optional language filter.
     * @param level Optional level filter.
     */
    fun loadQuestions(language: String? = null, level: String? = null) {
        viewModelScope.launch {
            try {
                val entities = when {
                    language != null && level != null -> {
                        questionRepository.getQuestionsByLanguageAndLevel(language, level)
                    }
                    language != null -> {
                        questionRepository.getQuestionsByLanguage(language)
                    }
                    else -> {
                        questionRepository.getAllQuestions()
                    }
                }
                
                val questions = CodingChallengesImporter.convertEntitiesToModels(entities)
                // Randomize questions for each session
                val randomizedQuestions = questions.shuffled()
                setQuestions(randomizedQuestions)
            } catch (e: Exception) {
                // If database is empty, try to import from assets
                CodingChallengesImporter.importFromAssetsIfDbEmpty(getApplication())
                // Retry loading after import
                val entities = questionRepository.getAllQuestions()
                val questions = CodingChallengesImporter.convertEntitiesToModels(entities)
                val randomizedQuestions = questions.shuffled()
                setQuestions(randomizedQuestions)
            }
        }
    }

    fun answerCurrentQuestion(answer: String) {
        val q = _questions.value.getOrNull(_currentIndex.value) ?: return
        _answers.value[q.id] = answer
        
        // Check if answer is correct
        val isCorrect = answer == q.correctAnswer
        _questionResults.value = _questionResults.value + (q.id to isCorrect)
        
        if (isCorrect) {
            _correctAnswers.value = _correctAnswers.value + 1
        }
        
        viewModelScope.launch {
            answerRepository.insert(
                CodingChallengeEntity(
                    questionId = q.id,
                    answerChosen = answer,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun goToNextQuestion() {
        if (_currentIndex.value < _questions.value.size - 1) {
            _currentIndex.value++
        } else {
            _showResults.value = true
        }
    }

    fun goToPreviousQuestion() {
        if (_currentIndex.value > 0) {
            _currentIndex.value--
        }
    }

    fun reset() {
        _currentIndex.value = 0
        _answers.value = mutableMapOf()
        _showSummary.value = false
        _showResults.value = false
        _correctAnswers.value = 0
        _questionResults.value = emptyMap()
    }

    fun resetForNewDay() {
        reset()
        // Reload questions with randomization for new day
        loadQuestions()
    }

    private fun getStartOfToday(): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    suspend fun getTodayAnswerCount(
        language: String,
        level: String,
        platform: String
    ): Int {
        val allAnswers = answerRepository.getAllChallengeAnswers()
        val startOfToday = getStartOfToday()
        return allAnswers.count {
            it.timestamp >= startOfToday &&
                    questions.value.find { q -> q.id == it.questionId }?.let { q ->
                        q.language == language && q.level == level && q.platform == platform
                    } == true
        }
    }

    fun canAnswerMoreToday(
        language: String,
        level: String,
        platform: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val limit = 7 // Fixed limit of 7 questions per day for all difficulties
            val count = getTodayAnswerCount(language, level, platform)
            _todayAnswerCount.value = count
            onResult(count < limit)
        }
    }

    fun answerCurrentQuestionWithLimit(
        answer: String,
        language: String,
        level: String,
        platform: String,
        onLimitReached: () -> Unit
    ) {
        canAnswerMoreToday(language, level, platform) { canAnswer ->
            if (canAnswer) {
                answerCurrentQuestion(answer)
                goToNextQuestion()
            } else {
                onLimitReached()
            }
        }
    }

    fun goToNextQuestionWithLimit(
        language: String,
        level: String,
        platform: String,
        onLimitReached: () -> Unit
    ) {
        canAnswerMoreToday(language, level, platform) { canAnswer ->
            if (canAnswer) {
                // Count as answered even if no answer selected
                val currentQuestion = _questions.value.getOrNull(_currentIndex.value)
                if (currentQuestion != null) {
                    _answers.value[currentQuestion.id] = "skipped"
                    _questionResults.value = _questionResults.value + (currentQuestion.id to false)
                }
                goToNextQuestion()
            } else {
                onLimitReached()
            }
        }
    }

    fun updateCurrentStreak() {
        viewModelScope.launch {
            val allAnswers = answerRepository.getAllChallengeAnswers()
            val days = allAnswers.map {
                val calendar = Calendar.getInstance(TimeZone.getDefault())
                calendar.timeInMillis = it.timestamp
                // Use only year, month, day for streak
                Triple(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }.toSet()
            if (days.isEmpty()) {
                _currentStreak.value = 0
                return@launch
            }
            // Sort days descending
            val sortedDays = days.sortedWith(compareByDescending<Triple<Int, Int, Int>> { it.first }
                .thenByDescending { it.second }
                .thenByDescending { it.third })
            var streak = 1
            for (i in 1 until sortedDays.size) {
                val previous = sortedDays[i - 1]
                val current = sortedDays[i]
                val previousDate = Calendar.getInstance().apply {
                    set(previous.first, previous.second, previous.third, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val currentDate = Calendar.getInstance().apply {
                    set(current.first, current.second, current.third, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val diff = (previousDate - currentDate) / (1000 * 60 * 60 * 24)
                if (diff == 1L) {
                    streak++
                } else if (diff > 1L) {
                    break
                }
            }
            _currentStreak.value = streak
        }
    }
}
