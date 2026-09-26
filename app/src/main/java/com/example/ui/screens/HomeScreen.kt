package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompetencyMetadata
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.PracticeFilterMode
import com.example.ui.components.HyperionTopBar
import com.example.ui.components.OngoingSessionCard
import com.example.ui.components.SessionLogsSheet
import com.example.ui.theme.MaritimeCorrectGreen
import com.example.ui.theme.MaritimeFavoriteGold
import com.example.ui.theme.MaritimeFlagAmber
import com.example.ui.theme.MaritimeGold
import com.example.ui.theme.MaritimeIncorrectRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    BackHandler {
        viewModel.navigateTo(AppScreen.TrackSelect)
    }

    val currentTrack by viewModel.currentTrack.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val flagged by viewModel.flagged.collectAsState()
    val incorrect by viewModel.incorrect.collectAsState()
    val competencyStats by viewModel.competencyStats.collectAsState()
    val sessionLogs by viewModel.sessionLogs.collectAsState()
    var showLogsSheet by remember { mutableStateOf(false) }

    val totalCount = allQuestions.size
    val attemptedCount = allQuestions.count { it.timesAttempted > 0 }
    val masteredCount = allQuestions.count { it.masteryStatus == "MASTERED" }
    val totalTimesAttempted = allQuestions.sumOf { it.timesAttempted }
    val totalTimesCorrect = allQuestions.sumOf { it.timesCorrect }
    val accuracy = if (totalTimesAttempted > 0) {
        (totalTimesCorrect.toFloat() / totalTimesAttempted * 100).toInt()
    } else 0

    Scaffold(
        topBar = {
            HyperionTopBar(
                title = "HYPERION",
                subtitle = "MARINA Reviewer • $currentTrack",
                actions = {
                    // Switch Track button
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.TrackSelect) },
                        modifier = Modifier.testTag("switch_track_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Track",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Admin button
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.Admin) },
                        modifier = Modifier.testTag("admin_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Track Switcher Pill
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.TrackSelect) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentTrack == "OIC-NW") "⚓ OIC-NW Track" else "📡 GMDSS Track",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "$totalCount Questions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Ongoing Session in Progress (Resumable Session Log)
            if (sessionLogs.isNotEmpty()) {
                item {
                    val latest = sessionLogs.first()
                    OngoingSessionCard(
                        session = latest,
                        totalLogsCount = sessionLogs.size,
                        onResume = { viewModel.resumeSession(latest) },
                        onDelete = { viewModel.deleteSessionLog(latest.id) },
                        onViewAllLogs = { showLogsSheet = true }
                    )
                }
            }

            // Primary Overview Card
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("track_overview_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (currentTrack == "OIC-NW") "OIC-NW Navigation Watch" else "GMDSS Radio Safety",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                                )
                                Text(
                                    text = "Philippine MARINA Licensure Master Bank",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Accuracy Circle/Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (accuracy >= 70) MaritimeCorrectGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "$accuracy% ACCURACY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (accuracy >= 70) MaritimeCorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats metrics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMini(label = "Bank Total", value = "$totalCount")
                            StatMini(label = "Attempted", value = "$attemptedCount")
                            StatMini(label = "Mastered", value = "$masteredCount")
                            StatMini(label = "Incorrect", value = "${incorrect.size}", isAlert = incorrect.isNotEmpty())
                        }

                        val progress = if (totalCount > 0) attemptedCount.toFloat() / totalCount else 0f
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.requestStartPractice(
                                        AppScreen.Practice(
                                            title = "$currentTrack Review (All)",
                                            filterMode = PracticeFilterMode.ALL
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                                    .testTag("continue_review_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "STUDY ALL",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.requestStartPractice(
                                        AppScreen.Practice(
                                            title = "$currentTrack (Random 25 Blitz)",
                                            filterMode = PracticeFilterMode.ALL,
                                            itemLimit = 25.coerceAtMost(if (allQuestions.isNotEmpty()) allQuestions.size else 25),
                                            isRandomized = true,
                                            shuffleOptions = true
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(48.dp)
                                    .testTag("random_25_home_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "RANDOM 25",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // 8,000+ Question Bank Loading Hub (Shows when bank count is under 500)
            if (totalCount < 500) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(AppScreen.Admin) }
                            .testTag("import_8000_questions_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ready to Load 8,000+ Questions",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Tap to import your MARINA Excel reviewer (.xlsx) into the offline database.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Quick Actions Section Title
            item {
                Text(
                    text = "QUICK ACTIONS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Quick Actions Grid (2 columns)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "Question Bank",
                            subtitle = "Browse by Competency",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            accentColor = MaterialTheme.colorScheme.primary,
                            testTag = "action_question_bank",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.navigateTo(AppScreen.QuestionBank)
                        }

                        ActionCard(
                            title = "Mock Exam",
                            subtitle = "Official Simulation",
                            icon = Icons.Default.Assignment,
                            accentColor = MaritimeGold,
                            testTag = "action_mock_exam",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.navigateTo(AppScreen.ExamSetup)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "My Incorrect",
                            subtitle = "${incorrect.size} questions to retry",
                            icon = Icons.Default.Cancel,
                            accentColor = MaritimeIncorrectRed,
                            badge = if (incorrect.isNotEmpty()) "${incorrect.size}" else null,
                            testTag = "action_incorrect",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.navigateTo(AppScreen.Incorrect)
                        }

                        ActionCard(
                            title = "Smart Review",
                            subtitle = "Weak areas & priority",
                            icon = Icons.Default.Psychology,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            testTag = "action_smart_review",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.requestStartPractice(
                                AppScreen.Practice(
                                    title = "Smart Review",
                                    filterMode = PracticeFilterMode.SMART_REVIEW
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "Favorites",
                            subtitle = "${favorites.size} starred",
                            icon = Icons.Default.Star,
                            accentColor = MaritimeFavoriteGold,
                            testTag = "action_favorites",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.navigateTo(AppScreen.Favorites)
                        }

                        ActionCard(
                            title = "Flagged",
                            subtitle = "${flagged.size} flagged items",
                            icon = Icons.Default.Flag,
                            accentColor = MaritimeFlagAmber,
                            testTag = "action_flagged",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.navigateTo(AppScreen.Flagged)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "Search Bank",
                            subtitle = "Offline keyword lookup",
                            icon = Icons.Default.Search,
                            accentColor = MaterialTheme.colorScheme.primary,
                            testTag = "action_search",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.navigateTo(AppScreen.Search)
                        }

                        ActionCard(
                            title = "Statistics",
                            subtitle = "History & Mastery",
                            icon = Icons.Default.BarChart,
                            accentColor = MaritimeCorrectGreen,
                            testTag = "action_statistics",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.navigateTo(AppScreen.Stats)
                        }
                    }

                    // Study Timer Row
                    ActionCard(
                        title = "Study Timer & Goals",
                        subtitle = "Track active study hours and daily targets",
                        icon = Icons.Default.Timer,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        testTag = "action_study_timer",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        viewModel.navigateTo(AppScreen.StudyTimer)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showLogsSheet) {
        SessionLogsSheet(
            logs = sessionLogs,
            onDismiss = { showLogsSheet = false },
            onResume = { log ->
                showLogsSheet = false
                viewModel.resumeSession(log)
            },
            onDeleteLog = { id ->
                viewModel.deleteSessionLog(id)
            },
            onClearAllLogs = {
                showLogsSheet = false
                viewModel.clearAllSessionLogs(trackOnly = true)
            }
        )
    }
}

@Composable
fun StatMini(label: String, value: String, isAlert: Boolean = false) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isAlert) MaritimeIncorrectRed else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badge: String? = null,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
