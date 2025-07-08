package com.tatumgames.tatumtech.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.tatumgames.tatumtech.android.R
import androidx.compose.material3.Typography

val LightColorScheme = lightColorScheme(
    primary = Purple200,
    onPrimary = White,
    secondary = Teal200,
    onSecondary = Black,
    background = White,
    surface = White,
    onBackground = Black,
    onSurface = Black
)

@Composable
fun TatumTechTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}
