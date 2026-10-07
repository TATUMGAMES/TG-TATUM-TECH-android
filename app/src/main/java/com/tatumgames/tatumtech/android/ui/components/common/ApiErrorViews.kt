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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.framework.android.http.response.ApiError

/**
 * Explains a failed API call in the standard dialog. Only dismissed through its buttons, so the
 * user can't miss it.
 *
 * Retryable failures offer "Try Again" when [onRetry] is given; the user starts the retry, the
 * request is never repeated automatically. [onRetry] should clear the error and resubmit.
 */
@Composable
fun ApiErrorDialog(
    error: ApiError,
    operation: ApiOperation,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    val presentation = presentApiError(error, operation)
    val retry = onRetry?.takeIf { presentation.canRetry }
    StandardAlertDialog(
        title = stringResource(presentation.title),
        description = presentation.serverMessage ?: stringResource(presentation.message),
        confirmButtonText = stringResource(if (retry != null) R.string.try_again else R.string.ok),
        onConfirm = retry ?: onDismiss,
        dismissButtonText = if (retry != null) stringResource(R.string.cancel) else null,
        onDismiss = if (retry != null) onDismiss else null
    )
}

/**
 * Inline replacement for content that failed to load, with a "Try Again" button. Loads are
 * read-only, so retrying is always offered.
 */
@Composable
fun ApiErrorState(
    error: ApiError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    operation: ApiOperation = ApiOperation.LOAD_CONTENT
) {
    val presentation = presentApiError(error, operation)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StandardText(
            text = stringResource(presentation.title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        StandardText(
            text = presentation.serverMessage ?: stringResource(presentation.message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        RoundedButton(
            text = stringResource(R.string.try_again),
            modifier = Modifier.fillMaxWidth(),
            onClick = onRetry
        )
    }
}
