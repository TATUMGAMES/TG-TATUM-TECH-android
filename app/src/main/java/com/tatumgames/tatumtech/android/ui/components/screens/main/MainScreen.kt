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
package com.tatumgames.tatumtech.android.ui.components.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.entity.UserEntity
import com.tatumgames.tatumtech.android.database.repository.UserDatabaseRepository
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.utils.CodingChallengesImporter
import com.tatumgames.tatumtech.android.utils.MockData.getDummyNotifications
import com.tatumgames.tatumtech.android.utils.Utils.generateAnonymousId
import com.tatumgames.tatumtech.android.utils.Utils.getUserNameOrAnonymous
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        navController = rememberNavController()
    )
}

@Composable
fun MainScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val userRepository = remember { UserDatabaseRepository(db.userDao()) }
    var userName by remember { mutableStateOf("") }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // Refresh user data when screen becomes active
    LaunchedEffect(Unit) {
        refreshTrigger++
    }
    
    // Initialize user data and coding challenges on first launch
    LaunchedEffect(Unit, refreshTrigger) {
        // Initialize user data
        if (!userRepository.userExists()) {
            val anonymousId = generateAnonymousId()
            val newUser = UserEntity(
                anonymousId = anonymousId,
                firstName = null,
                lastName = null,
                name = anonymousId,
                email = null
            )
            userRepository.insertUser(newUser)
            userName = anonymousId
        } else {
            val currentUser = userRepository.getCurrentUser()
            userName = getUserNameOrAnonymous(currentUser)
        }
        
        // Import coding challenges from assets if database is empty
        CodingChallengesImporter.importFromAssetsIfDbEmpty(context)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            },
            containerColor = Color(0xFFF0F0F0)
        ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Top Title with Hamburger Menu
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
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.menu),
                    modifier = Modifier
                        .clickable { isDrawerOpen = true }
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Greeting with dynamic user name
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

            // Scrollable content below
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Feature Cards
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FeatureCard(
                            image = painterResource(id = R.drawable.upcoming_events),
                            text = stringResource(R.string.upcoming_events),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(NavRoutes.UPCOMING_EVENTS_SCREEN) }
                        )
                        FeatureCard(
                            image = painterResource(id = R.drawable.coding_challenges),
                            text = stringResource(R.string.coding),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(NavRoutes.CODING_CHALLENGES_SCREEN) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FeatureCard(
                            image = painterResource(id = R.drawable.my_timeline),
                            text = stringResource(R.string.my_timeline),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(NavRoutes.MY_TIMELINE_SCREEN) }
                        )
                        FeatureCard(
                            image = painterResource(id = R.drawable.donate),
                            text = stringResource(R.string.donate),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(NavRoutes.DONATE_SCREEN) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FeatureCard(
                            image = painterResource(id = R.drawable.community),
                            text = stringResource(R.string.community),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(NavRoutes.COMMUNITY_SCREEN) }
                        )
                        FeatureCard(
                            image = painterResource(id = R.drawable.scanner),
                            text = stringResource(R.string.scanner),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(NavRoutes.SCANNER_SCREEN) }
                        )
                    }

                    FeatureCard(
                        image = painterResource(id = R.drawable.stats),
                        text = stringResource(R.string.stats),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate(NavRoutes.STATS_SCREEN) }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Notifications Title
                StandardText(
                    text = stringResource(id = R.string.recent_notifications),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notifications List
                val notifications = getDummyNotifications(context)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
        }
        
        // Right-side drawer with overlay - positioned above everything
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
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { isDrawerOpen = false }
                )
                
                // Right-side drawer panel (¾ screen width from right)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(LocalConfiguration.current.screenWidthDp.dp * 0.75f)
                        .align(Alignment.CenterEnd)
                        .background(Color.White)
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
