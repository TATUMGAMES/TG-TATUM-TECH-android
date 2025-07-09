package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.screens.models.Attendee
import com.tatumgames.tatumtech.android.utils.Utils
import com.tatumgames.tatumtech.android.utils.Utils.generateColorFromString

@Composable
fun AttendeeDisplay(
    attendee: Attendee,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val attendeeColor = generateColorFromString(attendee.userId)
    
    // Determine if this attendee should show initials
    val shouldShowInitials = attendee.profileImage == "initials" || attendee.userId.hashCode() % 5 == 0
    
    if (shouldShowInitials) {
        // Show initials
        val initials = attendee.name.split(" ").take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
        Box(
            modifier = modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(attendeeColor)
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        // Show profile image
        val drawableId = when (attendee.profileImage) {
            "profile_male_placeholder_01" -> R.drawable.profile_male_placeholder_01
            "profile_male_placeholder_02" -> R.drawable.profile_male_placeholder_02
            "profile_male_placeholder_03" -> R.drawable.profile_male_placeholder_03
            "profile_female_placeholder_01" -> R.drawable.profile_female_placeholder_01
            "profile_female_placeholder_02" -> R.drawable.profile_female_placeholder_02
            "profile_female_placeholder_03" -> R.drawable.profile_female_placeholder_03
            else -> R.drawable.profile_male_placeholder_01
        }
        
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = null,
            modifier = modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(1.dp, attendeeColor, CircleShape),
            contentScale = ContentScale.Crop
        )
    }
} 
