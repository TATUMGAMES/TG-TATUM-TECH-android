package com.tatumgames.tatumtech.android.ui.components.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tatumgames.tatumtech.android.ui.components.common.Header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext

// Data class for coding challenge question
data class CodingChallengeQuestion(
    val question_id: String,
    val created_at: String,
    val language: String,
    val level: String,
    val question: String,
    val options: List<String>,
    val correct_answer: String,
    val explanation: String,
    val platform: String
)

// Utility to load questions from assets
suspend fun loadQuestionsFromAssets(
    context: Context,
    language: String,
    level: String
): List<CodingChallengeQuestion> = withContext(Dispatchers.IO) {
    val fileName = when (language) {
        "JavaScript" -> "coding_challenges_javascript_beginner.json" // TODO: handle level
        "Python" -> "coding_challenges_python_beginner.json"
        "Kotlin" -> "coding_challenges_kotlin_beginner.json"
        else -> null
    }
    if (fileName == null) return@withContext emptyList()
    val inputStream = context.assets.open(fileName)
    val json = inputStream.bufferedReader().use(BufferedReader::readText)
    val jsonArray = JSONArray(json)
    val questions = mutableListOf<CodingChallengeQuestion>()
    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        questions.add(
            CodingChallengeQuestion(
                question_id = obj.getString("question_id"),
                created_at = obj.getString("created_at"),
                language = obj.getString("language"),
                level = obj.getString("level"),
                question = obj.getString("question"),
                options = obj.getJSONArray("options").let { arr -> List(arr.length()) { arr.getString(it) } },
                correct_answer = obj.getString("correct_answer"),
                explanation = obj.getString("explanation"),
                platform = obj.getString("platform")
            )
        )
    }
    questions
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State for selections
    var selectedPlatform by remember { mutableStateOf("Mobile") }
    var selectedLanguage by remember { mutableStateOf("Kotlin") }
    var selectedLevel by remember { mutableStateOf("Beginner") }
    var questions by remember { mutableStateOf<List<CodingChallengeQuestion>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var userAnswers by remember { mutableStateOf(mutableMapOf<Int, String>()) }
    var showSummary by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var cooldownActive by remember { mutableStateOf(false) }
    var questionsPerDay by remember { mutableStateOf(5) }

    // Set questions per day based on level
    LaunchedEffect(selectedLevel) {
        questionsPerDay = when (selectedLevel) {
            "Beginner" -> 5
            "Intermediate" -> 7
            "Advanced" -> 10
            else -> 5
        }
    }

    // Load questions when selection changes
    LaunchedEffect(selectedLanguage, selectedLevel) {
        isLoading = true
        coroutineScope.launch {
            val loaded = loadQuestionsFromAssets(context, selectedLanguage, selectedLevel)
            questions = loaded.shuffled().take(questionsPerDay)
            currentIndex = 0
            userAnswers = mutableMapOf()
            showSummary = false
            isLoading = false
            // TODO: Implement daily cooldown logic (persist last attempt date)
        }
    }

    Scaffold(
        topBar = {
            Header(
                text = "Learn",
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val (platform, language, level, challenge) = createRefs()

            // Platform Selection
            PlatformSelectionSection(
                selectedPlatform = selectedPlatform,
                onSelect = { selectedPlatform = it },
                modifier = Modifier.constrainAs(platform) {
                    top.linkTo(parent.top, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            // Language Selection
            LanguageSelectionSection(
                selectedPlatform = selectedPlatform,
                selectedLanguage = selectedLanguage,
                onSelect = { selectedLanguage = it },
                modifier = Modifier.constrainAs(language) {
                    top.linkTo(platform.bottom, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            // Level Selection
            LevelSelectionSection(
                selectedLevel = selectedLevel,
                onSelect = { selectedLevel = it },
                modifier = Modifier.constrainAs(level) {
                    top.linkTo(language.bottom, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            // Challenge Section
            Box(modifier = Modifier.constrainAs(challenge) {
                top.linkTo(level.bottom, margin = 32.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (cooldownActive) {
                    Text("You have completed your daily challenges. Come back tomorrow!", color = Color.Gray)
                } else if (showSummary) {
                    ChallengeSummary(
                        questions = questions,
                        userAnswers = userAnswers,
                        onRestart = {
                            showSummary = false
                            userAnswers = mutableMapOf()
                            currentIndex = 0
                        }
                    )
                } else if (questions.isNotEmpty()) {
                    ChallengeQuestionCard(
                        question = questions[currentIndex],
                        questionNumber = currentIndex + 1,
                        totalQuestions = questions.size,
                        selectedAnswer = userAnswers[currentIndex],
                        onSelectAnswer = { answer ->
                            userAnswers = userAnswers.toMutableMap().apply { put(currentIndex, answer) }
                        },
                        onPrev = { if (currentIndex > 0) currentIndex-- },
                        onNext = {
                            if (currentIndex < questions.size - 1) currentIndex++
                            else showSummary = true
                        },
                        isFirst = currentIndex == 0,
                        isLast = currentIndex == questions.size - 1
                    )
                } else {
                    Text("No questions available for this selection.", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PlatformSelectionSection(selectedPlatform: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Select Platform", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PlatformTag("Mobile", selectedPlatform == "Mobile") { onSelect("Mobile") }
            PlatformTag("Web", selectedPlatform == "Web") { onSelect("Web") }
            PlatformTag("Games", selectedPlatform == "Games") { onSelect("Games") }
        }
    }
}

@Composable
fun PlatformTag(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color(0xFF6200EE) else Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun LanguageSelectionSection(selectedPlatform: String, selectedLanguage: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val languages = when (selectedPlatform) {
        "Mobile" -> listOf("Java", "Kotlin", "Swift")
        "Web" -> listOf("JavaScript", "Python")
        "Games" -> listOf("C#", "C++")
        else -> emptyList()
    }
    var language by remember { mutableStateOf(selectedLanguage) }
    LaunchedEffect(selectedPlatform) { if (languages.isNotEmpty()) onSelect(languages[0]) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Select Language", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            languages.forEach { lang ->
                LanguageTag(lang, selectedLanguage == lang) { onSelect(lang) }
            }
        }
    }
}

@Composable
fun LanguageTag(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color(0xFF6200EE) else Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun LevelSelectionSection(selectedLevel: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Select Level", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LevelTag("Beginner", selectedLevel == "Beginner") { onSelect("Beginner") }
            LevelTag("Intermediate", selectedLevel == "Intermediate") { onSelect("Intermediate") }
            LevelTag("Advanced", selectedLevel == "Advanced") { onSelect("Advanced") }
        }
    }
}

@Composable
fun LevelTag(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color(0xFF6200EE) else Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ChallengeQuestionCard(
    question: CodingChallengeQuestion,
    questionNumber: Int,
    totalQuestions: Int,
    selectedAnswer: String?,
    onSelectAnswer: (String) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Question $questionNumber of $totalQuestions", fontWeight = FontWeight.Bold, color = Color(0xFF6200EE))
            Spacer(modifier = Modifier.height(8.dp))
            Text(question.question, fontSize = 16.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            question.options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedAnswer == option) Color(0xFF6200EE).copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onSelectAnswer(option) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedAnswer == option,
                        onClick = { onSelectAnswer(option) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6200EE))
                    )
                    Text(option, fontSize = 15.sp, color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!isFirst) {
                    Button(onClick = onPrev, shape = RoundedCornerShape(12.dp)) {
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedAnswer != null
                ) {
                    Text(if (isLast) "Finish" else "Next")
                }
            }
        }
    }
}

@Composable
fun ChallengeSummary(
    questions: List<CodingChallengeQuestion>,
    userAnswers: Map<Int, String>,
    onRestart: () -> Unit
) {
    val correctCount = questions.indices.count { userAnswers[it] == questions[it].correct_answer }
    val percent = if (questions.isNotEmpty()) (correctCount * 100 / questions.size) else 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Summary", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Score: $correctCount / ${questions.size} ($percent%)", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        questions.forEachIndexed { idx, q ->
            val userAns = userAnswers[idx]
            val isCorrect = userAns == q.correct_answer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Q${idx + 1}: ${q.question}", fontWeight = FontWeight.Bold)
                    Text("Your answer: ${userAns ?: "No answer"}", color = if (isCorrect) Color(0xFF388E3C) else Color(0xFFD32F2F))
                    Text("Correct answer: ${q.correct_answer}", color = Color.Black)
                    Text("Explanation: ${q.explanation}", color = Color.Gray, fontSize = 13.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRestart, shape = RoundedCornerShape(12.dp)) {
            Text("Restart")
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun LearnScreenPreview() {
    LearnScreen(navController = rememberNavController())
}
