package com.tatumgames.tatumtech.android.ui.components.screens.models

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

data class NotificationItem(
    val icon: ImageVector? = null,
    @DrawableRes val iconResId: Int? = null,
    val title: String,
    val description: String
)
