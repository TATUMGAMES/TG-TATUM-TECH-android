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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.FeatureCard
import com.tatumgames.tatumtech.android.ui.components.common.Notification
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.utils.MockData.getDummyNotifications

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
            // Top Title
            StandardText(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Greeting (static for now, dynamic later)
            val name = "Leonard" // TODO: Fetch from user profile repository
            val greetingText = if (name.isNotBlank()) {
                stringResource(id = R.string.greeting_with_name, name)
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
                            image = painterResource(id = R.drawable.upcoming_events), // Icons.Default.DateRange,
                            text = stringResource(R.string.upcoming_events),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(NavRoutes.UPCOMING_EVENTS_SCREEN) }
                        )
                        FeatureCard(
                            image = painterResource(id = R.drawable.coding_challenges), // Icons.Default.Build,
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
                            image = painterResource(id = R.drawable.my_timeline), // Icons.Default.Info,
                            text = stringResource(R.string.my_timeline),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(NavRoutes.MY_TIMELINE_SCREEN) }
                        )
                        FeatureCard(
                            image = painterResource(id = R.drawable.donate), // Icons.Default.FavoriteBorder,
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
                            image = painterResource(id = R.drawable.community), // Icons.Default.Info,
                            text = stringResource(R.string.community),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(NavRoutes.COMMUNITY_SCREEN) }
                        )
                        FeatureCard(
                            image = painterResource(id = R.drawable.scanner), // Icons.Default.FavoriteBorder,
                            text = stringResource(R.string.scanner),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(NavRoutes.SCANNER_SCREEN) }
                        )
                    }

                    FeatureCard(
                        image = painterResource(id = R.drawable.stats), // Icons.Default.ShoppingCart,
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
                val context = LocalContext.current
                val notifications = getDummyNotifications(context)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    notifications.forEach { notification ->
                        val painter = notification.iconResId?.let { painterResource(id = it) }
                        Notification(
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
}
