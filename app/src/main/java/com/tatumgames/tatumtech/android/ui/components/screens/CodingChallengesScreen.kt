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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.viewmodels.CodingChallengesViewModel
import com.tatumgames.tatumtech.android.ui.components.screens.viewmodels.factory.CodingChallengesViewModelFactory

@Composable
fun CodingChallengesScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val viewModel: CodingChallengesViewModel = viewModel(
        factory = CodingChallengesViewModelFactory(app)
    )

    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val showSummary by viewModel.showSummary.collectAsState()

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.title_coding_challenges),
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                questions.isEmpty() -> {
                    StandardText(text = stringResource(R.string.no_questions))
                }

                showSummary -> {
                    StandardText(text = stringResource(R.string.challenge_complete))
                }

                else -> {
                    val question = questions.getOrNull(currentIndex)
                    val questionText = question?.question ?: stringResource(R.string.loading)
                    StandardText(
                        text = stringResource(
                            R.string.question_number,
                            currentIndex + 1,
                            questionText
                        )
                    )
                }
            }
        }
    }
}
