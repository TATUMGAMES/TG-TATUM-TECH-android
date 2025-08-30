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
package com.tatumgames.tatumtech.android.ui.components.screens.coding

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels.CodingChallengesViewModel
import com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels.factory.CodingChallengesViewModelFactory
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.ui.theme.SpringPurple
import com.tatumgames.tatumtech.android.ui.theme.White

@Composable
fun CodingChallengesScreen(
    navController: NavController
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
    val correctAnswers by viewModel.correctAnswers.collectAsState()
    val questionResults by viewModel.questionResults.collectAsState()

    // Language and difficulty selection using chips
    var selectedLanguage by remember { mutableStateOf("Kotlin") }
    var selectedDifficulty by remember { mutableStateOf("Beginner") }

        // Answer selection
    var selectedAnswer by remember { mutableStateOf("") }
    
    // Progress tracking
    var canAnswerMore by remember { mutableStateOf(true) }
    
    // Animation states
    var showCorrectAnimation by remember { mutableStateOf(false) }
    var showIncorrectAnimation by remember { mutableStateOf(false) }
    
    // Coroutine scope for animations
    val coroutineScope = rememberCoroutineScope()

    val languages = listOf("Kotlin", "JavaScript", "Python")
    val difficulties = listOf("Beginner", "Intermediate")

    // Load questions from database when the screen is first displayed or when selection changes
    LaunchedEffect(selectedLanguage, selectedDifficulty) {
        viewModel.loadQuestions(selectedLanguage, selectedDifficulty)
        // Check if user can answer more questions today
        viewModel.canAnswerMoreToday(selectedLanguage, selectedDifficulty, "Mobile") { canAnswer ->
            canAnswerMore = canAnswer
        }
    }

    // Reset answer selection when question changes
    LaunchedEffect(currentIndex) {
        selectedAnswer = ""
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.title_coding_challenges),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Language Selection using Chips
            StandardText(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
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

            // Difficulty Selection using Chips
            StandardText(
                text = stringResource(R.string.select_level),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
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

            // Questions Content
            when {
                questions.isEmpty() -> {
                    StandardText(text = stringResource(R.string.no_questions))
                }

                showResults -> {
                    ChallengeResultsScreen(
                        correctAnswers = correctAnswers,
                        totalQuestions = todayAnswerCount,
                        questionResults = questionResults,
                        questions = questions,
                        navController = navController,
                        onReturnToChallenges = {
                            viewModel.resetForNewDay()
                        }
                    )
                }

                !canAnswerMore -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    val question = questions.getOrNull(currentIndex)
                    if (question != null) {
                        // Progress Bar
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
                                text = "${todayAnswerCount}/7",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (todayAnswerCount.toFloat() / 7f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Purple500,
                            trackColor = Purple500.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Question Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Question Header
                                StandardText(
                                    text = "Question ${currentIndex + 1} of ${questions.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Purple500
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Question Text
                                StandardText(
                                    text = question.question,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Multiple Choice Options
                                question.options.forEachIndexed { index, option ->
                                    RadioButtonOption(
                                        text = "${('A' + index)}. $option",
                                        isSelected = selectedAnswer == option,
                                        onClick = { selectedAnswer = option }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Navigation Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (selectedAnswer.isNotEmpty()) {
                                        // Show animation based on answer correctness
                                        val currentQuestion = questions.getOrNull(currentIndex)
                                        if (currentQuestion != null) {
                                            val isCorrect = selectedAnswer == currentQuestion.correctAnswer
                                            if (isCorrect) {
                                                showCorrectAnimation = true
                                            } else {
                                                showIncorrectAnimation = true
                                            }
                                            
                                            // Hide animation after 1.5 seconds
                                            coroutineScope.launch {
                                                delay(1500)
                                                showCorrectAnimation = false
                                                showIncorrectAnimation = false
                                            }
                                        }
                                        
                                        viewModel.answerCurrentQuestionWithLimit(
                                            selectedAnswer,
                                            selectedLanguage,
                                            selectedDifficulty,
                                            "Mobile"
                                        ) {
                                            // Limit reached callback
                                            canAnswerMore = false
                                        }
                                        selectedAnswer = ""
                                    }
                                },
                                enabled = selectedAnswer.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Purple500,
                                    disabledContainerColor = Purple500.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                StandardText(
                                    text = stringResource(R.string.submit),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Button(
                                onClick = {
                                    viewModel.goToNextQuestionWithLimit(
                                        selectedLanguage,
                                        selectedDifficulty,
                                        "Mobile"
                                    ) {
                                        // Limit reached callback
                                        canAnswerMore = false
                                    }
                                    selectedAnswer = ""
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SpringPurple
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                StandardText(
                                    text = stringResource(R.string.next),
                                    color = Purple500,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    } else {
                        StandardText(text = stringResource(R.string.loading))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Answer feedback animations
        AnimatedVisibility(
            visible = showCorrectAnimation,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.coding_challenge_correct),
                    contentDescription = "Correct Answer",
                    modifier = Modifier.size(200.dp)
                )
            }
        }
        
        AnimatedVisibility(
            visible = showIncorrectAnimation,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.coding_challenge_incorrect),
                    contentDescription = "Incorrect Answer",
                    modifier = Modifier.size(200.dp)
                )
            }
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
                color = if (isSelected) Purple500 else Color(0xFFCCCCCC),
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
    questions: List<com.tatumgames.tatumtech.android.ui.components.screens.coding.models.CodingChallenges>,
    navController: NavController,
    onReturnToChallenges: () -> Unit
) {
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

                        // Score display
            StandardText(
                text = stringResource(R.string.score_format, correctAnswers, totalQuestions),
                style = MaterialTheme.typography.headlineMedium,
                color = Purple500
            )
            
            val percentage = if (totalQuestions > 0) {
                ((correctAnswers.toFloat() / totalQuestions.toFloat()) * 100).toInt()
            } else 0
            StandardText(
                text = stringResource(R.string.percentage_format, percentage),
                style = MaterialTheme.typography.bodyLarge,
                color = Purple500
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Question results
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
                        text = if (isCorrect) stringResource(R.string.correct) else stringResource(R.string.incorrect),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onReturnToChallenges()
                    navController.navigate(NavRoutes.MAIN_SCREEN) {
                        popUpTo(NavRoutes.CODING_CHALLENGES_SCREEN) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Purple500),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                StandardText(
                    text = stringResource(R.string.try_again_tomorrow),
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onClick() },
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
