package com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.CodingChallengeEntity
import com.tatumgames.tatumtech.android.database.repository.CodingChallengeDatabaseRepository
import com.tatumgames.tatumtech.android.ui.components.screens.coding.models.CodingChallenges
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

    fun setQuestions(list: List<CodingChallenges>) {
        _questions.value = list
        _currentIndex.value = 0
        _answers.value = mutableMapOf()
        _showSummary.value = false
    }

    fun answerCurrentQuestion(answer: String) {
        val q = _questions.value.getOrNull(_currentIndex.value) ?: return
        _answers.value[q.id] = answer
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
            _showSummary.value = true
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
            val limit = when (level) {
                "Beginner" -> 5
                "Intermediate" -> 7
                "Advanced" -> 10
                else -> 5
            }
            val count = getTodayAnswerCount(language, level, platform)
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
