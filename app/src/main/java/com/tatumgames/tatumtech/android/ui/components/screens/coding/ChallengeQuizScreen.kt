/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package com.tatumgames.tatumtech.android.ui.components.screens.coding

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.MotionDefaults
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.coding.models.AnswerFeedback
import com.tatumgames.tatumtech.android.ui.components.screens.coding.models.CodingChallenges
import com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels.CodingChallengesViewModel
import com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels.CodingChallengesViewModel.Companion.DAILY_ANSWER_LIMIT
import com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels.factory.CodingChallengesViewModelFactory
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.Grey400
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.ui.theme.Red300
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import com.tatumgames.tatumtech.android.ui.theme.SuccessGreen
import com.tatumgames.tatumtech.android.ui.theme.White

private const val DAILY_LIMIT = DAILY_ANSWER_LIMIT

/**
 * Shared coding / quiz challenge UI (language + difficulty chips, questions, results).
 *
 * @param quizRoute Route string for this screen — used when leaving results to pop the back stack correctly.
 * @param showLeetTeachingUi When true, show pattern badge and monospace code block for Leet-style questions.
 */
@Composable
fun ChallengeQuizScreen(
    navController: NavController,
    titleResId: Int,
    languages: List<String>,
    difficulties: List<String> = listOf("Beginner", "Intermediate", "Advanced"),
    quizRoute: String,
    showLeetTeachingUi: Boolean = false
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val viewModel: CodingChallengesViewModel = viewModel(
        factory = CodingChallengesViewModelFactory(app)
    )

    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val showResults by viewModel.showResults.collectAsState()
    val todayAnswerCount by viewModel.todayAnswerCount.collectAsState()
    val questionResults by viewModel.questionResults.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val answerFeedback by viewModel.answerFeedback.collectAsState()
    val quizLoading by viewModel.quizLoading.collectAsState()
    val dailyLimitReachedForBucket by viewModel.dailyLimitReachedForBucket.collectAsState()

    // Keep last non-NONE feedback so AnimatedVisibility exit does not flash the opposite icon/text.
    var displayedFeedback by remember { mutableStateOf(AnswerFeedback.NONE) }
    var feedbackQuestion by remember { mutableStateOf<CodingChallenges?>(null) }
    LaunchedEffect(answerFeedback, currentIndex, questions) {
        if (answerFeedback != AnswerFeedback.NONE) {
            displayedFeedback = answerFeedback
            feedbackQuestion = questions.getOrNull(currentIndex)
        }
    }
    val initialLanguage = languages.firstOrNull().orEmpty()
    var selectedLanguage by remember(quizRoute) { mutableStateOf(initialLanguage) }
    var selectedDifficulty by remember(quizRoute) {
        mutableStateOf(
            difficulties.firstOrNull().orEmpty()
        )
    }

    val chipScrollLang = rememberScrollState()
    val chipScrollDiff = rememberScrollState()
    val contentScroll = rememberScrollState()
    val codeSnippetScroll = rememberScrollState()
    val showLanguageRow = languages.size > 1
    val animationsEnabled = MotionDefaults.animationsEnabled()
    val feedbackBlocking = answerFeedback != AnswerFeedback.NONE

    LaunchedEffect(selectedLanguage, selectedDifficulty, quizRoute) {
        if (selectedLanguage.isNotEmpty() && selectedDifficulty.isNotEmpty()) {
            viewModel.loadQuiz(
                quizRoute = quizRoute,
                language = selectedLanguage,
                level = selectedDifficulty
            )
        }
    }

    LaunchedEffect(currentIndex) {
        viewModel.onSelectedAnswerChange("")
        codeSnippetScroll.scrollTo(0)
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(titleResId),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = ScreenScaffoldLight
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(contentScroll)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                if (showLanguageRow) {
                    StandardText(
                        text = stringResource(R.string.select_language),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(chipScrollLang)
                    ) {
                        languages.forEach { language ->
                            SelectableChip(
                                text = language,
                                isSelected = selectedLanguage == language,
                                onClick = { selectedLanguage = language }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                StandardText(
                    text = stringResource(R.string.select_level),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(chipScrollDiff)
                ) {
                    difficulties.forEach { difficulty ->
                        SelectableChip(
                            text = difficulty,
                            isSelected = selectedDifficulty == difficulty,
                            onClick = { selectedDifficulty = difficulty }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                when {
                    quizLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Purple500)
                        }
                    }

                    showResults && questions.isNotEmpty() -> {
                        val scoreFromResults = questionResults.values.count { it }
                        ChallengeResultsScreen(
                            correctAnswers = scoreFromResults,
                            totalQuestions = questions.size,
                            questionResults = questionResults,
                            questions = questions,
                            todayAnswerCount = todayAnswerCount,
                            dailyLimit = DAILY_LIMIT,
                            navController = navController,
                            quizRoute = quizRoute,
                            onDoAnotherChallenge = {
                                viewModel.resetForNewDay()
                            }
                        )
                    }

                    dailyLimitReachedForBucket -> {
                        DailyLimitCard()
                    }

                    questions.isEmpty() -> {
                        StandardText(text = stringResource(R.string.no_questions))
                    }

                    else -> {
                        val question = questions.getOrNull(currentIndex)
                        if (question != null) {
                            val isLastQuestion = currentIndex == questions.lastIndex
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StandardText(
                                    text = stringResource(R.string.progress),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                )
                                StandardText(
                                    text = "$todayAnswerCount/$DAILY_LIMIT",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = {
                                    (todayAnswerCount.toFloat() / DAILY_LIMIT.toFloat()).coerceIn(
                                        0f,
                                        1f
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Purple500,
                                trackColor = Purple500.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            AnimatedContent(
                                targetState = currentIndex to question,
                                transitionSpec = {
                                    val enter = fadeIn(
                                        MotionDefaults.durationSpec(
                                            animationsEnabled,
                                            MotionDefaults.CONTENT_MS
                                        )
                                    ) + slideInHorizontally(
                                        animationSpec = MotionDefaults.durationSpec(
                                            animationsEnabled,
                                            MotionDefaults.CONTENT_MS
                                        ),
                                        initialOffsetX = { if (animationsEnabled) it / 16 else 0 }
                                    )
                                    val exit = fadeOut(
                                        MotionDefaults.durationSpec(
                                            animationsEnabled,
                                            MotionDefaults.CONTENT_MS
                                        )
                                    ) + slideOutHorizontally(
                                        animationSpec = MotionDefaults.durationSpec(
                                            animationsEnabled,
                                            MotionDefaults.CONTENT_MS
                                        ),
                                        targetOffsetX = { if (animationsEnabled) -it / 16 else 0 }
                                    )
                                    enter togetherWith exit
                                },
                                label = "quizQuestion"
                            ) { (questionIndex, animatedQuestion) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        StandardText(
                                            text = "Question ${questionIndex + 1} of ${questions.size}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Purple500
                                        )
                                        if (showLeetTeachingUi && animatedQuestion.pattern.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer
                                            ) {
                                                StandardText(
                                                    text = stringResource(
                                                        R.string.leetcode_pattern_label,
                                                        animatedQuestion.pattern
                                                    ),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(
                                                        horizontal = 12.dp,
                                                        vertical = 8.dp
                                                    )
                                                )
                                            }
                                        }
                                        if (showLeetTeachingUi && animatedQuestion.codeSnippet.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Card(
                                                modifier = Modifier.semantics {
                                                    contentDescription =
                                                        context.getString(R.string.leetcode_code_block)
                                                },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(codeSnippetScroll)
                                                        .padding(12.dp)
                                                ) {
                                                    Text(
                                                        text = animatedQuestion.codeSnippet,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        softWrap = false
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        StandardText(
                                            text = animatedQuestion.question,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        animatedQuestion.options.forEachIndexed { index, option ->
                                            RadioButtonOption(
                                                text = "${('A' + index)}. $option",
                                                isSelected = selectedAnswer == option,
                                                enabled = !feedbackBlocking,
                                                onClick = {
                                                    viewModel.onSelectedAnswerChange(option)
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            val canSubmitToday = todayAnswerCount < DAILY_LIMIT
                            Button(
                                onClick = {
                                    if (selectedAnswer.isNotEmpty() && canSubmitToday && !feedbackBlocking) {
                                        viewModel.submitAnswer(
                                            answer = selectedAnswer,
                                            quizRoute = quizRoute,
                                            language = selectedLanguage,
                                            level = selectedDifficulty
                                        )
                                    }
                                },
                                enabled = selectedAnswer.isNotEmpty() &&
                                    canSubmitToday &&
                                    !feedbackBlocking,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Purple500,
                                    disabledContainerColor = Purple500.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                StandardText(
                                    text = if (isLastQuestion) {
                                        stringResource(R.string.submit)
                                    } else {
                                        stringResource(R.string.next)
                                    },
                                    color = White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            StandardText(text = stringResource(R.string.loading))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            val feedbackForDisplay =
                if (displayedFeedback != AnswerFeedback.NONE) {
                    displayedFeedback
                } else {
                    answerFeedback
                }
            val questionForFeedback = feedbackQuestion ?: questions.getOrNull(currentIndex)
            AnswerFeedbackOverlay(
                visible = answerFeedback != AnswerFeedback.NONE,
                feedback = feedbackForDisplay,
                correctAnswer = questionForFeedback?.correctAnswer.orEmpty(),
                explanation = questionForFeedback?.explanation.orEmpty(),
                onContinue = { viewModel.acknowledgeAnswerFeedback() }
            )
        }
    }
}

@Composable
private fun DailyLimitCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StandardText(
                text = stringResource(R.string.daily_limit_reached),
                style = MaterialTheme.typography.titleLarge,
                color = Purple500
            )
            Spacer(modifier = Modifier.height(8.dp))
            StandardText(
                text = stringResource(R.string.daily_limit_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) Purple500 else White
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Purple500 else Grey400,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        StandardText(
            text = text,
            color = if (isSelected) White else Black,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ChallengeResultsScreen(
    correctAnswers: Int,
    totalQuestions: Int,
    questionResults: Map<String, Boolean>,
    questions: List<CodingChallenges>,
    todayAnswerCount: Int,
    dailyLimit: Int,
    navController: NavController,
    quizRoute: String,
    onDoAnotherChallenge: () -> Unit
) {
    val canDoAnotherChallenge = todayAnswerCount < dailyLimit
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StandardText(
                text = stringResource(R.string.challenge_results),
                style = MaterialTheme.typography.titleLarge,
                color = Purple500
            )

            Spacer(modifier = Modifier.height(16.dp))

            StandardText(
                text = stringResource(R.string.score_format, correctAnswers, totalQuestions),
                style = MaterialTheme.typography.headlineMedium,
                color = Purple500
            )

            val percentage = if (totalQuestions > 0) {
                ((correctAnswers.toFloat() / totalQuestions.toFloat()) * 100).toInt()
            } else {
                0
            }
            StandardText(
                text = stringResource(R.string.percentage_format, percentage),
                style = MaterialTheme.typography.bodyLarge,
                color = Purple500
            )

            Spacer(modifier = Modifier.height(16.dp))

            questions.forEachIndexed { index, question ->
                val isCorrect = questionResults[question.id] ?: false
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StandardText(
                        text = "Q${index + 1}:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(40.dp)
                    )
                    StandardText(
                        text = if (isCorrect) {
                            stringResource(R.string.correct)
                        } else {
                            stringResource(R.string.incorrect)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCorrect) SuccessGreen else Red300
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (canDoAnotherChallenge) {
                        onDoAnotherChallenge()
                    } else {
                        navController.navigate(NavRoutes.HOME_PAGER_SCREEN) {
                            popUpTo(quizRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Purple500),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                StandardText(
                    text = if (canDoAnotherChallenge) {
                        stringResource(R.string.do_another_challenge)
                    } else {
                        stringResource(R.string.try_again_tomorrow)
                    },
                    color = White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun RadioButtonOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { if (enabled) onClick() },
            enabled = enabled,
            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                selectedColor = Purple500
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        StandardText(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}
