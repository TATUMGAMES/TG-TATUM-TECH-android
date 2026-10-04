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
package com.tatumgames.tatumtech.android.ui.components.screens.rating

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.analytics.AnalyticsService
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.OutlinedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.common.TitleText
import com.tatumgames.tatumtech.android.ui.components.screens.rating.RatingPromptManager.MAX_RATING
import com.tatumgames.tatumtech.android.ui.theme.Gold
import com.tatumgames.tatumtech.android.ui.theme.Grey400
import com.tatumgames.tatumtech.android.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Lets the selected stars render before the screen closes or hands off to the store. */
private const val RATING_SELECTION_FEEDBACK_MS = 350L

/**
 * "Enjoying The Tatum Tech App?" prompt. Back, the header arrow, and "Not now" always close it
 * without recording a rating.
 *
 * @param trigger One of [com.tatumgames.tatumtech.android.analytics.RatingPromptTriggers].
 */
@Composable
fun RatingScreen(
    navController: NavController,
    trigger: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedRating by rememberSaveable { mutableIntStateOf(0) }
    var submitted by remember { mutableStateOf(false) }

    BackHandler(enabled = submitted) {}

    fun close() {
        navController.popBackStack()
    }

    fun submit(rating: Int) {
        if (submitted) return
        submitted = true
        selectedRating = rating
        scope.launch {
            delay(RATING_SELECTION_FEEDBACK_MS)
            val sendToStore = RatingPromptManager.shouldSendToStore(rating)
            AnalyticsService.rateApp(rating, trigger, sendToStore)
            if (sendToStore) {
                RatingPromptManager.markSentToAppStore(context)
                RatingPromptManager.openAppStoreListing(context)
            }
            close()
        }
    }

    Scaffold(
        topBar = {
            Header(onBackClick = { if (!submitted) close() })
        },
        containerColor = White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.tatumgames_logo),
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )

            TitleText(
                text = stringResource(R.string.rating_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            StandardText(
                text = stringResource(R.string.rating_mission_statement),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            StandardText(
                text = stringResource(R.string.rating_prompt),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center
            )

            RatingStars(
                rating = selectedRating,
                enabled = !submitted,
                onRatingSelected = ::submit
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                text = stringResource(R.string.rating_not_now),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                onClick = { if (!submitted) close() }
            )
        }
    }
}

@Composable
private fun RatingStars(
    rating: Int,
    enabled: Boolean,
    onRatingSelected: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (star in 1..MAX_RATING) {
            val description = stringResource(R.string.rating_star_content_description, star, MAX_RATING)
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (star <= rating) Gold else Grey400,
                modifier = Modifier
                    .size(48.dp)
                    .selectable(
                        selected = star <= rating,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onRatingSelected(star) }
                    )
                    .semantics { contentDescription = description }
            )
        }
    }
}
