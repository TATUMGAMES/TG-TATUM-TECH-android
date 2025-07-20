package com.tatumgames.tatumtech.android.ui.components.screens.models

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

data class Notification(
    val icon: ImageVector? = null,
    @DrawableRes val iconResId: Int? = null,
    val title: String,
    val description: String
)
