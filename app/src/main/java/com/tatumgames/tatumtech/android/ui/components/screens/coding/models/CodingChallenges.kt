package com.tatumgames.tatumtech.android.ui.components.screens.coding.models

import kotlinx.serialization.Serializable

@Serializable
data class CodingChallenges(
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
