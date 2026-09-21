/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package com.tatumgames.tatumtech.android.ui.components.screens.coding

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes

@Composable
fun CodingChallengesScreen(navController: NavController) {
    ChallengeQuizScreen(
        navController = navController,
        titleResId = R.string.title_coding_challenges,
        languages = listOf("Kotlin", "JavaScript", "Python", "Java", "C#"),
        quizRoute = NavRoutes.CODING_CHALLENGES_SCREEN
    )
}

@Composable
fun AiLlmChallengesScreen(navController: NavController) {
    ChallengeQuizScreen(
        navController = navController,
        titleResId = R.string.title_ai_llm_challenges,
        languages = listOf("AI/LLM"),
        quizRoute = NavRoutes.AI_LLM_CHALLENGES_SCREEN
    )
}

@Composable
fun LeetCodeChallengesScreen(navController: NavController) {
    ChallengeQuizScreen(
        navController = navController,
        titleResId = R.string.title_leet_code_challenges,
        languages = listOf("LeetCode"),
        quizRoute = NavRoutes.LEET_CODE_CHALLENGES_SCREEN,
        showLeetTeachingUi = true
    )
}

@Composable
fun MockInterviewChallengesScreen(navController: NavController) {
    ChallengeQuizScreen(
        navController = navController,
        titleResId = R.string.title_mock_interview_challenges,
        languages = listOf("MockInterview"),
        quizRoute = NavRoutes.MOCK_INTERVIEW_CHALLENGES_SCREEN
    )
}
