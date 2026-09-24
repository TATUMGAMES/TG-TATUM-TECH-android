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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.MotionDefaults
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.coding.models.AnswerFeedback
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.Grey300
import com.tatumgames.tatumtech.android.ui.theme.Purple500
import com.tatumgames.tatumtech.android.ui.theme.Red300
import com.tatumgames.tatumtech.android.ui.theme.SuccessGreen
import com.tatumgames.tatumtech.android.ui.theme.White
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Polished correct/incorrect feedback with explanation and Continue control.
 * Icon + text remain readable without relying solely on color.
 */
@Composable
fun AnswerFeedbackOverlay(
    visible: Boolean,
    feedback: AnswerFeedback,
    correctAnswer: String,
    explanation: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animationsEnabled = MotionDefaults.animationsEnabled()
    val isCorrect = feedback == AnswerFeedback.CORRECT
    val accent = if (isCorrect) {
        SuccessGreen
    } else {
        Red300
    }

    AnimatedVisibility(
        visible = visible && feedback != AnswerFeedback.NONE,
        enter = fadeIn(
            MotionDefaults.durationSpec(
                animationsEnabled,
                MotionDefaults.FEEDBACK_ENTER_MS
            )
        ) +
                scaleIn(
                    animationSpec = MotionDefaults.durationSpec(
                        animationsEnabled,
                        MotionDefaults.FEEDBACK_ENTER_MS
                    ),
                    initialScale = if (animationsEnabled) {
                        0.92f
                    } else {
                        1f
                    }
                ),
        exit = fadeOut(
            MotionDefaults.durationSpec(
                animationsEnabled,
                MotionDefaults.FEEDBACK_EXIT_MS
            )
        ),
        modifier = modifier.fillMaxSize()
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { },
            contentAlignment = Alignment.Center
        ) {
            FeedbackAccentBackdrop(
                accent = accent,
                isCorrect = isCorrect,
                animationsEnabled = animationsEnabled
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .feedbackMotion(isCorrect = isCorrect, animationsEnabled = animationsEnabled),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    FeedbackIcon(
                        isCorrect = isCorrect,
                        animationsEnabled = animationsEnabled
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    StandardText(
                        text = if (isCorrect) {
                            stringResource(R.string.answer_feedback_correct_title)
                        } else {
                            stringResource(R.string.answer_feedback_incorrect_title)
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = accent,
                        textAlign = TextAlign.Center
                    )

                    if (!isCorrect && correctAnswer.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        StandardText(
                            text = stringResource(
                                R.string.answer_feedback_correct_answer,
                                correctAnswer
                            ),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            textAlign = TextAlign.Center
                        )
                    }

                    val explanationText = explanation.trim().ifBlank {
                        stringResource(R.string.answer_feedback_explanation_fallback)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    StandardText(
                        text = explanationText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Grey300)
                            .padding(10.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onContinue,
                        colors = ButtonDefaults.buttonColors(containerColor = Purple500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        StandardText(
                            text = stringResource(R.string.answer_feedback_continue),
                            color = White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackAccentBackdrop(
    accent: Color,
    isCorrect: Boolean,
    animationsEnabled: Boolean
) {
    val pulse = if (animationsEnabled) {
        val transition = rememberInfiniteTransition(label = "feedbackPulse")
        val value by transition.animateFloat(
            initialValue = 0.18f,
            targetValue = 0.32f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
        value
    } else {
        0.22f
    }

    val ringProgress = remember { Animatable(0f) }
    LaunchedEffect(isCorrect, animationsEnabled) {
        ringProgress.snapTo(0f)
        if (animationsEnabled) {
            ringProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(420, easing = FastOutSlowInEasing)
            )
        } else {
            ringProgress.snapTo(1f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.38f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = pulse), Color.Transparent),
                center = center,
                radius = size.minDimension * 0.42f
            ),
            radius = size.minDimension * 0.42f,
            center = center
        )
        drawCircle(
            color = accent.copy(alpha = 0.35f * ringProgress.value),
            radius = size.minDimension * 0.18f * ringProgress.value,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
        if (isCorrect && animationsEnabled && ringProgress.value > 0.15f) {
            val particleProgress = ((ringProgress.value - 0.15f) / 0.85f).coerceIn(0f, 1f)
            val particleCount = 10
            val rnd = Random(42)
            repeat(particleCount) { i ->
                val angle = (i / particleCount.toFloat()) * Math.PI * 2.0 + rnd.nextDouble(0.0, 0.4)
                val dist = size.minDimension * 0.08f + particleProgress * size.minDimension * 0.16f
                val px = center.x + (cos(angle) * dist).toFloat()
                val py = center.y + (sin(angle) * dist).toFloat()
                drawCircle(
                    color = accent.copy(alpha = (1f - particleProgress) * 0.7f),
                    radius = 3.5f * (1f - particleProgress * 0.4f),
                    center = Offset(px, py)
                )
            }
        }
    }
}

@Composable
private fun FeedbackIcon(
    isCorrect: Boolean,
    animationsEnabled: Boolean
) {
    val scale = remember { Animatable(if (animationsEnabled) 0.6f else 1f) }
    LaunchedEffect(isCorrect, animationsEnabled) {
        scale.snapTo(if (animationsEnabled) 0.6f else 1f)
        if (animationsEnabled) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f)
            )
        }
    }

    Image(
        painter = painterResource(
            id = if (isCorrect) {
                R.drawable.coding_challenge_correct
            } else {
                R.drawable.coding_challenge_incorrect
            }
        ),
        contentDescription = if (isCorrect) {
            stringResource(R.string.cd_answer_correct)
        } else {
            stringResource(R.string.cd_answer_incorrect)
        },
        modifier = Modifier
            .size(96.dp)
            .scale(scale.value)
    )
}

@Composable
private fun Modifier.feedbackMotion(
    isCorrect: Boolean,
    animationsEnabled: Boolean
): Modifier {
    if (!animationsEnabled || isCorrect) return this

    val shake = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        val offsets = listOf(0f, -8f, 8f, -5f, 5f, -2f, 0f)
        for (o in offsets) {
            shake.animateTo(
                targetValue = o,
                animationSpec = tween(28, easing = LinearEasing)
            )
        }
    }
    return this.offset { IntOffset(shake.value.roundToInt(), 0) }
}
