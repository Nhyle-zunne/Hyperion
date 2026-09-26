package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompetencyMetadata
import com.example.data.model.ExamAnswer
import com.example.data.model.ExamRecord
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.PracticeFilterMode
import com.example.ui.components.HyperionTopBar
import com.example.ui.theme.MaritimeCorrectGreen
import com.example.ui.theme.MaritimeFlagAmber
import com.example.ui.theme.MaritimeGold
import com.example.ui.theme.MaritimeIncorrectRed
import kotlinx.coroutines.launch
import org.json.JSONObject

// ==========================================
// 1. EXAM SETUP SCREEN
// ==========================================
@Composable
fun ExamSetupScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val currentTrack by viewModel.currentTrack.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Official, 1 = Custom

    Scaffold(
        topBar = {
            HyperionTopBar(
                title = "Mock Examination",
                subtitle = "$currentTrack Simulation Mode",
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Official Simulation", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Custom Mock Exam", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                OfficialSimulationTab(currentTrack = currentTrack, onStart = { viewModel.requestStartOfficialExam() })
            } else {
                CustomExamTab(
                    currentTrack = currentTrack,
                    onStart = { comps, count, time -> viewModel.requestStartCustomExam(comps, count, time) }
                )
            }
        }
    }
}

@Composable
fun OfficialSimulationTab(currentTrack: String, onStart: () -> Unit) {
    val totalItems = if (currentTrack == "OIC-NW") 200 else 100
    val timeMinutes = if (currentTrack == "OIC-NW") 180 else 90

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "$currentTrack Licensure Exam Simulation",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "Modeled after MARINA Licensure Examination Specifications",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailPill("Items", "$totalItems Questions")
                    DetailPill("Duration", "$timeMinutes Minutes")
                    DetailPill("Passing", "70%")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Competency Distribution:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (currentTrack == "OIC-NW") {
                    Text(
                        text = "• F1 Navigation (125 items): C1=35, C2=20, C3=15, C4=10, C5=10, C7=20, C9=15\n• F2 Cargo (30 items): C10=15, C11=15\n• F3 Ship Control (45 items): C12=15, C13=15, C17=15",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(
                        text = "• C1 Subsystems & Functional Requirements: 85 items\n• C2 Radio Services in Emergencies: 15 items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Notice: This is a standalone simulated examination designed for preparation purposes. It draws exclusively from the authentic master question bank.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_official_exam_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Assignment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START OFFICIAL SIMULATION",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomExamTab(
    currentTrack: String,
    onStart: (List<String>, Int, Int) -> Unit
) {
    val compList = if (currentTrack == "OIC-NW") CompetencyMetadata.OIC_NW_COMPETENCIES else CompetencyMetadata.GMDSS_COMPETENCIES
    val selectedComps = remember { mutableStateListOf<String>().apply { addAll(compList.map { it.code }) } }

    var questionCount by remember { mutableIntStateOf(50) }
    var timeMinutes by remember { mutableIntStateOf(60) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Number of Questions: $questionCount",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = questionCount.toFloat(),
                    onValueChange = { questionCount = it.toInt() },
                    valueRange = 10f..200f,
                    steps = 18
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Time Limit: $timeMinutes minutes",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = timeMinutes.toFloat(),
                    onValueChange = { timeMinutes = it.toInt() },
                    valueRange = 15f..180f,
                    steps = 10
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Include Competencies",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = {
                        if (selectedComps.size == compList.size) {
                            selectedComps.clear()
                            selectedComps.add(compList.first().code)
                        } else {
                            selectedComps.clear()
                            selectedComps.addAll(compList.map { it.code })
                        }
                    }) {
                        Text(if (selectedComps.size == compList.size) "Deselect All" else "Select All")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                compList.forEach { comp ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedComps.contains(comp.code)) {
                                    if (selectedComps.size > 1) selectedComps.remove(comp.code)
                                } else {
                                    selectedComps.add(comp.code)
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedComps.contains(comp.code),
                            onCheckedChange = {
                                if (it) selectedComps.add(comp.code)
                                else if (selectedComps.size > 1) selectedComps.remove(comp.code)
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "${comp.code} – ${comp.title}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { onStart(selectedComps.toList(), questionCount, timeMinutes) },
            enabled = selectedComps.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("START CUSTOM EXAM ($questionCount QUESTIONS)", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DetailPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ==========================================
// 2. EXAM RUNNER SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExamRunnerScreen(viewModel: MainViewModel) {
    val activeExam by viewModel.activeExam.collectAsState()

    if (activeExam == null || activeExam!!.questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active examination.")
        }
        return
    }

    val exam = activeExam!!
    val currentQuestion = exam.questions.getOrNull(exam.currentIndex)

    var showSubmitDialog by remember { mutableStateOf(false) }
    var showExitExamDialog by remember { mutableStateOf(false) }
    var showNavigatorSheet by remember { mutableStateOf(false) }

    val answeredCount = exam.userAnswers.size
    val totalCount = exam.questions.size

    val minutes = exam.timeRemainingSeconds / 60
    val seconds = exam.timeRemainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    BackHandler {
        showExitExamDialog = true
    }

    Scaffold(
        topBar = {
            HyperionTopBar(
                title = "Exam: ${exam.currentIndex + 1} / $totalCount",
                subtitle = "Answered: $answeredCount / $totalCount",
                onBack = { showExitExamDialog = true },
                actions = {
                    // Timer display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (exam.timeRemainingSeconds < 300) MaritimeIncorrectRed.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (exam.timeRemainingSeconds < 300) MaritimeIncorrectRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (exam.timeRemainingSeconds < 300) MaritimeIncorrectRed else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Question grid palette button
                    IconButton(onClick = { showNavigatorSheet = true }) {
                        Icon(imageVector = Icons.Default.GridView, contentDescription = "Palette")
                    }

                    // Submit Exam button
                    TextButton(onClick = { showSubmitDialog = true }) {
                        Text("SUBMIT", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (currentQuestion != null) {
            val selectedOption = exam.userAnswers[currentQuestion.id]
            val isFlagged = exam.flaggedQuestionIds.contains(currentQuestion.id)

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Flag button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${currentQuestion.competencyCode}${currentQuestion.partNumber?.let { " • Part $it" } ?: ""}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.toggleExamQuestionFlag(currentQuestion.id) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isFlagged) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                            contentDescription = null,
                            tint = if (isFlagged) MaritimeFlagAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFlagged) "Flagged" else "Flag for Review")
                    }
                }

                // Question Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 24.sp
                            )
                        )
                    }
                }

                // Options List
                val options = currentQuestion.getOptionsList()
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    options.forEachIndexed { optIndex, optText ->
                        val letter = ('A'.code + optIndex).toChar().toString()
                        val isSelected = selectedOption == optIndex

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectExamAnswer(currentQuestion.id, optIndex)
                                },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = letter,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = optText,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Navigation Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setExamCurrentIndex(exam.currentIndex - 1) },
                        enabled = exam.currentIndex > 0,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PREVIOUS")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (exam.currentIndex < totalCount - 1) {
                                viewModel.setExamCurrentIndex(exam.currentIndex + 1)
                            } else {
                                showSubmitDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(if (exam.currentIndex < totalCount - 1) "NEXT" else "FINISH")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }

    // Modal Sheet for Question Grid Navigator
    if (showNavigatorSheet) {
        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = { showNavigatorSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Question Palette (${exam.questions.size} items)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🟢 Answered ($answeredCount)", style = MaterialTheme.typography.labelSmall)
                    Text("🟠 Flagged (${exam.flaggedQuestionIds.size})", style = MaterialTheme.typography.labelSmall)
                    Text("⚪ Unanswered (${totalCount - answeredCount})", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.height(300.dp)) {
                    val paletteScroll = rememberScrollState()
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(paletteScroll),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        exam.questions.forEachIndexed { idx, q ->
                            val isAnswered = exam.userAnswers.containsKey(q.id)
                            val isFlag = exam.flaggedQuestionIds.contains(q.id)
                            val isCurrent = exam.currentIndex == idx

                            val bgColor = when {
                                isCurrent -> MaterialTheme.colorScheme.primary
                                isFlag -> MaritimeFlagAmber
                                isAnswered -> MaritimeCorrectGreen
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .clickable {
                                        viewModel.setExamCurrentIndex(idx)
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            showNavigatorSheet = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isAnswered || isCurrent || isFlag) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showNavigatorSheet = false
                            showSubmitDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SUBMIT EXAMINATION NOW")
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitDialog) {
        val unanswered = totalCount - answeredCount
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Examination?") },
            text = {
                Column {
                    Text("You have answered $answeredCount of $totalCount questions.")
                    if (unanswered > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Warning: You still have $unanswered unanswered questions!",
                            color = MaritimeIncorrectRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Are you ready to submit and calculate your final score?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitExam()
                    }
                ) {
                    Text("Yes, Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Continue Exam")
                }
            }
        )
    }

    // Exit Exam (Pause & Save) Dialog
    if (showExitExamDialog) {
        AlertDialog(
            onDismissRequest = { showExitExamDialog = false },
            icon = { Icon(Icons.Default.Timer, contentDescription = null, tint = MaritimeGold) },
            title = { Text("Pause & Save Examination?") },
            text = {
                Column {
                    Text("Your exam session will be logged ($answeredCount of $totalCount answered, $formattedTime remaining).")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You can resume this exam anytime from the Home screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitExamDialog = false
                        viewModel.saveExamSession()
                        viewModel.popToHome()
                    },
                    modifier = Modifier.testTag("save_and_exit_exam_button")
                ) {
                    Text("Save & Exit to Home")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        showExitExamDialog = false
                        showSubmitDialog = true
                    }) {
                        Text("Submit Exam", color = MaritimeIncorrectRed)
                    }
                    TextButton(onClick = { showExitExamDialog = false }) {
                        Text("Continue Exam")
                    }
                }
            }
        )
    }
}

// ==========================================
// 3. EXAM RESULT SCREEN
// ==========================================
@Composable
fun ExamResultScreen(
    viewModel: MainViewModel,
    recordId: Long
) {
    BackHandler { viewModel.popToHome() }

    var record by remember { mutableStateOf<ExamRecord?>(null) }
    LaunchedEffect(recordId) {
        record = viewModel.examRepo.getExamRecord(recordId)
    }

    Scaffold(
        topBar = {
            HyperionTopBar(
                title = "Exam Results",
                onBack = { viewModel.popToHome() }
            )
        }
    ) { paddingValues ->
        if (record == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading results...")
            }
            return@Scaffold
        }

        val r = record!!
        val isPassed = r.passed
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big Score Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPassed) MaritimeCorrectGreen.copy(alpha = 0.1f) else MaritimeIncorrectRed.copy(alpha = 0.1f)
                ),
                border = BorderStroke(2.dp, if (isPassed) MaritimeCorrectGreen else MaritimeIncorrectRed)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isPassed) "EXAMINATION PASSED" else "DID NOT PASS",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = if (isPassed) MaritimeCorrectGreen else MaritimeIncorrectRed
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${r.score} / ${r.totalQuestions}",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black)
                    )

                    Text(
                        text = "${String.format("%.1f", r.percentage)}%",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isPassed) MaritimeCorrectGreen else MaritimeIncorrectRed
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Passing Threshold: 70.0%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Summary Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailPill("Correct", "${r.score}")
                DetailPill("Incorrect", "${r.totalQuestions - r.score}")
                val mins = r.timeUsedSeconds / 60
                val secs = r.timeUsedSeconds % 60
                DetailPill("Time Used", "${mins}m ${secs}s")
            }

            // Competency Breakdown
            val breakdownList = remember(r.competencyBreakdownJson) {
                val list = mutableListOf<Triple<String, String, Int>>()
                try {
                    val json = JSONObject(r.competencyBreakdownJson)
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val comp = keys.next()
                        val scoreStr = json.getString(comp)
                        val parts = scoreStr.split("/")
                        val correct = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val total = parts.getOrNull(1)?.toIntOrNull() ?: 1
                        val compPct = (correct.toFloat() / total * 100).toInt()
                        list.add(Triple(comp, "$correct / $total", compPct))
                    }
                } catch (_: Exception) {}
                list
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Competency Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (breakdownList.isEmpty()) {
                        Text("Breakdown summary available.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        breakdownList.forEach { (comp, scoreText, compPct) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$comp (${CompetencyMetadata.getCompetencyTitle(r.track, comp)})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$scoreText ($compPct%)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (compPct >= 70) MaritimeCorrectGreen else MaritimeIncorrectRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { compPct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.ExamReview(recordId)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("REVIEW ALL ANSWERS", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.Incorrect) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("RETRY INCORRECT ITEMS")
                }

                TextButton(
                    onClick = { viewModel.popToHome() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RETURN TO DASHBOARD")
                }
            }
        }
    }
}

// ==========================================
// 4. EXAM REVIEW SCREEN
// ==========================================
@Composable
fun ExamReviewScreen(
    viewModel: MainViewModel,
    recordId: Long
) {
    BackHandler { viewModel.navigateBack() }

    var record by remember { mutableStateOf<ExamRecord?>(null) }
    var answers by remember { mutableStateOf<List<ExamAnswer>>(emptyList()) }

    LaunchedEffect(recordId) {
        record = viewModel.examRepo.getExamRecord(recordId)
        answers = viewModel.examRepo.getAnswersForExam(recordId)
    }

    Scaffold(
        topBar = {
            HyperionTopBar(
                title = "Exam Review",
                subtitle = "Total: ${answers.size} questions",
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(answers) { ans ->
                val isCorrect = ans.isCorrect
                val selectedLetter = if (ans.selectedIndex >= 0) ('A'.code + ans.selectedIndex).toChar().toString() else "None"
                val correctLetter = ('A'.code + ans.correctIndex).toChar().toString()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isCorrect) MaritimeCorrectGreen else MaritimeIncorrectRed)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = ans.competencyCode,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isCorrect) MaritimeCorrectGreen else MaritimeIncorrectRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCorrect) "Correct" else "Incorrect",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCorrect) MaritimeCorrectGreen else MaritimeIncorrectRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = ans.questionText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Your Choice: $selectedLetter",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCorrect) MaritimeCorrectGreen else MaritimeIncorrectRed
                            )
                            Text(
                                text = "Correct Answer: $correctLetter",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaritimeCorrectGreen
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
