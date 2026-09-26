package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompetencyMetadata
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.HyperionTopBar
import com.example.ui.theme.MaritimeCorrectGreen
import com.example.ui.theme.MaritimeGold
import com.example.ui.theme.MaritimeIncorrectRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val currentTrack by viewModel.currentTrack.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()
    val competencyStats by viewModel.competencyStats.collectAsState()
    val examHistory by viewModel.examHistory.collectAsState()

    val total = allQuestions.size
    val attempted = allQuestions.count { it.timesAttempted > 0 }
    val mastered = allQuestions.count { it.masteryStatus == "MASTERED" }
    val improving = allQuestions.count { it.masteryStatus == "IMPROVING" }
    val needsReview = allQuestions.count { it.masteryStatus == "NEEDS_REVIEW" || it.timesIncorrect > 0 }
    val notAttempted = allQuestions.count { it.timesAttempted == 0 }

    val sumAttempted = allQuestions.sumOf { it.timesAttempted }
    val sumCorrect = allQuestions.sumOf { it.timesCorrect }
    val sumIncorrect = allQuestions.sumOf { it.timesIncorrect }
    val accuracy = if (sumAttempted > 0) (sumCorrect.toFloat() / sumAttempted * 100).toInt() else 0

    Scaffold(
        topBar = {
            HyperionTopBar(
                title = "Analytics & Mastery",
                subtitle = "$currentTrack Performance",
                onBack = { viewModel.navigateBack() }
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
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Summary Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "$currentTrack Overall Performance",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMini("Bank Total", "$total")
                            StatMini("Reviewed", "$attempted")
                            StatMini("Overall Accuracy", "$accuracy%")
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMini("Correct", "$sumCorrect")
                            StatMini("Incorrect", "$sumIncorrect", isAlert = sumIncorrect > 0)
                            StatMini("Mastered", "$mastered")
                        }
                    }
                }
            }

            // Question Mastery Progression
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Question Mastery Progression",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Based on 3 consecutive correct answers = Mastered",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        MasteryBar(label = "Mastered (3x correct)", count = mastered, total = total, color = MaritimeCorrectGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        MasteryBar(label = "Improving (1-2x correct)", count = improving, total = total, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        MasteryBar(label = "Needs Review (errors)", count = needsReview, total = total, color = MaritimeIncorrectRed)
                        Spacer(modifier = Modifier.height(8.dp))
                        MasteryBar(label = "Not Attempted", count = notAttempted, total = total, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Competency Breakdown
            item {
                Text(
                    text = "COMPETENCY BREAKDOWN",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(competencyStats) { stat ->
                val compTitle = CompetencyMetadata.getCompetencyTitle(currentTrack, stat.competencyCode)
                val compAcc = if (stat.attemptedCount > 0) (stat.correctCount.toFloat() / stat.attemptedCount * 100).toInt() else 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stat.competencyCode}: $compTitle",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$compAcc% ($stat.attemptedCount / $stat.totalCount)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (compAcc >= 70) MaritimeCorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { if (stat.totalCount > 0) stat.attemptedCount.toFloat() / stat.totalCount else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            // Exam History Section
            item {
                Text(
                    text = "MOCK EXAMINATION HISTORY",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            if (examHistory.isEmpty()) {
                item {
                    Text(
                        text = "No completed mock examinations yet. Take a simulated exam to record results.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(examHistory) { exam ->
                    val dateFormatted = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(exam.timestamp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(AppScreen.ExamResult(exam.id)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exam.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "$dateFormatted • ${exam.totalQuestions} Questions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (exam.passed) MaritimeCorrectGreen else MaritimeIncorrectRed
                                ) {
                                    Text(
                                        text = if (exam.passed) "PASS" else "FAIL",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${exam.score}/${exam.totalQuestions} (${String.format("%.1f", exam.percentage)}%)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
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

@Composable
fun MasteryBar(
    label: String,
    count: Int,
    total: Int,
    color: androidx.compose.ui.graphics.Color
) {
    val progress = if (total > 0) count.toFloat() / total else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = "$count (${(progress * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color
        )
    }
}
