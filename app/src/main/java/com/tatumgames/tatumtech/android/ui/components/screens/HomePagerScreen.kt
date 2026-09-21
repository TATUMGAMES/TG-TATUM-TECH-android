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
package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.PagerTabBar
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.layout.HorizontalPagerLayout
import com.tatumgames.tatumtech.android.ui.components.screens.main.NotificationBar
import com.tatumgames.tatumtech.android.ui.components.screens.main.UserProfileDrawer
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.ui.viewmodels.HomePagerViewModel
import com.tatumgames.tatumtech.android.ui.viewmodels.factory.HomePagerViewModelFactory
import com.tatumgames.tatumtech.android.utils.CodingChallengesImporter
import com.tatumgames.tatumtech.android.utils.MockData.getDummyNotifications
import kotlinx.coroutines.launch

/**
 * Main home screen with horizontal pager functionality.
 * Replicates MainScreen's exact UI structure, but with FeatureCards organized in a horizontal pager.
 * Only the FeatureCard grid area is inside the pager - all other elements (title, greeting, tabs, notifications, bottom bar) are static.
 *
 * @param navController Navigation controller for screen navigation
 */
@Composable
fun HomePagerScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: HomePagerViewModel = viewModel(
        factory = HomePagerViewModelFactory(context)
    )

    var userName by remember { mutableStateOf("") }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var notificationsExpanded by remember { mutableStateOf(true) }
    val notificationsScroll = rememberScrollState()

    val categories = viewModel.pagerCategories
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { categories.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // Refresh user data when screen becomes active; sync quiz questions (default home).
    LaunchedEffect(Unit) {
        viewModel.refreshUser()
        CodingChallengesImporter.syncCodingQuestionsFromAssets(context.applicationContext)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            },
            containerColor = White
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Top Title Row with Hamburger Menu (STATIC - outside pager)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StandardText(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    // Hamburger Menu Icon
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.menu),
                        modifier = Modifier
                            .clickable { isDrawerOpen = true }
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Greeting with dynamic user name (STATIC - outside pager)
                val greetingText = if (userName.isNotBlank()) {
                    stringResource(id = R.string.greeting_with_name, userName)
                } else {
                    stringResource(id = R.string.greeting_generic)
                }

                StandardText(
                    text = greetingText,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Pager Tab Bar (STATIC - outside pager)
                PagerTabBar(
                    categories = categories,
                    selectedIndex = pagerState.currentPage,
                    onTabSelected = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal Pager (ONLY FeatureCard grids inside here)
                HorizontalPagerLayout(
                    categories = categories,
                    getItemsForCategory = { viewModel.getItemsForCategory(it) },
                    navController = navController,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    pagerState = pagerState
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Collapsible Recent Notifications (full-width header tap target)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            notificationsExpanded = !notificationsExpanded
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StandardText(
                        text = stringResource(id = R.string.recent_notifications),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Icon(
                        imageVector = if (notificationsExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = stringResource(
                            if (notificationsExpanded) {
                                R.string.cd_notifications_section_expanded
                            } else {
                                R.string.cd_notifications_section_collapsed
                            }
                        ),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                AnimatedVisibility(
                    visible = notificationsExpanded,
                    enter = expandVertically(animationSpec = tween(220)) + fadeIn(tween(200)),
                    exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(tween(180))
                ) {
                    val notifications = getDummyNotifications(context)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(notificationsScroll),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        notifications.forEach { notification ->
                            val painter = notification.iconResId?.let { painterResource(id = it) }
                            NotificationBar(
                                icon = notification.icon,
                                image = painter,
                                title = notification.title,
                                description = notification.description
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Right-side drawer with overlay - positioned above everything (STATIC - outside pager)
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Semi-transparent overlay (¼ screen width from left)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Black.copy(alpha = 0.3f))
                        .clickable { isDrawerOpen = false }
                )

                // Right-side drawer panel (¾ screen width from right)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(LocalConfiguration.current.screenWidthDp.dp * 0.75f)
                        .align(Alignment.CenterEnd)
                        .background(White)
                        .clickable(enabled = false) {} // Disable close on click inside
                ) {
                    UserProfileDrawer(
                        onClose = { isDrawerOpen = false },
                        navController = navController
                    )
                }
            }
        }
    }
}
