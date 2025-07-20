package com.tatumgames.tatumtech.android.ui.components.screens.models

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeQuestion(
    val id: String,
    val createdAt: String,
    val language: String,
    val level: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val platform: String
) 
