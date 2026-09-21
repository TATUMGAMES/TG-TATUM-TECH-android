package com.tatumgames.tatumtech.android.ui.components.common

import android.content.Intent
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.net.toUri
import java.util.regex.Pattern

/**
 * Composable for displaying text with automatically detected clickable URLs.
 * 
 * This component scans the provided text for URL patterns and makes them clickable.
 * When a URL is tapped, it opens the link in the device's default browser.
 * URLs are styled with primary color, underline, and medium font weight for
 * clear visual distinction from regular text.
 * 
 * @param text The text content that may contain URLs (supports http/https protocols)
 * @param style The text style to apply to the entire text content
 * @param modifier Modifier for the composable layout
 * 
 * @author Tatum Games, LLC
 * @since 1.0.0
 */
@Composable
fun ClickableText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val urlPattern = Pattern.compile(
        "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)",
        Pattern.CASE_INSENSITIVE
    )

    val annotatedString = buildAnnotatedString {
        val matcher = urlPattern.matcher(text)
        var lastEnd = 0

        while (matcher.find()) {
            // Add text before the URL
            if (matcher.start() > lastEnd) {
                append(text.substring(lastEnd, matcher.start()))
            }

            // Add the URL with clickable annotation
            val url = matcher.group()
            pushStringAnnotation(
                tag = "URL",
                annotation = url
            )
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append(url)
            }
            pop()

            lastEnd = matcher.end()
        }

        // Add remaining text
        if (lastEnd < text.length) {
            append(text.substring(lastEnd))
        }
    }

    ClickableText(
        text = annotatedString,
        style = style,
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            ).firstOrNull()?.let { annotation ->
                val intent = Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                context.startActivity(intent)
            }
        }
    )
}
