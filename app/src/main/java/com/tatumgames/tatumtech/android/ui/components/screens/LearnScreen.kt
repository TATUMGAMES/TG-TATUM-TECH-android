package com.tatumgames.tatumtech.android.ui.components.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource // Keep if using custom drawables
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tatumgames.tatumtech.android.R // Assuming R is correctly imported

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(navController: NavController) {
    Scaffold(
        containerColor = Color(0xFFF0F0F0) // Light grey background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Make screen scrollable
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.popBackStack() } // Example back navigation
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Learn",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Select a language
            SectionTitle("Select a language")
            Spacer(modifier = Modifier.height(12.dp))
            var selectedLanguage by remember { mutableStateOf("Python") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableTag(text = "Python", isSelected = selectedLanguage == "Python") { selectedLanguage = "Python" }
                SelectableTag(text = "JavaScript", isSelected = selectedLanguage == "JavaScript") { selectedLanguage = "JavaScript" }
                SelectableTag(text = "C#", isSelected = selectedLanguage == "C#") { selectedLanguage = "C#" }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Select a platform
            SectionTitle("Select a platform")
            Spacer(modifier = Modifier.height(12.dp))
            var selectedPlatform by remember { mutableStateOf("Mobile") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableTag(text = "Mobile", isSelected = selectedPlatform == "Mobile") { selectedPlatform = "Mobile" }
                SelectableTag(text = "Web", isSelected = selectedPlatform == "Web") { selectedPlatform = "Web" }
                SelectableTag(text = "Games", isSelected = selectedPlatform == "Games") { selectedPlatform = "Games" }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Select a level
            SectionTitle("Select a level")
            Spacer(modifier = Modifier.height(12.dp))
            var selectedLevel by remember { mutableStateOf("Beginner") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableTag(text = "Beginner", isSelected = selectedLevel == "Beginner") { selectedLevel = "Beginner" }
                SelectableTag(text = "Intermediate", isSelected = selectedLevel == "Intermediate") { selectedLevel = "Intermediate" }
                SelectableTag(text = "Advanced", isSelected = selectedLevel == "Advanced") { selectedLevel = "Advanced" }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Challenge Section
            SectionTitle("Challenge 1")
            Text(
                text = "What is the output of the following Python code?",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "print(\"Hello, World!\")",
                fontSize = 15.sp,
                color = Color.Black,
                fontWeight = FontWeight.Light,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Multiple Choice Options
            var selectedOption by remember { mutableStateOf("") }
            val options = listOf("Hello, World!", "Hello World", "Hello, world!", "hello, world!")

            options.forEach { option ->
                RadioButtonOption(text = option, isSelected = selectedOption == option) {
                    selectedOption = option
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Code Input Area
            var userCode by remember { mutableStateOf("") }
            OutlinedTextField(
                value = userCode,
                onValueChange = { userCode = it },
                label = { Text("Type your code here") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // Allow content to determine height
                    .defaultMinSize(minHeight = 120.dp), // Minimum height
                shape = RoundedCornerShape(12.dp)
//                colors = TextFieldDefaults.colors(
//                    containerColor = Color.White,
//                    focusedBorderColor = MaterialTheme.colorScheme.primary,
//                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
//                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Progress", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Text(text = "20/100", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = 0.2f, // 20/100
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary, // Primary color for progress
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) // Lighter track color
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* Handle Submit */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)), // Use a distinct color for Submit
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "Submit", color = Color.White, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { /* Handle Next */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDE7F6)), // Light purple background
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "Next", color = Color(0xFF6200EE), fontSize = 16.sp) // Purple text
                }
            }
            Spacer(modifier = Modifier.height(24.dp)) // Padding for scrollability
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black
    )
}

@Composable
fun SelectableTag(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor), width = 1.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 14.sp)
    }
}

@Composable
fun RadioButtonOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 16.sp, color = Color.Black)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun LearnScreenPreview() {
    LearnScreen(navController = rememberNavController())
}
