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
package com.tatumgames.tatumtech.android.ui.components.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.repository.TimelineDatabaseRepository
import com.tatumgames.tatumtech.android.enums.TimelineType
import com.tatumgames.tatumtech.android.ui.components.common.AchievementsList
import com.tatumgames.tatumtech.android.ui.components.common.AnimatedPercentRing
import com.tatumgames.tatumtech.android.ui.components.common.BarChartView
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.ProgressRing
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.models.Achievement
import com.tatumgames.tatumtech.android.ui.components.screens.viewmodels.CodingChallengesViewModel
import com.tatumgames.tatumtech.android.ui.theme.Gold
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.ui.theme.Teal200
import com.tatumgames.tatumtech.android.utils.MockData.getAllAchievements
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val timelineRepository = remember { TimelineDatabaseRepository(database.timelineDao()) }

    var eventsAttended by remember { mutableIntStateOf(0) }
    var challengesCompleted by remember { mutableIntStateOf(0) }
    var percentCorrect by remember { mutableIntStateOf(0) }
    var qrCodesScanned by remember { mutableIntStateOf(0) }
    var achievementsUnlocked by remember { mutableIntStateOf(0) }

    var animatedEvents by remember { mutableIntStateOf(0) }
    var animatedChallenges by remember { mutableIntStateOf(0) }
    var animatedQrCodes by remember { mutableIntStateOf(0) }
    var animatedPercent by remember { mutableIntStateOf(0) }

    var achievementsList by remember { mutableStateOf<List<Achievement>>(emptyList()) }

    val challengeViewModel: CodingChallengesViewModel = viewModel(factory = viewModelFactory {
        CodingChallengesViewModel(context.applicationContext as Application)
    })
    val streak by challengeViewModel.currentStreak.collectAsState()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val allTimeline = timelineRepository.getAllTimelineEvents()
            eventsAttended =
                allTimeline.count { it.type == TimelineType.EVENT_REGISTRATION.typeValue }
            challengesCompleted =
                allTimeline.count { it.type == TimelineType.CHALLENGE_COMPLETION.typeValue }
            qrCodesScanned = allTimeline.count { it.type == TimelineType.QR_SCAN.typeValue }

            val allAchievements = getAllAchievements(context)
            achievementsList = allAchievements.map { ach ->
                ach.copy(
                    unlocked = when (ach.name) {
                        context.getString(R.string.achievement_desc_first_step) -> eventsAttended >= 1
                        context.getString(R.string.achievement_title_committed) -> eventsAttended >= 3
                        context.getString(R.string.achievement_title_event_veteran) -> eventsAttended >= 5
                        context.getString(R.string.achievement_title_coder_in_training) -> challengesCompleted >= 5
                        context.getString(R.string.achievement_title_leveling_up) -> challengesCompleted >= 15
                        context.getString(R.string.achievement_title_beginner_master) -> challengesCompleted >= 50
                        context.getString(R.string.achievement_title_problem_solver) -> challengesCompleted >= 5
                        context.getString(R.string.achievement_title_code_climber) -> challengesCompleted >= 15
                        context.getString(R.string.achievement_title_intermediate_champ) -> challengesCompleted >= 50
                        context.getString(R.string.achievement_title_code_warrior) -> challengesCompleted >= 5
                        context.getString(R.string.achievement_title_algorithm_slayer) -> challengesCompleted >= 15
                        context.getString(R.string.achievement_title_elite_hacker) -> challengesCompleted >= 50
                        context.getString(R.string.achievement_title_qr_curious) -> qrCodesScanned >= 1
                        context.getString(R.string.achievement_title_qr_explorer) -> qrCodesScanned >= 3
                        context.getString(R.string.achievement_title_qr_adventurer) -> qrCodesScanned >= 5
                        context.getString(R.string.achievement_title_qr_hunter) -> qrCodesScanned >= 10
                        context.getString(R.string.achievement_title_qr_master) -> qrCodesScanned >= 20
                        context.getString(R.string.achievement_title_qr_legend) -> qrCodesScanned >= 50
                        context.getString(R.string.achievement_title_heart_giver) -> allTimeline.any {
                            it.type == TimelineType.DONATION.typeValue
                        }

                        else -> false
                    }
                )
            }
            achievementsUnlocked = achievementsList.count { it.unlocked }
            challengeViewModel.updateCurrentStreak()
        }

        repeat(eventsAttended + 1) {
            animatedEvents = it
            delay(30)
        }
        repeat(challengesCompleted + 1) {
            animatedChallenges = it
            delay(15)
        }
        repeat(qrCodesScanned + 1) {
            animatedQrCodes = it
            delay(15)
        }
        repeat(percentCorrect + 1) {
            animatedPercent = it
            delay(10)
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
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StandardText(
                text = stringResource(R.string.your_progress),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

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
            StandardText(
                text = stringResource(R.string.percent_correct),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            AnimatedPercentRing(percent = animatedPercent)

            Spacer(modifier = Modifier.height(24.dp))
            StandardText(
                text = stringResource(R.string.current_streak_days, streak),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.height(24.dp))
            StandardText(
                text = stringResource(R.string.achievements_unlocked, achievementsUnlocked),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.height(24.dp))
            StandardText(
                text = stringResource(R.string.category_breakdown_coming_soon),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )

            BarChartView()
            Spacer(modifier = Modifier.height(24.dp))
            StandardText(
                text = stringResource(R.string.achievements),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            AchievementsList(achievementsList)
        }
    }
}
