package com.tatumgames.tatumtech.android.ui.components.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.ui.components.common.Header
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MyTimelineScreen(navController: NavController) {
    var filter by remember { mutableStateOf(TimelineFilter.WEEK) }
    val timelineItems = remember { getMockTimelineItems(filter) }

    Scaffold(
        topBar = {
            Header(text = "My Timeline", onBackClick = { navController.popBackStack() })
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TimelineFilterBar(filter = filter, onFilterChange = { filter = it })
            Spacer(modifier = Modifier.height(8.dp))
            TimelineList(items = timelineItems)
        }
    }
}

enum class TimelineFilter { WEEK, MONTH }

@Composable
fun TimelineFilterBar(filter: TimelineFilter, onFilterChange: (TimelineFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        FilterChip(
            selected = filter == TimelineFilter.WEEK,
            onClick = { onFilterChange(TimelineFilter.WEEK) },
            label = { Text("Last Week") }
        )
        Spacer(modifier = Modifier.width(12.dp))
        FilterChip(
            selected = filter == TimelineFilter.MONTH,
            onClick = { onFilterChange(TimelineFilter.MONTH) },
            label = { Text("Last Month") }
        )
    }
}

@Composable
fun TimelineList(items: List<TimelineItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(items) { item ->
            AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn(animationSpec = tween(500)),
                exit = androidx.compose.animation.fadeOut(animationSpec = tween(500))
            ) {
                TimelineEntry(item)
            }
        }
    }
}

@Composable
fun TimelineEntry(item: TimelineItem) {
    Row(verticalAlignment = Alignment.Top) {
        // Timeline indicator
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(item.color, shape = MaterialTheme.shapes.small)
            )
            if (!item.isLast) {
                Spacer(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .background(Color.LightGray)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        // Content
        Column {
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(item.description, color = Color.Gray, fontSize = 14.sp)
            Text(item.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")), fontSize = 12.sp, color = Color(0xFF6200EE))
        }
    }
}

data class TimelineItem(
    val title: String,
    val description: String,
    val date: LocalDateTime,
    val color: Color,
    val isLast: Boolean = false
)

fun getMockTimelineItems(filter: TimelineFilter): List<TimelineItem> {
    val now = LocalDateTime.now()
    val base = listOf(
        TimelineItem(
            title = "Registered for Game Dev Summit",
            description = "You registered for the Game Dev Summit event.",
            date = now.minusDays(1),
            color = Color(0xFF6200EE)
        ),
        TimelineItem(
            title = "Completed 5 Coding Challenges",
            description = "You completed your daily coding challenges.",
            date = now.minusDays(2),
            color = Color(0xFF03DAC5)
        ),
        TimelineItem(
            title = "Checked in at Workshop Y",
            description = "QR scan successful at Workshop Y.",
            date = now.minusDays(3),
            color = Color(0xFFFFC107)
        ),
        TimelineItem(
            title = "Won Giveaway Z",
            description = "You won a prize in the event giveaway!",
            date = now.minusDays(5),
            color = Color(0xFFE91E63)
        ),
        TimelineItem(
            title = "Attended event X",
            description = "You attended event X.",
            date = now.minusDays(6),
            color = Color(0xFF2196F3)
        )
    )
    val filtered = when (filter) {
        TimelineFilter.WEEK -> base.filter { it.date.isAfter(now.minusWeeks(1)) }
        TimelineFilter.MONTH -> base.filter { it.date.isAfter(now.minusMonths(1)) }
    }
    return filtered.mapIndexed { idx, item ->
        if (idx == filtered.lastIndex) item.copy(isLast = true) else item
    }
} 