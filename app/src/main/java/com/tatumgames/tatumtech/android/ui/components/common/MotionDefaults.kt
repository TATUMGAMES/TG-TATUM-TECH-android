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
package com.tatumgames.tatumtech.android.ui.components.common

import android.provider.Settings
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Shared motion vocabulary: short fade/translation for navigation and content reveals.
 * Respects system animator duration scale (0 = animations disabled).
 */
object MotionDefaults {
    const val NAV_MS = 220
    const val CONTENT_MS = 200
    const val FEEDBACK_ENTER_MS = 280
    const val FEEDBACK_EXIT_MS = 180

    @Composable
    fun animationsEnabled(): Boolean {
        val context = LocalContext.current
        return remember(context) {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) != 0f
        }
    }

    fun <T> durationSpec(enabled: Boolean, durationMs: Int): FiniteAnimationSpec<T> =
        tween(if (enabled) durationMs else 0)

    fun navEnter(enabled: Boolean) =
        fadeIn(animationSpec = durationSpec(enabled, NAV_MS)) +
            slideInHorizontally(
                animationSpec = durationSpec(enabled, NAV_MS),
                initialOffsetX = { if (enabled) it / 24 else 0 }
            )

    fun navExit(enabled: Boolean) =
        fadeOut(animationSpec = durationSpec(enabled, NAV_MS - 40))

    fun navPopEnter(enabled: Boolean) =
        fadeIn(animationSpec = durationSpec(enabled, NAV_MS))

    fun navPopExit(enabled: Boolean) =
        fadeOut(animationSpec = durationSpec(enabled, NAV_MS - 40)) +
            slideOutHorizontally(
                animationSpec = durationSpec(enabled, NAV_MS),
                targetOffsetX = { if (enabled) it / 24 else 0 }
            )
}
