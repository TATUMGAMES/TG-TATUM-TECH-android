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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.theme.TatumTechTheme

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    TatumTechTheme {
        Header()
    }
}

@Composable
fun Header(
    modifier: Modifier = Modifier,
    text: String = "",
    onBackClick: () -> Unit = {}
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            // this adds padding equal to the height of the status bar
            .statusBarsPadding()
    ) {
        val (backButton, title, divider) = createRefs()

        // back button
        Image(
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .clickable { onBackClick() }
                .constrainAs(backButton) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            painter = painterResource(id = R.drawable.back_arrow),
            contentDescription = null
        )

        // title
        TitleText(
            modifier = Modifier
                .constrainAs(title) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            text = text,
            color = colorResource(R.color.black),
            style = MaterialTheme.typography.headlineSmall.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        )

        // divider
        HorizontalDivider(
            modifier = Modifier
                .constrainAs(divider) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(title.bottom)
                    width = Dimension.fillToConstraints
                },
            thickness = 0.5.dp,
            color = colorResource(R.color.black)
        )
    }
}
