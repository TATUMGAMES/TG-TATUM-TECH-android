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
import com.tatumgames.tatumtech.android.database.QuizProgressEntityHelper
import com.tatumgames.tatumtech.android.database.entity.QuizAnswerEventEntity
import com.tatumgames.tatumtech.android.database.entity.QuizProgressEntity
import com.tatumgames.tatumtech.android.database.repository.CodingQuestionDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.QuizAnswerEventDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.QuizProgressDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.ui.components.screens.coding.ChallengeCompletionTracker
import com.tatumgames.tatumtech.android.ui.components.screens.coding.QuizSessionBuilder
import com.tatumgames.tatumtech.android.ui.components.screens.coding.models.AnswerFeedback
import com.tatumgames.tatumtech.android.ui.components.screens.coding.models.CodingChallenges
import com.tatumgames.tatumtech.android.utils.CodingChallengesImporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.TimeZone

/**
 * ViewModel for coding challenge quizzes: session state, daily limits per
 * `(quizRoute + language + level)` bucket, and persisted progress.
 */
class CodingChallengesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val questionRepository = CodingQuestionDatabaseRepository(database.codingQuestionDao())
    private val quizProgressRepository = QuizProgressDatabaseRepository(database.quizProgressDao())
    private val quizAnswerEventRepository =
        QuizAnswerEventDatabaseRepository(database.quizAnswerEventDao())
    private val timelineRepository = TimelineDatabaseRepository(database.timelineDao())

    private val jsonFormat = Json { ignoreUnknownKeys = true }

    private val _questionList = MutableStateFlow<List<CodingChallenges>>(emptyList())
    val questions: StateFlow<List<CodingChallenges>> = _questionList.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    /** Always holds an immutable map snapshot; updates replace the whole map (no shared [MutableMap]). */
    private val _answersByQuestionId = MutableStateFlow<Map<String, String>>(emptyMap())
    val answers: StateFlow<Map<String, String>> = _answersByQuestionId.asStateFlow()

    private val _showSummary = MutableStateFlow(false)
    val showSummary: StateFlow<Boolean> = _showSummary.asStateFlow()

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

    private val _selectedAnswer = MutableStateFlow("")
    val selectedAnswer: StateFlow<String> = _selectedAnswer.asStateFlow()

    private val _answerFeedback = MutableStateFlow(AnswerFeedback.NONE)
    val answerFeedback: StateFlow<AnswerFeedback> = _answerFeedback.asStateFlow()

    private val _quizLoading = MutableStateFlow(false)
    val quizLoading: StateFlow<Boolean> = _quizLoading.asStateFlow()

    private val _dailyLimitReachedForBucket = MutableStateFlow(false)
    val dailyLimitReachedForBucket: StateFlow<Boolean> = _dailyLimitReachedForBucket.asStateFlow()

    private var activeQuizRoute: String = ""
    private var activeLanguageNormalized: String = ""
    private var activeLevel: String = ""

    private var submitAnswerJob: Job? = null

    /** Captured at submit so Continue can persist/advance without re-reading mutable UI state. */
    private var pendingAnswerCommit: PendingAnswerCommit? = null

    /**
     * Updates the current answer selection for the active question (UI binding).
     *
     * @param value Selected option text, or empty if none.
     */
    fun onSelectedAnswerChange(value: String) {
        if (_answerFeedback.value != AnswerFeedback.NONE) return
        _selectedAnswer.value = value
    }

    /**
     * Loads or resumes a quiz for the given bucket and refreshes today’s answer count from the DB.
     *
     * Side effects: reads/writes [QuizProgressEntity], updates question list, index, results flags.
     *
     * @param quizRoute Navigation route key for the quiz domain (isolates Coding vs AI/LLM vs LeetCode).
     * @param language Language chip value; use empty string when the quiz has no language.
     * @param level Difficulty level (Beginner / Intermediate / Advanced).
     * @param sessionSize Number of questions per session (default 10).
     */
    fun loadQuiz(
        quizRoute: String,
        language: String?,
        level: String,
        sessionSize: Int = SESSION_QUESTION_COUNT
    ) {
        viewModelScope.launch {
            _quizLoading.value = true
            try {
                activeQuizRoute = quizRoute
                activeLanguageNormalized = QuizProgressEntityHelper.normalizedLanguage(language)
                activeLevel = level
                _showResults.value = false
                _selectedAnswer.value = ""
                _answerFeedback.value = AnswerFeedback.NONE
                pendingAnswerCommit = null

                val progressId = QuizProgressEntityHelper.makeProgressId(quizRoute, language, level)
                val startOfDay = getStartOfTodayMillis()
                refreshTodayCount(quizRoute, activeLanguageNormalized, level, startOfDay)

                val existing = quizProgressRepository.getById(progressId)

                when {
                    existing != null && existing.isCompleted -> {
                        applyCompletedProgress(existing)
                    }

                    existing != null && !existing.isCompleted -> {
                        applyInProgressRestore(existing)
                    }

                    _todayAnswerCount.value >= DAILY_ANSWER_LIMIT -> {
                        _dailyLimitReachedForBucket.value = true
                        _questionList.value = emptyList()
                    }

                    else -> {
                        startNewRandomSession(
                            quizRoute = quizRoute,
                            languageNormalized = activeLanguageNormalized,
                            level = level,
                            sessionSize = sessionSize,
                            progressId = progressId
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CodingChallengesImporter.syncCodingQuestionsFromAssets(getApplication())
                tryReloadAfterImport(quizRoute, language, level, sessionSize)
            } finally {
                _quizLoading.value = false
            }
        }
    }

    /**
     * Re-runs the same load path as [loadQuiz] after asset import when the initial load threw
     * (typically empty DB). Updates active bucket fields and session state.
     */
    private suspend fun tryReloadAfterImport(
        quizRoute: String,
        language: String?,
        level: String,
        sessionSize: Int
    ) {
        val progressId = QuizProgressEntityHelper.makeProgressId(quizRoute, language, level)
        val startOfDay = getStartOfTodayMillis()
        activeQuizRoute = quizRoute
        activeLanguageNormalized = QuizProgressEntityHelper.normalizedLanguage(language)
        activeLevel = level
        refreshTodayCount(quizRoute, activeLanguageNormalized, level, startOfDay)
        val existing = quizProgressRepository.getById(progressId)
        when {
            existing != null && existing.isCompleted -> applyCompletedProgress(existing)
            existing != null && !existing.isCompleted -> applyInProgressRestore(existing)
            _todayAnswerCount.value >= DAILY_ANSWER_LIMIT -> {
                _dailyLimitReachedForBucket.value = true
                _questionList.value = emptyList()
            }

            else -> startNewRandomSession(
                quizRoute, activeLanguageNormalized, level, sessionSize, progressId
            )
        }
    }

    private suspend fun applyCompletedProgress(existing: QuizProgressEntity) {
        val ids = decodeQuestionIdsJson(existing.questionIdsJson)
        if (ids.isEmpty()) {
            _questionList.value = emptyList()
            return
        }
        val entities = questionRepository.getQuestionsByQuestionIds(ids)
        val ordered = ids.mapNotNull { id -> entities.find { it.questionId == id } }
        val models = CodingChallengesImporter.convertEntitiesToModels(ordered)
        _questionList.value = models
        _currentQuestionIndex.value = 0
        val answersMap = decodeAnswersJson(existing.answersJson).toMap()
        _answersByQuestionId.value = answersMap
        recalcSessionScore(models, answersMap)
        _showResults.value = true
        _dailyLimitReachedForBucket.value = false
        // Restore path: ensure timeline has the completion without creating duplicates.
        ChallengeCompletionTracker.recordCompletionIfAbsent(
            timelineRepository = timelineRepository,
            progressId = existing.id,
            questionIdsJson = existing.questionIdsJson,
            language = existing.language,
            quizRoute = existing.quizRoute,
            level = existing.level,
            timestamp = existing.lastUpdated
        )
    }

    private suspend fun applyInProgressRestore(existing: QuizProgressEntity) {
        val ids = decodeQuestionIdsJson(existing.questionIdsJson)
        if (ids.isEmpty()) {
            _questionList.value = emptyList()
            return
        }
        val entities = questionRepository.getQuestionsByQuestionIds(ids)
        val ordered = ids.mapNotNull { id -> entities.find { it.questionId == id } }
        val models = CodingChallengesImporter.convertEntitiesToModels(ordered)
        _questionList.value = models
        _currentQuestionIndex.value =
            existing.currentIndex.coerceIn(0, (models.size - 1).coerceAtLeast(0))
        val answersMap = decodeAnswersJson(existing.answersJson).toMap()
        _answersByQuestionId.value = answersMap
        recalcSessionScore(models, answersMap)
        _showResults.value = false
        _dailyLimitReachedForBucket.value = false
    }

    private suspend fun startNewRandomSession(
        quizRoute: String,
        languageNormalized: String,
        level: String,
        sessionSize: Int,
        progressId: String
    ) {
        var entities = questionRepository.getQuestionsByLanguageAndLevel(languageNormalized, level)
        if (entities.isEmpty()) {
            CodingChallengesImporter.syncCodingQuestionsFromAssets(getApplication())
            entities = questionRepository.getQuestionsByLanguageAndLevel(languageNormalized, level)
        }
        val pool = CodingChallengesImporter.convertEntitiesToModels(entities)
        val effectiveSize = QuizSessionBuilder.effectiveSessionSize(
            requestedSize = sessionSize,
            todayAnswerCount = _todayAnswerCount.value,
            dailyLimit = DAILY_ANSWER_LIMIT
        )
        if (effectiveSize <= 0) {
            _dailyLimitReachedForBucket.value = true
            _questionList.value = emptyList()
            return
        }

        val models = QuizSessionBuilder.buildSession(pool, effectiveSize)
        if (models.isEmpty()) {
            _questionList.value = emptyList()
            return
        }
        _questionList.value = models
        _currentQuestionIndex.value = 0
        _answersByQuestionId.value = emptyMap()
        _questionResults.value = emptyMap()
        _correctAnswers.value = 0
        _showResults.value = false
        _dailyLimitReachedForBucket.value = false
        _answerFeedback.value = AnswerFeedback.NONE

        val now = System.currentTimeMillis()
        quizProgressRepository.insertOrReplace(
            QuizProgressEntity(
                id = progressId,
                quizRoute = quizRoute,
                language = languageNormalized,
                level = level,
                currentIndex = 0,
                answersJson = encodeAnswersJson(emptyMap()),
                questionIdsJson = encodeQuestionIdsJson(models.map { it.id }),
                isCompleted = false,
                lastUpdated = now
            )
        )
    }

    /**
     * Shows correct/incorrect feedback with explanation. Does not advance until
     * [acknowledgeAnswerFeedback] so the learner can read the explanation.
     *
     * Cancels any in-flight commit job so an older acknowledge cannot overwrite newer state.
     */
    fun submitAnswer(answer: String, quizRoute: String, language: String?, level: String) {
        if (answer.isBlank()) return
        if (_answerFeedback.value != AnswerFeedback.NONE) return

        submitAnswerJob?.cancel()
        viewModelScope.launch {
            val lang = QuizProgressEntityHelper.normalizedLanguage(language)
            val startOfDay = getStartOfTodayMillis()
            if (!canAnswerMore(quizRoute, lang, level, startOfDay)) {
                _dailyLimitReachedForBucket.value = true
                refreshTodayCount(quizRoute, lang, level, startOfDay)
                return@launch
            }

            val currentQuestion =
                _questionList.value.getOrNull(_currentQuestionIndex.value) ?: return@launch
            val isCorrect = answer == currentQuestion.correctAnswer
            pendingAnswerCommit = PendingAnswerCommit(
                quizRoute = quizRoute,
                languageNormalized = lang,
                level = level,
                questionId = currentQuestion.id,
                answer = answer,
                isCorrect = isCorrect,
                questionIndex = _currentQuestionIndex.value
            )
            _answerFeedback.value =
                if (isCorrect) AnswerFeedback.CORRECT else AnswerFeedback.INCORRECT
        }
    }

    /**
     * Clears the feedback overlay, persists the pending answer, then advances or shows results.
     */
    fun acknowledgeAnswerFeedback() {
        val pending = pendingAnswerCommit ?: run {
            _answerFeedback.value = AnswerFeedback.NONE
            return
        }
        submitAnswerJob?.cancel()
        submitAnswerJob = viewModelScope.launch {
            _answerFeedback.value = AnswerFeedback.NONE
            pendingAnswerCommit = null

            val startOfDay = getStartOfTodayMillis()
            quizAnswerEventRepository.insert(
                QuizAnswerEventEntity(
                    quizRoute = pending.quizRoute,
                    language = pending.languageNormalized,
                    level = pending.level,
                    questionId = pending.questionId,
                    answerChosen = pending.answer,
                    isCorrect = pending.isCorrect,
                    timestamp = System.currentTimeMillis()
                )
            )

            _answersByQuestionId.value =
                _answersByQuestionId.value + (pending.questionId to pending.answer)
            val newMap = _answersByQuestionId.value
            recalcSessionScore(_questionList.value, newMap)
            refreshTodayCount(
                pending.quizRoute,
                pending.languageNormalized,
                pending.level,
                startOfDay
            )

            val progressId = QuizProgressEntityHelper.makeProgressId(
                pending.quizRoute,
                pending.languageNormalized,
                pending.level
            )
            val isLast = pending.questionIndex >= _questionList.value.lastIndex

            if (isLast) {
                _showResults.value = true
                val questionIdsJson = encodeQuestionIdsJson(_questionList.value.map { it.id })
                val completedAt = System.currentTimeMillis()
                quizProgressRepository.insertOrReplace(
                    QuizProgressEntity(
                        id = progressId,
                        quizRoute = pending.quizRoute,
                        language = pending.languageNormalized,
                        level = pending.level,
                        currentIndex = pending.questionIndex,
                        answersJson = encodeAnswersJson(newMap),
                        questionIdsJson = questionIdsJson,
                        isCompleted = true,
                        lastUpdated = completedAt
                    )
                )
                ChallengeCompletionTracker.recordCompletionIfAbsent(
                    timelineRepository = timelineRepository,
                    progressId = progressId,
                    questionIdsJson = questionIdsJson,
                    language = pending.languageNormalized,
                    quizRoute = pending.quizRoute,
                    level = pending.level,
                    timestamp = completedAt
                )
            } else {
                _currentQuestionIndex.value = pending.questionIndex + 1
                _selectedAnswer.value = ""
                quizProgressRepository.insertOrReplace(
                    QuizProgressEntity(
                        id = progressId,
                        quizRoute = pending.quizRoute,
                        language = pending.languageNormalized,
                        level = pending.level,
                        currentIndex = _currentQuestionIndex.value,
                        answersJson = encodeAnswersJson(newMap),
                        questionIdsJson = encodeQuestionIdsJson(_questionList.value.map { it.id }),
                        isCompleted = false,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private data class PendingAnswerCommit(
        val quizRoute: String,
        val languageNormalized: String,
        val level: String,
        val questionId: String,
        val answer: String,
        val isCorrect: Boolean,
        val questionIndex: Int
    )

    /**
     * Deletes persisted progress for the active bucket and reloads a new session when under the daily cap.
     */
    fun resetForNewDay() {
        viewModelScope.launch {
            val progressId = QuizProgressEntityHelper.makeProgressId(
                activeQuizRoute,
                activeLanguageNormalized,
                activeLevel
            )
            quizProgressRepository.deleteById(progressId)
            resetSessionState()
            if (activeQuizRoute.isNotEmpty() && activeLevel.isNotEmpty()) {
                loadQuiz(activeQuizRoute, activeLanguageNormalized, activeLevel)
            }
        }
    }

    fun updateCurrentStreak() {
        viewModelScope.launch {
            val allAnswers = quizAnswerEventRepository.getAllOrderByTimestampDesc()
            val days = allAnswers.map {
                val calendar = Calendar.getInstance(TimeZone.getDefault())
                calendar.timeInMillis = it.timestamp
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
            val sortedDays =
                days.sortedWith(
                    compareByDescending<Triple<Int, Int, Int>> { it.first }
                        .thenByDescending { it.second }
                        .thenByDescending { it.third }
                )
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

    private fun resetSessionState() {
        _questionList.value = emptyList()
        _currentQuestionIndex.value = 0
        _answersByQuestionId.value = emptyMap()
        _showSummary.value = false
        _showResults.value = false
        _correctAnswers.value = 0
        _questionResults.value = emptyMap()
        _selectedAnswer.value = ""
        _answerFeedback.value = AnswerFeedback.NONE
        pendingAnswerCommit = null
        _dailyLimitReachedForBucket.value = false
    }

    private suspend fun refreshTodayCount(
        quizRoute: String,
        languageNormalized: String,
        level: String,
        startOfDay: Long
    ) {
        val count = getTodayAnswerCount(quizRoute, languageNormalized, level, startOfDay)
        _todayAnswerCount.value = count
    }

    private suspend fun getTodayAnswerCount(
        quizRoute: String,
        languageNormalized: String,
        level: String,
        startOfDayMillis: Long
    ): Int = quizAnswerEventRepository.countTodayForBucket(
        startOfDayMillis,
        quizRoute,
        languageNormalized,
        level
    )

    private suspend fun canAnswerMore(
        quizRoute: String,
        languageNormalized: String,
        level: String,
        startOfDayMillis: Long
    ): Boolean {
        val count = getTodayAnswerCount(quizRoute, languageNormalized, level, startOfDayMillis)
        return count < DAILY_ANSWER_LIMIT
    }

    private fun recalcSessionScore(
        models: List<CodingChallenges>,
        answersMap: Map<String, String>
    ) {
        val (results, correct) = QuizSessionBuilder.countCorrect(models, answersMap)
        _questionResults.value = results
        _correctAnswers.value = correct
    }

    /** Local calendar start of day in the default timezone, as epoch millis. */
    private fun getStartOfTodayMillis(): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun encodeAnswersJson(map: Map<String, String>): String =
        if (map.isEmpty()) "{}" else jsonFormat.encodeToString(map)

    private fun decodeAnswersJson(s: String): Map<String, String> =
        if (s.isBlank() || s == "{}") emptyMap() else jsonFormat.decodeFromString(s)

    private fun encodeQuestionIdsJson(ids: List<String>): String =
        jsonFormat.encodeToString(ids)

    private fun decodeQuestionIdsJson(s: String): List<String> =
        if (s.isBlank() || s == "[]") emptyList() else jsonFormat.decodeFromString(s)

    companion object {
        const val DAILY_ANSWER_LIMIT = 30
        private const val SESSION_QUESTION_COUNT = 10
    }
}
