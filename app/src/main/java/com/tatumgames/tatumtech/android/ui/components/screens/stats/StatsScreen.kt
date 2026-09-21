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
package com.tatumgames.tatumtech.android.ui.components.screens.stats

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.repository.QuizAnswerEventDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.QuizProgressDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.enums.TimelineType
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.coding.ChallengeCompletionTracker
import com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels.CodingChallengesViewModel
import com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels.factory.CodingChallengesViewModelFactory
import com.tatumgames.tatumtech.android.ui.models.Achievement
import com.tatumgames.tatumtech.android.ui.theme.Gold
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import com.tatumgames.tatumtech.android.ui.theme.Teal200
import com.tatumgames.tatumtech.android.ui.utils.JsonImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val LONG_ANIMATION_STEP_DELAY_MS = 30L
private const val MEDIUM_ANIMATION_STEP_DELAY_MS = LONG_ANIMATION_STEP_DELAY_MS / 2
private const val SHORT_ANIMATION_STEP_DELAY_MS = 10L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val timelineRepository = remember { TimelineDatabaseRepository(database.timelineDao()) }
    val quizProgressRepository = remember {
        QuizProgressDatabaseRepository(database.quizProgressDao())
    }

    var eventsAttended by remember { mutableIntStateOf(0) }
    var challengesCompleted by remember { mutableIntStateOf(0) }
    var percentCorrect by remember { mutableIntStateOf(0) }
    var qrCodesScanned by remember { mutableIntStateOf(0) }
    var achievementsUnlocked by remember { mutableIntStateOf(0) }
    var totalQuestionsAnswered by remember { mutableIntStateOf(0) }
    var correctAnswers by remember { mutableIntStateOf(0) }
    var categoryCounts by remember {
        mutableStateOf(ChallengeCompletionTracker.statsCategories.associateWith { 0 })
    }

    var animatedEvents by remember { mutableIntStateOf(0) }
    var animatedChallenges by remember { mutableIntStateOf(0) }
    var animatedQrCodes by remember { mutableIntStateOf(0) }
    var animatedPercent by remember { mutableIntStateOf(0) }

    var achievementsList by remember { mutableStateOf<List<Achievement>>(emptyList()) }
    var statsLoaded by remember { mutableStateOf(false) }

    val challengeViewModel: CodingChallengesViewModel = viewModel(
        factory = CodingChallengesViewModelFactory(context.applicationContext as Application)
    )
    val streak by challengeViewModel.currentStreak.collectAsState()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            ChallengeCompletionTracker.backfillFromCompletedProgress(
                quizProgressRepository = quizProgressRepository,
                timelineRepository = timelineRepository
            )

            val allTimeline = timelineRepository.getAllTimelineEvents()
            val challengeEvents = allTimeline.filter {
                it.type == TimelineType.CHALLENGE_COMPLETION.typeValue
            }
            eventsAttended =
                allTimeline.count { it.type == TimelineType.EVENT_REGISTRATION.typeValue }
            challengesCompleted = challengeEvents.size
            categoryCounts = ChallengeCompletionTracker.categoryCounts(challengeEvents)
            qrCodesScanned = allTimeline.count { it.type == TimelineType.QR_SCAN.typeValue }

            val quizAnswerRepo = QuizAnswerEventDatabaseRepository(database.quizAnswerEventDao())
            val quizAnswerEvents = quizAnswerRepo.getAllOrderByTimestampDesc()
            totalQuestionsAnswered = quizAnswerEvents.size
            correctAnswers = quizAnswerEvents.count { it.isCorrect }
            percentCorrect = if (totalQuestionsAnswered > 0) {
                (correctAnswers * 100) / totalQuestionsAnswered
            } else 0

            val progress = EngagementTracker.buildProgressCounts(context)
            achievementsList = AchievementUnlockEvaluator.withUnlockState(
                JsonImporter.loadAchievements(context),
                progress
            )
            achievementsUnlocked = achievementsList.count { it.isUnlocked }
            challengeViewModel.updateCurrentStreak()
        }
        statsLoaded = true

        repeat(eventsAttended + 1) {
            animatedEvents = it
            delay(LONG_ANIMATION_STEP_DELAY_MS)
        }
        repeat(challengesCompleted + 1) {
            animatedChallenges = it
            delay(MEDIUM_ANIMATION_STEP_DELAY_MS)
        }
        repeat(qrCodesScanned + 1) {
            animatedQrCodes = it
            delay(MEDIUM_ANIMATION_STEP_DELAY_MS)
        }
        repeat(percentCorrect + 1) {
            animatedPercent = it
            delay(SHORT_ANIMATION_STEP_DELAY_MS)
        }
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.stats),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = ScreenScaffoldLight
    ) { paddingValues ->
        val hasData =
            eventsAttended > 0 ||
                    challengesCompleted > 0 ||
                    qrCodesScanned > 0 ||
                    achievementsUnlocked > 0 ||
                    totalQuestionsAnswered > 0

        if (statsLoaded && !hasData) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                StandardText(
                    text = stringResource(R.string.no_statistics_loaded),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    StandardText(
                        text = stringResource(R.string.your_progress),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProgressRing(
                            label = stringResource(R.string.events),
                            value = animatedEvents,
                            max = 20,
                            color = Purple500
                        )
                        ProgressRing(
                            label = stringResource(R.string.challenges),
                            value = animatedChallenges,
                            max = 30,
                            color = Gold
                        )
                        ProgressRing(
                            label = stringResource(R.string.qr_scans),
                            value = animatedQrCodes,
                            max = 30,
                            color = Teal200
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        StandardText(
                            text = stringResource(R.string.percent_correct),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AnimatedPercentRing(percent = animatedPercent)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    StandardText(
                        text = stringResource(R.string.current_streak_days, streak),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    StandardText(
                        text = stringResource(R.string.achievements_unlocked, achievementsUnlocked),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    StandardText(
                        text = stringResource(R.string.category_breakdown),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryBreakdownView(
                        categoryCounts = categoryCounts,
                        totalCompletions = challengesCompleted,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    StandardText(
                        text = stringResource(R.string.coding_challenge_stats),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProgressRing(
                            label = stringResource(R.string.stats_questions),
                            value = totalQuestionsAnswered,
                            max = 100,
                            color = Purple500
                        )
                        ProgressRing(
                            label = stringResource(R.string.stats_correct),
                            value = correctAnswers,
                            max = totalQuestionsAnswered.coerceAtLeast(1),
                            color = Gold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StandardText(
                            text = stringResource(R.string.achievements),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        StandardText(
                            text = stringResource(R.string.view_all),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.clickable {
                                navController.navigate(NavRoutes.ACHIEVEMENTS_SCREEN)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(achievementsList) { achievement ->
                    AchievementsListEntry(achievement = achievement)
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
