package com.tatumgames.tatumtech.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
