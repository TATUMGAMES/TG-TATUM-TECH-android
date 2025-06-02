package com.tatumgames.tatumtech.android.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatumgames.tatumtech.android.R

@Composable
fun OutlinedButton(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
    borderColor: Color = colorResource(R.color.purple_200),
    textColor: Color = colorResource(R.color.purple_200),
    backgroundColor: Color = colorResource(R.color.white),
    onClick: () -> Unit
) {
    val cornerShapeDp = 8.dp
    Box(
        modifier = modifier
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerShapeDp)
            )
            // match border shape (inner button background)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(cornerShapeDp)
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = textStyle,
            color = textColor,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun RoundedButton(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
    backgroundColor: Color = colorResource(R.color.purple_200),
    textColor: Color = colorResource(R.color.white),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .border(
                // no border for solid button
                width = 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = style,
            color = textColor
        )
    }
}
