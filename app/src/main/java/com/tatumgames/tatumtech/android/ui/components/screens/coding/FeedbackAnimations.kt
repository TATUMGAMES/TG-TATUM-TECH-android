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
package com.tatumgames.tatumtech.android.ui.components.screens.coding

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tatumgames.tatumtech.android.ui.theme.Gold
import com.tatumgames.tatumtech.android.ui.theme.PartnerContactBlue
import com.tatumgames.tatumtech.android.ui.theme.Purple200
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.ui.theme.SuccessGreen
import com.tatumgames.tatumtech.android.ui.theme.Teal200
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Plays an animated GIF resource once and holds its last frame, using the platform decoder
 * (API 28+). Below API 28, when [animate] is false, or if decoding fails, shows [fallbackRes].
 * Playback stops when this leaves the composition.
 */
@Composable
internal fun AnimatedGifImage(
    @DrawableRes gifRes: Int,
    @DrawableRes fallbackRes: Int,
    contentDescription: String,
    animate: Boolean,
    modifier: Modifier = Modifier
) {
    val drawable = if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        rememberAnimatedImageDrawable(gifRes)
    } else {
        null
    }
    if (drawable == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        Image(
            painter = painterResource(fallbackRes),
            contentDescription = contentDescription,
            modifier = modifier
        )
        return
    }
    PlayOnce(drawable)
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageDrawable(drawable)
            }
        },
        update = { it.contentDescription = contentDescription },
        modifier = modifier
    )
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
private fun rememberAnimatedImageDrawable(@DrawableRes gifRes: Int): AnimatedImageDrawable? {
    val resources = LocalContext.current.resources
    return remember(gifRes) {
        runCatching {
            ImageDecoder.decodeDrawable(ImageDecoder.createSource(resources, gifRes))
        }.getOrNull() as? AnimatedImageDrawable
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
private fun PlayOnce(drawable: AnimatedImageDrawable) {
    DisposableEffect(drawable) {
        drawable.repeatCount = 0
        drawable.start()
        onDispose { drawable.stop() }
    }
}

private class ConfettiPiece(
    val angle: Double,
    val speed: Float,
    val size: Size,
    val color: Color,
    val spin: Float,
    val startRotation: Float
)

private val CONFETTI_COLORS = listOf(Purple200, Purple500, SuccessGreen, Teal200, Gold, PartnerContactBlue)

/**
 * One short burst of paper confetti from [origin] (in this composable's coordinates): pieces
 * fly up and outward, fall under gravity while spinning, and fade out. Draws nothing once the
 * burst has finished, and never handles touches.
 */
@Composable
internal fun ConfettiBurst(
    origin: Offset,
    modifier: Modifier = Modifier,
    pieceCount: Int = 36,
    durationMs: Int = 1_600
) {
    val pieces = remember(pieceCount) {
        val random = Random(System.nanoTime())
        List(pieceCount) {
            ConfettiPiece(
                // Mostly upward: -165° to -15°.
                angle = Math.toRadians(random.nextDouble(-165.0, -15.0)),
                speed = random.nextFloat() * 0.55f + 0.45f,
                size = Size(random.nextFloat() * 5f + 6f, random.nextFloat() * 4f + 3f),
                color = CONFETTI_COLORS[random.nextInt(CONFETTI_COLORS.size)],
                spin = random.nextFloat() * 720f - 360f,
                startRotation = random.nextFloat() * 360f
            )
        }
    }
    val progress = remember { Animatable(0f) }
    var finished by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMs, easing = LinearEasing))
        finished = true
    }
    if (finished) return

    Canvas(modifier = modifier) {
        val t = progress.value
        val travel = 170.dp.toPx()
        val gravity = 420.dp.toPx()
        val alpha = if (t < 0.6f) 1f else (1f - (t - 0.6f) / 0.4f).coerceIn(0f, 1f)
        val density = this.density
        pieces.forEach { piece ->
            // Velocity decays (air drag) while gravity pulls down.
            val distance = travel * piece.speed * (1f - (1f - t) * (1f - t))
            val x = origin.x + (cos(piece.angle) * distance).toFloat()
            val y = origin.y + (sin(piece.angle) * distance).toFloat() + gravity * t * t * 0.5f
            val w = piece.size.width * density
            val h = piece.size.height * density
            rotate(piece.startRotation + piece.spin * t, pivot = Offset(x, y)) {
                drawRect(
                    color = piece.color.copy(alpha = alpha),
                    topLeft = Offset(x - w / 2f, y - h / 2f),
                    size = Size(w, h)
                )
            }
        }
    }
}
