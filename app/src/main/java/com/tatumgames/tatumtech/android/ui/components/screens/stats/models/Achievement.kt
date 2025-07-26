package com.tatumgames.tatumtech.android.ui.components.screens.stats.models

import androidx.annotation.DrawableRes

data class Achievement(
    val tier: Int,
    val name: String,
    val description: String,
    @DrawableRes val iconRes: Int,
    val unlocked: Boolean = false
)
