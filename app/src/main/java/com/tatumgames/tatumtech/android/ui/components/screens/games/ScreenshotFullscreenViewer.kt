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
package com.tatumgames.tatumtech.android.ui.components.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.ui.utils.GameMediaResolver

/**
 * Fullscreen screenshot carousel. Starts on [initialPage]; X and system back dismiss to Game Details.
 */
@Composable
fun ScreenshotFullscreenViewer(
    screenshots: List<String>,
    initialPage: Int,
    gameTitle: String,
    onDismiss: () -> Unit
) {
    if (screenshots.isEmpty()) return

    val context = LocalContext.current
    val startPage = initialPage.coerceIn(0, screenshots.lastIndex)
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { screenshots.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler(onBack = onDismiss)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(GameMediaResolver.resolve(context, screenshots[page]))
                        .crossfade(200)
                        .build(),
                    contentDescription = stringResource(R.string.games_screenshot_cd, gameTitle),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 48.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(R.drawable.games),
                    error = painterResource(R.drawable.games)
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp)
                    .size(48.dp)
                    .background(Black.copy(alpha = 0.55f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.games_screenshot_viewer_close_cd),
                    tint = White
                )
            }
        }
    }
}
