package com.tatumgames.tatumtech.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.tatumgames.tatumtech.android.R
import androidx.compose.material3.Typography

@Composable
fun TatumTechTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = colorResource(R.color.purple_200),
            onPrimary = Color.White,
        ),
        typography = Typography(),
        content = content
    )
}
