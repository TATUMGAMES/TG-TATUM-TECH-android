package com.tatumgames.tatumtech.android.ui.components.screens

import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.ui.components.common.Header
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.CircleShape
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    // Mock stats
    val eventsAttended = 8
    val workshopsRegistered = 5
    val challengesCompleted = 23
    val percentCorrect = 87
    val achievements = listOf("First Challenge!", "5 Days Streak", "Workshop Winner")
    val workshopBreakdown = listOf(3f, 1f, 1f) // e.g. [Tech, Art, Design]
    val workshopLabels = listOf("Tech", "Art", "Design")

    // Animated counters
    var animatedEvents by remember { mutableStateOf(0) }
    var animatedWorkshops by remember { mutableStateOf(0) }
    var animatedChallenges by remember { mutableStateOf(0) }
    var animatedPercent by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        repeat(eventsAttended + 1) {
            animatedEvents = it
            delay(30)
        }
        repeat(workshopsRegistered + 1) {
            animatedWorkshops = it
            delay(30)
        }
        repeat(challengesCompleted + 1) {
            animatedChallenges = it
            delay(15)
        }
        repeat(percentCorrect + 1) {
            animatedPercent = it
            delay(10)
        }
    }

    Scaffold(
        topBar = {
            Header(text = "Stats", onBackClick = { navController.popBackStack() })
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your Progress", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressRing(label = "Events", value = animatedEvents, max = 20, color = Color(0xFF6200EE))
                ProgressRing(label = "Workshops", value = animatedWorkshops, max = 10, color = Color(0xFF03DAC5))
                ProgressRing(label = "Challenges", value = animatedChallenges, max = 30, color = Color(0xFFFFC107))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Percent Correct", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            AnimatedPercentRing(percent = animatedPercent)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Workshops Breakdown", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            BarChartView(data = workshopBreakdown, labels = workshopLabels)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Achievements", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            AchievementsList(achievements)
        }
    }
}

@Composable
fun ProgressRing(label: String, value: Int, max: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            progress = value / max.toFloat(),
            color = color,
            strokeWidth = 8.dp,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("$value", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text(label, fontSize = 13.sp, color = Color.Gray)
    }
}

@Composable
fun AnimatedPercentRing(percent: Int) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
        CircularProgressIndicator(
            progress = percent / 100f,
            color = Color(0xFF388E3C),
            strokeWidth = 10.dp,
            modifier = Modifier.size(100.dp)
        )
        Text("$percent%", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 20.sp)
    }
}

@Composable
fun BarChartView(data: List<Float>, labels: List<String>) {
    val context = LocalContext.current
    val density = LocalDensity.current
//    AndroidView(
//        factory = { ctx ->
//            BarChart(ctx).apply {
//                layoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    (180 * density.density).toInt()
//                )
//                setDrawBarShadow(false)
//                setDrawValueAboveBar(true)
//                description.text = ""
//                legend.isEnabled = false
//                setTouchEnabled(false)
//                setScaleEnabled(false)
//                setPinchZoom(false)
//                setDrawGridBackground(false)
//                val entries = data.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
//                val dataSet = BarDataSet(entries, "").apply {
//                    colors = ColorTemplate.MATERIAL_COLORS.toList()
//                    valueTextColor = AndroidColor.BLACK
//                    valueTextSize = 12f
//                }
//                val barData = BarData(dataSet)
//                barData.barWidth = 0.9f
//                setData(barData)
//                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
//                xAxis.granularity = 1f
//                xAxis.setDrawGridLines(false)
//                xAxis.setDrawAxisLine(false)
//                xAxis.position = XAxis.XAxisPosition.BOTTOM
//                axisLeft.axisMinimum = 0f
//                axisLeft.setDrawGridLines(false)
//                axisRight.isEnabled = false
//                invalidate()
//            }
//        },
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(180.dp)
//            .background(Color.White, RoundedCornerShape(12.dp))
//            .padding(8.dp)
//    )
}

@Composable
fun AchievementsList(achievements: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        achievements.forEach { achievement ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFFFFC107), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(achievement, fontSize = 15.sp)
            }
        }
    }
} 
