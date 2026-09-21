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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.constants.Constants
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.RoundedButton
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import com.tatumgames.tatumtech.android.utils.Utils.openUrl

@Composable
fun GetYourGameDiscoveredScreen(navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.mikros_dialog_title),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = ScreenScaffoldLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StandardText(
                text = stringResource(R.string.mikros_dialog_body),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            RoundedButton(
                text = stringResource(R.string.mikros_dialog_open_site),
                modifier = Modifier.fillMaxWidth(),
                onClick = { openUrl(context, Constants.URL_MIKROS_DEVELOPER) }
            )
            RoundedButton(
                text = stringResource(R.string.mikros_explainer_video),
                modifier = Modifier.fillMaxWidth(),
                onClick = { openUrl(context, Constants.URL_MIKROS_EXPLAINER_VIDEO) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
