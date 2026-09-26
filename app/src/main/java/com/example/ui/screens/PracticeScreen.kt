package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Question
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.PracticeFilterMode
import com.example.ui.StudyMode
import com.example.ui.components.HyperionTopBar
import com.example.ui.theme.MaritimeCorrectGreen
import com.example.ui.theme.MaritimeFavoriteGold
import com.example.ui.theme.MaritimeFlagAmber
import com.example.ui.theme.MaritimeGold
import com.example.ui.theme.MaritimeIncorrectRed
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    viewModel: MainViewModel,
    practiceArgs: AppScreen.Practice
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val currentTrack by viewModel.currentTrack.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()

    // Configurable state initialized from practiceArgs
    var activeFilter by remember { mutableStateOf(practiceArgs.filterMode) }
    var isRandomized by remember { mutableStateOf(practiceArgs.isRandomized) }
    var shuffleOptions by remember { mutableStateOf(practiceArgs.shuffleOptions) }
    var currentStudyMode by remember { mutableStateOf(practiceArgs.studyMode) }
    var currentLimit by remember { mutableStateOf(practiceArgs.itemLimit) }
    var randomSeed by remember { mutableIntStateOf(practiceArgs.randomSeed) }

    fun filterAndOrderQuestions(
        source: List<Question>,
        filter: PracticeFilterMode,
        seed: Int,
        randomize: Boolean,
        limit: Int?
    ): List<Question> {
        var list = source
        if (practiceArgs.competencyCode != null) {
            list = list.filter { it.competencyCode.equals(practiceArgs.competencyCode, ignoreCase = true) }
        }
        if (practiceArgs.partNumber != null) {
            list = list.filter { it.partNumber == practiceArgs.partNumber }
        }
        val filtered = when (filter) {
            PracticeFilterMode.ALL -> list
            PracticeFilterMode.UNANSWERED -> list.filter { it.timesAttempted == 0 }
            PracticeFilterMode.INCORRECT -> list.filter { it.timesIncorrect > 0 }
            PracticeFilterMode.FAVORITES -> list.filter { it.isFavorite }
            PracticeFilterMode.FLAGGED -> list.filter { it.isFlagged }
            PracticeFilterMode.NEEDS_REVIEW -> list.filter { it.masteryStatus == "NEEDS_REVIEW" || it.timesIncorrect > 0 }
            PracticeFilterMode.MASTERED -> list.filter { it.masteryStatus == "MASTERED" }
            PracticeFilterMode.SMART_REVIEW -> list.sortedWith(
                compareByDescending<Question> { it.timesIncorrect }
                    .thenBy { if (it.timesAttempted == 0) 0 else 1 }
            )
        }

        val ordered = if (randomize) {
            filtered.shuffled(Random(seed))
        } else {
            filtered
        }

        return if (limit != null && limit > 0) {
            ordered.take(limit)
        } else {
            ordered
        }
    }

    var workingQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isDeckInitialized by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableIntStateOf(practiceArgs.initialIndex) }

    // Session tracking for scorecard & question navigator
    val sessionAnswers = remember { mutableStateMapOf<Long, Int>().apply { putAll(practiceArgs.initialAnswers) } }
    val sessionResults = remember { mutableStateMapOf<Long, Boolean>().apply { putAll(practiceArgs.initialResults) } }

    var isSessionComplete by remember { mutableStateOf(false) }

    // Initialize session questions snapshot (or restore from saved customQuestionIds)
    LaunchedEffect(allQuestions) {
        if (!isDeckInitialized && allQuestions.isNotEmpty()) {
            if (!practiceArgs.customQuestionIds.isNullOrEmpty()) {
                val qMap = allQuestions.associateBy { it.id }
                val restored = practiceArgs.customQuestionIds.mapNotNull { qMap[it] }
                workingQuestions = if (restored.isNotEmpty()) restored else filterAndOrderQuestions(
                    source = allQuestions,
                    filter = activeFilter,
                    seed = randomSeed,
                    randomize = isRandomized,
                    limit = currentLimit
                )
            } else {
                workingQuestions = filterAndOrderQuestions(
                    source = allQuestions,
                    filter = activeFilter,
                    seed = randomSeed,
                    randomize = isRandomized,
                    limit = currentLimit
                )
            }
            if (practiceArgs.initialIndex in workingQuestions.indices) {
                currentIndex = practiceArgs.initialIndex
            }
            isDeckInitialized = true
        }
    }

    // When session completes, remove its saved ongoing log
    LaunchedEffect(isSessionComplete) {
        if (isSessionComplete && practiceArgs.resumeSessionLogId != null) {
            viewModel.deleteSessionLog(practiceArgs.resumeSessionLogId)
        }
    }

    val saveOngoingSession: () -> Unit = {
        if (workingQuestions.isNotEmpty() && !isSessionComplete) {
            viewModel.savePracticeSession(
                title = practiceArgs.title,
                filterMode = activeFilter,
                studyMode = currentStudyMode,
                competencyCode = practiceArgs.competencyCode,
                partNumber = practiceArgs.partNumber,
                itemLimit = currentLimit,
                isRandomized = isRandomized,
                shuffleOptions = shuffleOptions,
                timeLimitSecondsPerItem = practiceArgs.timeLimitSecondsPerItem,
                randomSeed = randomSeed,
                questions = workingQuestions,
                currentIndex = currentIndex,
                answers = sessionAnswers.toMap(),
                results = sessionResults.toMap(),
                existingLogId = practiceArgs.resumeSessionLogId
            )
        }
    }

    val onBackAction: () -> Unit = {
        saveOngoingSession()
        viewModel.navigateBack()
    }

    BackHandler {
        onBackAction()
    }

    var showGridDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var isFlashcardFlipped by remember(currentIndex) { mutableStateOf(false) }

    val currentQuestion = workingQuestions.getOrNull(currentIndex)
    val selectedOptionIndex = currentQuestion?.let { sessionAnswers[it.id] }
    val hasAnswered = selectedOptionIndex != null

    // Speed run timer state
    var speedTimeRemaining by remember(currentIndex, currentStudyMode) {
        mutableIntStateOf(practiceArgs.timeLimitSecondsPerItem ?: 30)
    }

    LaunchedEffect(currentIndex, hasAnswered, currentStudyMode) {
        if (currentStudyMode == StudyMode.SPEED_RUN && !hasAnswered && workingQuestions.isNotEmpty()) {
            speedTimeRemaining = practiceArgs.timeLimitSecondsPerItem ?: 30
            while (speedTimeRemaining > 0 && !hasAnswered) {
                delay(1000)
                speedTimeRemaining--
            }
            if (speedTimeRemaining <= 0 && !hasAnswered) {
                // Time expired: auto-reveal answer
                val q = workingQuestions.getOrNull(currentIndex)
                if (q != null) {
                    sessionAnswers[q.id] = -1 // timed out
                    sessionResults[q.id] = false
                    viewModel.recordPracticeAnswer(q, -1)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            val limitText = if (currentLimit != null) " (Limit $currentLimit)" else ""
            HyperionTopBar(
                title = practiceArgs.title,
                subtitle = if (workingQuestions.isNotEmpty()) {
                    "Question ${currentIndex + 1} of ${workingQuestions.size}$limitText"
                } else "0 Questions",
                onBack = { onBackAction() },
                actions = {
                    // Jump Grid Dialog button
                    if (workingQuestions.isNotEmpty()) {
                        IconButton(
                            onClick = { showGridDialog = true },
                            modifier = Modifier.testTag("jump_grid_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Question Matrix",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Reshuffle current deck
                    IconButton(
                        onClick = {
                            isRandomized = true
                            randomSeed = (1..100000).random()
                            workingQuestions = filterAndOrderQuestions(
                                source = allQuestions,
                                filter = activeFilter,
                                seed = randomSeed,
                                randomize = true,
                                limit = currentLimit
                            )
                            currentIndex = 0
                            sessionAnswers.clear()
                            sessionResults.clear()
                            isSessionComplete = false
                        },
                        modifier = Modifier.testTag("reshuffle_deck_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle Deck",
                            tint = if (isRandomized) MaritimeGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Switch between Multiple Choice and Flashcard
                    IconButton(
                        onClick = {
                            currentStudyMode = if (currentStudyMode == StudyMode.FLASHCARD) {
                                StudyMode.TUTOR
                            } else {
                                StudyMode.FLASHCARD
                            }
                        },
                        modifier = Modifier.testTag("toggle_flashcard_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = "Flashcard Mode",
                            tint = if (currentStudyMode == StudyMode.FLASHCARD) MaritimeGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Report Question
                    if (currentQuestion != null) {
                        IconButton(onClick = { showReportDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = "Report Question",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (workingQuestions.isEmpty() && !isDeckInitialized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (workingQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No questions found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No items match the selected filter '$activeFilter'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        activeFilter = PracticeFilterMode.ALL
                        workingQuestions = filterAndOrderQuestions(
                            source = allQuestions,
                            filter = PracticeFilterMode.ALL,
                            seed = randomSeed,
                            randomize = isRandomized,
                            limit = currentLimit
                        )
                        currentIndex = 0
                        sessionAnswers.clear()
                        sessionResults.clear()
                        isSessionComplete = false
                    }) {
                        Text("Show All Questions")
                    }
                }
            }
        } else if (isSessionComplete) {
            // SESSION COMPLETION SCORECARD
            SessionScorecard(
                totalQuestions = workingQuestions.size,
                correctCount = sessionResults.count { it.value },
                incorrectCount = sessionResults.count { !it.value },
                onRetryMissed = {
                    val missedQuestions = workingQuestions.filter { sessionResults[it.id] == false }
                    if (missedQuestions.isNotEmpty()) {
                        workingQuestions = missedQuestions
                        currentIndex = 0
                        sessionAnswers.clear()
                        sessionResults.clear()
                        isSessionComplete = false
                    }
                },
                onRepeatDrill = {
                    randomSeed = (1..100000).random()
                    workingQuestions = filterAndOrderQuestions(
                        source = allQuestions,
                        filter = activeFilter,
                        seed = randomSeed,
                        randomize = true,
                        limit = currentLimit
                    )
                    currentIndex = 0
                    sessionAnswers.clear()
                    sessionResults.clear()
                    isSessionComplete = false
                },
                onStudyAllBank = {
                    activeFilter = PracticeFilterMode.ALL
                    isRandomized = false
                    currentLimit = null
                    workingQuestions = filterAndOrderQuestions(
                        source = allQuestions,
                        filter = PracticeFilterMode.ALL,
                        seed = 1,
                        randomize = false,
                        limit = null
                    )
                    currentIndex = 0
                    sessionAnswers.clear()
                    sessionResults.clear()
                    isSessionComplete = false
                },
                onReturnToBank = { viewModel.navigateBack() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            )
        } else if (currentQuestion != null) {
            val originalOptions = currentQuestion.getOptionsList()

            // Prepare answer list with stable randomization mapping
            val pairedOptions = remember(currentQuestion.id, shuffleOptions) {
                val list = originalOptions.mapIndexed { idx, text ->
                    Triple(text, idx == currentQuestion.correctAnswerIndex, idx)
                }
                if (shuffleOptions) list.shuffled(Random(currentQuestion.id)) else list
            }

            val correctDisplayIndex = pairedOptions.indexOfFirst { it.second }
            val correctDisplayLetter = ('A'.code + correctDisplayIndex).toChar().toString()

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Progress and Mode Badge Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val progress = (currentIndex + 1).toFloat() / workingQuestions.size
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (currentStudyMode) {
                            StudyMode.TUTOR -> MaterialTheme.colorScheme.primaryContainer
                            StudyMode.FLASHCARD -> MaritimeGold.copy(alpha = 0.2f)
                            StudyMode.SPEED_RUN -> MaritimeIncorrectRed.copy(alpha = 0.15f)
                            StudyMode.DRILL_EXAM -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            text = when (currentStudyMode) {
                                StudyMode.TUTOR -> "TUTOR MODE"
                                StudyMode.FLASHCARD -> "FLASHCARD"
                                StudyMode.SPEED_RUN -> "SPEED RUN: ${speedTimeRemaining}s"
                                StudyMode.DRILL_EXAM -> "DRILL EXAM"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when (currentStudyMode) {
                                StudyMode.TUTOR -> MaterialTheme.colorScheme.onPrimaryContainer
                                StudyMode.FLASHCARD -> MaritimeGold
                                StudyMode.SPEED_RUN -> MaritimeIncorrectRed
                                StudyMode.DRILL_EXAM -> MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Filter & Competency Metadata row
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Options shuffle indicator toggle
                        IconButton(
                            onClick = { shuffleOptions = !shuffleOptions },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle Options",
                                tint = if (shuffleOptions) MaritimeGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Favorite toggle
                        IconButton(
                            onClick = {
                                viewModel.toggleFavorite(currentQuestion)
                                workingQuestions = workingQuestions.map {
                                    if (it.id == currentQuestion.id) it.copy(isFavorite = !it.isFavorite) else it
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (currentQuestion.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (currentQuestion.isFavorite) MaritimeFavoriteGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Flag toggle
                        IconButton(
                            onClick = {
                                viewModel.toggleFlag(currentQuestion)
                                workingQuestions = workingQuestions.map {
                                    if (it.id == currentQuestion.id) it.copy(isFlagged = !it.isFlagged) else it
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (currentQuestion.isFlagged) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                                contentDescription = "Flag",
                                tint = if (currentQuestion.isFlagged) MaritimeFlagAmber else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ==========================================
                // FLASHCARD VIEW (When Flashcard mode active)
                // ==========================================
                if (currentStudyMode == StudyMode.FLASHCARD) {
                    FlashcardInteractiveView(
                        question = currentQuestion,
                        currentIndex = currentIndex,
                        totalCount = workingQuestions.size,
                        isFlipped = isFlashcardFlipped,
                        onFlip = { isFlashcardFlipped = !isFlashcardFlipped },
                        onMarkMastered = {
                            viewModel.recordPracticeAnswer(currentQuestion, currentQuestion.correctAnswerIndex)
                            sessionResults[currentQuestion.id] = true
                            if (currentIndex < workingQuestions.size - 1) {
                                currentIndex++
                            } else {
                                isSessionComplete = true
                            }
                        },
                        onMarkReview = {
                            viewModel.recordPracticeAnswer(currentQuestion, -1)
                            sessionResults[currentQuestion.id] = false
                            if (currentIndex < workingQuestions.size - 1) {
                                currentIndex++
                            } else {
                                isSessionComplete = true
                            }
                        }
                    )
                } else {
                    // ==========================================
                    // STANDARD MULTIPLE CHOICE VIEW
                    // ==========================================
                    // Question Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("question_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "QUESTION ${currentIndex + 1} / ${workingQuestions.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
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

                    // Answer Choices
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        pairedOptions.forEachIndexed { optIndex, (optionText, isCorrectOption, originalIdx) ->
                            val letter = ('A'.code + optIndex).toChar().toString()
                            val isSelected = selectedOptionIndex == optIndex

                            // Colors based on answer state and study mode
                            val showFeedback = hasAnswered && (currentStudyMode != StudyMode.DRILL_EXAM)

                            val (cardColor, borderColor, contentColor) = when {
                                !showFeedback -> {
                                    if (isSelected) {
                                        Triple(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    } else {
                                        Triple(
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                showFeedback -> {
                                    if (isCorrectOption) {
                                        Triple(
                                            MaritimeCorrectGreen.copy(alpha = 0.15f),
                                            MaritimeCorrectGreen,
                                            MaritimeCorrectGreen
                                        )
                                    } else if (isSelected && !isCorrectOption) {
                                        Triple(
                                            MaritimeIncorrectRed.copy(alpha = 0.15f),
                                            MaritimeIncorrectRed,
                                            MaritimeIncorrectRed
                                        )
                                    } else {
                                        Triple(
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                else -> Triple(Color.Transparent, Color.Transparent, Color.Unspecified)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !hasAnswered) {
                                        sessionAnswers[currentQuestion.id] = optIndex
                                        val wasCorrect = isCorrectOption
                                        sessionResults[currentQuestion.id] = wasCorrect
                                        viewModel.recordPracticeAnswer(currentQuestion, originalIdx)
                                    }
                                    .testTag("option_${letter.lowercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(if (isSelected || (showFeedback && isCorrectOption)) 2.dp else 1.dp, borderColor),
                                colors = CardDefaults.cardColors(containerColor = cardColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (showFeedback && isCorrectOption) MaritimeCorrectGreen
                                        else if (showFeedback && isSelected) MaritimeIncorrectRed
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (showFeedback && isCorrectOption) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            } else if (showFeedback && isSelected && !isCorrectOption) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = letter,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                        color = if (showFeedback && (isCorrectOption || isSelected)) contentColor else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Instant Feedback Card (For Tutor & Speed Run modes)
                    AnimatedVisibility(visible = hasAnswered && currentStudyMode != StudyMode.DRILL_EXAM) {
                        val wasCorrect = sessionResults[currentQuestion.id] == true
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (wasCorrect) MaritimeCorrectGreen.copy(alpha = 0.1f) else MaritimeIncorrectRed.copy(alpha = 0.1f)
                            ),
                            border = BorderStroke(1.dp, if (wasCorrect) MaritimeCorrectGreen else MaritimeIncorrectRed)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (wasCorrect) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (wasCorrect) MaritimeCorrectGreen else MaritimeIncorrectRed
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (wasCorrect) "CORRECT!" else "INCORRECT",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (wasCorrect) MaritimeCorrectGreen else MaritimeIncorrectRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Correct Answer: Option $correctDisplayLetter (${pairedOptions.getOrNull(correctDisplayIndex)?.first ?: ""})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (!currentQuestion.explanation.isNullOrBlank()) {
                                        currentQuestion.explanation
                                    } else {
                                        "Official MARINA Licensure Question."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (!currentQuestion.sourceSheet.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Source Reference: ${currentQuestion.sourceSheet}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Navigation Controls (Previous / Next / Finish)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentIndex > 0) {
                                currentIndex--
                            }
                        },
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("prev_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PREVIOUS")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    val isLastQuestion = currentIndex >= workingQuestions.size - 1
                    Button(
                        onClick = {
                            if (isLastQuestion) {
                                isSessionComplete = true
                            } else {
                                currentIndex++
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("next_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (isLastQuestion) "COMPLETE DRILL" else "NEXT")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }

    // Question Navigator Grid Dialog
    if (showGridDialog && workingQuestions.isNotEmpty()) {
        QuestionNavigatorDialog(
            totalQuestions = workingQuestions.size,
            currentIndex = currentIndex,
            sessionAnswers = sessionAnswers,
            sessionResults = sessionResults,
            workingQuestions = workingQuestions,
            onDismiss = { showGridDialog = false },
            onSelectIndex = { targetIdx ->
                if (targetIdx in workingQuestions.indices) {
                    currentIndex = targetIdx
                }
                showGridDialog = false
            }
        )
    }

    // Report Question Dialog
    if (showReportDialog && currentQuestion != null) {
        var reportReason by remember { mutableStateOf("Wrong answer") }
        var reportDetails by remember { mutableStateOf("") }
        val reasons = listOf(
            "Wrong answer",
            "Possible typo",
            "Duplicate",
            "Missing answer",
            "Incorrect explanation",
            "Unclear question"
        )

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Question #${currentQuestion.id}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select issue type:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    reasons.take(4).forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reportReason = r }
                        ) {
                            RadioButton(selected = reportReason == r, onClick = { reportReason = r })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = r, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    TextField(
                        value = reportDetails,
                        onValueChange = { reportDetails = it },
                        label = { Text("Additional notes (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reportQuestion(currentQuestion.id, reportReason, reportDetails)
                        showReportDialog = false
                    }
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FlashcardInteractiveView(
    question: Question,
    currentIndex: Int,
    totalCount: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onMarkMastered: () -> Unit,
    onMarkReview: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFlip() }
                .testTag("flashcard_interactive_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFlipped) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(2.dp, if (isFlipped) MaritimeGold else MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FLASHCARD • ${currentIndex + 1} OF $totalCount",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isFlipped) "REVERSE (ANSWER)" else "FRONT (QUESTION)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = question.questionText,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (!isFlipped) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = MaritimeGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tap to Reveal Answer & Rationale",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaritimeGold
                        )
                    }
                } else {
                    val correctText = (when (question.correctAnswerIndex) {
                        0 -> question.optionA
                        1 -> question.optionB
                        2 -> question.optionC
                        3 -> question.optionD
                        4 -> question.optionE
                        5 -> question.optionF
                        else -> question.optionA
                    }) ?: ""

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaritimeCorrectGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MaritimeCorrectGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "CORRECT ANSWER (${question.correctAnswerLetter}):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaritimeCorrectGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = correctText,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaritimeCorrectGreen
                            )
                        }
                    }

                    if (!question.explanation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Rationale: ${question.explanation}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Active Recall assessment buttons on flashcard flip
        if (isFlipped) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onMarkReview,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaritimeIncorrectRed),
                    border = BorderStroke(1.dp, MaritimeIncorrectRed),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Needs Review")
                }

                Button(
                    onClick = onMarkMastered,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaritimeCorrectGreen),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mastered!")
                }
            }
        }
    }
}

@Composable
fun SessionScorecard(
    totalQuestions: Int,
    correctCount: Int,
    incorrectCount: Int,
    onRetryMissed: () -> Unit,
    onRepeatDrill: () -> Unit,
    onStudyAllBank: () -> Unit,
    onReturnToBank: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accuracy = if (totalQuestions > 0) (correctCount.toFloat() / totalQuestions * 100).toInt() else 0
    val isPassed = accuracy >= 70

    Card(
        modifier = modifier.testTag("session_scorecard_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (isPassed) MaritimeCorrectGreen.copy(alpha = 0.2f) else MaritimeIncorrectRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPassed) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isPassed) MaritimeCorrectGreen else MaritimeIncorrectRed,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = if (isPassed) "EXCELLENT DRILL!" else "DRILL FINISHED",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )

            Text(
                text = if (isPassed) "You met the MARINA 70% passing standard!" else "Keep practicing to achieve the 70% benchmark.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Accuracy & Score Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$accuracy%",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                        color = if (isPassed) MaritimeCorrectGreen else MaritimeIncorrectRed
                    )
                    Text(
                        text = "$correctCount Correct out of $totalQuestions Total",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            // Stats breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreStat(label = "Correct", value = "$correctCount", color = MaritimeCorrectGreen)
                ScoreStat(label = "Incorrect", value = "$incorrectCount", color = MaritimeIncorrectRed)
                ScoreStat(label = "Standard", value = "70%", color = MaritimeGold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            if (incorrectCount > 0) {
                Button(
                    onClick = onRetryMissed,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaritimeIncorrectRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry $incorrectCount Missed Questions", fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onRepeatDrill,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Shuffle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reshuffle & Repeat Drill", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onStudyAllBank,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Study All Bank Questions", fontWeight = FontWeight.SemiBold)
            }

            TextButton(
                onClick = onReturnToBank,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Question Bank")
            }
        }
    }
}

@Composable
fun ScoreStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionNavigatorDialog(
    totalQuestions: Int,
    currentIndex: Int,
    sessionAnswers: Map<Long, Int>,
    sessionResults: Map<Long, Boolean>,
    workingQuestions: List<Question>,
    onDismiss: () -> Unit,
    onSelectIndex: (Int) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(480.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question Navigator ($totalQuestions)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (0 until totalQuestions).forEach { idx ->
                            val q = workingQuestions.getOrNull(idx)
                            val isCurrent = idx == currentIndex
                            val wasAnswered = q != null && sessionAnswers.containsKey(q.id)
                            val wasCorrect = q != null && sessionResults[q.id] == true
                            val wasIncorrect = q != null && sessionResults.containsKey(q.id) && sessionResults[q.id] == false
                            val isFlagged = q?.isFlagged == true

                            val bgColor = when {
                                isCurrent -> MaterialTheme.colorScheme.primary
                                wasCorrect -> MaritimeCorrectGreen
                                wasIncorrect -> MaritimeIncorrectRed
                                isFlagged -> MaritimeFlagAmber
                                wasAnswered -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val textColor = when {
                                isCurrent || wasCorrect || wasIncorrect || isFlagged -> Color.White
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(bgColor)
                                    .clickable { onSelectIndex(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
