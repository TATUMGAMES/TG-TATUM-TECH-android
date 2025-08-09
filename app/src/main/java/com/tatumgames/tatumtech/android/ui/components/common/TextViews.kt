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

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.constants.Constants.URL_PRIVACY_POLICY
import com.tatumgames.tatumtech.android.constants.Constants.URL_TERMS

@Composable
fun StandardText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = colorResource(R.color.black)
) {
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        color = color,
        modifier = modifier
    )
}

@Composable
fun TitleText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = colorResource(R.color.black)
) {
    Text(
        text = text,
        style = style,
        textAlign = textAlign,
        color = color,
        modifier = modifier.padding(8.dp)
    )
}

@Composable
fun ClickableText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = colorResource(R.color.black),
    onClick: () -> Unit
) {
    val annotatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = color
            )
        ) {
            append(text)
        }
    }

    Text(
        text = annotatedText,
        textAlign = textAlign,
        style = style,
        modifier = modifier.clickable { onClick() }
    )
}

@Composable
fun LinkifyText(
    modifier: Modifier = Modifier,
    fullText: String,
    linkText: String,
    url: String,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val context = LocalContext.current

    val annotatedText = buildAnnotatedString {
        append(fullText)
        val start = fullText.indexOf(linkText)
        val end = start + linkText.length
        addStyle(
            SpanStyle(
                color = color,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold
            ),
            start = start,
            end = end
        )
        addStringAnnotation(
            tag = "URL",
            annotation = url,
            start = start,
            end = end
        )
    }

    Text(
        text = annotatedText,
        style = style,
        textAlign = textAlign,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures {
                annotatedText.getStringAnnotations(
                    tag = "URL",
                    start = 0,
                    end = annotatedText.length
                ).firstOrNull()?.let { annotation ->
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                    )
                }
            }
        })
}

@Composable
fun TermsAndPrivacyText(
    modifier: Modifier = Modifier,
    textColor: Color = colorResource(id = R.color.purple_500)
) {
    val context = LocalContext.current

    val terms = stringResource(id = R.string.terms)
    val privacyPolicy = stringResource(id = R.string.privacy_policy)
    val termsAndPrivacyPolicy = stringResource(id = R.string.terms_and_policy, terms, privacyPolicy)

    val annotatedText = buildAnnotatedString {
        // use placeholders and string resource for terms and policy
        append(termsAndPrivacyPolicy)
        val startA = termsAndPrivacyPolicy.indexOf(terms)
        val endA = termsAndPrivacyPolicy.indexOf(terms) + terms.length
        val startB = termsAndPrivacyPolicy.indexOf(privacyPolicy)
        val endB = termsAndPrivacyPolicy.indexOf(privacyPolicy) + privacyPolicy.length

        // add styles for "Terms"
        addStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold
            ),
            start = startA,
            end = endA
        )
        addStringAnnotation(
            tag = terms,
            annotation = URL_TERMS,
            start = startA,
            end = endA
        )

        // add styles for "Privacy Policy"
        addStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold
            ),
            start = startB,
            end = endB
        )
        addStringAnnotation(
            tag = privacyPolicy,
            annotation = URL_PRIVACY_POLICY,
            start = startB,
            end = endB
        )
    }

    Text(
        text = annotatedText,
        style = TextStyle(fontSize = 14.sp, lineHeight = 24.sp),
        textAlign = TextAlign.Center,
        color = textColor,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                annotatedText.getStringAnnotations(
                    tag = "URL",
                    start = offset.x.toInt(),
                    end = offset.x.toInt()
                ).firstOrNull()?.let { annotation ->
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                    )
                }
            }
        }
    )
}
