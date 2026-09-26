package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CompetencyCount
import com.example.data.model.CompetencyMetadata
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.PracticeFilterMode
import com.example.ui.StudyMode
import com.example.ui.components.HyperionTopBar
import com.example.ui.components.StudyConfigDialog
import com.example.ui.theme.MaritimeGold

data class ConfigDialogState(
    val title: String,
    val subtitle: String,
    val totalCount: Int,
    val competencyCode: String? = null,
    val partNumber: Int? = null
)

@Composable
fun QuestionBankScreen(viewModel: MainViewModel) {
    BackHandler {
        viewModel.navigateBack()
    }

    val currentTrack by viewModel.currentTrack.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()
    val competencyStats by viewModel.competencyStats.collectAsState()

    val statsMap = competencyStats.associateBy { it.competencyCode }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf("ALL") }
    var activeConfigDialog by remember { mutableStateOf<ConfigDialogState?>(null) }

    Scaffold(
        topBar = {
            HyperionTopBar(
                title = "$currentTrack Question Bank",
                subtitle = "Total: ${allQuestions.size} Questions",
                onBack = { viewModel.navigateBack() },
                actions = {
                    IconButton(
                        onClick = {
                            activeConfigDialog = ConfigDialogState(
                                title = "$currentTrack Complete Bank",
                                subtitle = "Practice questions across all competencies",
                                totalCount = allQuestions.size
                            )
                        },
                        modifier = Modifier.testTag("open_bank_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Configure Study",
                            tint = MaritimeGold
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Master Action Hub Banner
            item {
                Spacer(modifier = Modifier.height(4.dp))
                MasterStudyHub(
                    track = currentTrack,
                    totalCount = allQuestions.size,
                    onStudyAll = {
                        viewModel.requestStartPractice(
                            AppScreen.Practice(
                                title = "$currentTrack (All Questions)",
                                filterMode = PracticeFilterMode.ALL,
                                itemLimit = null,
                                isRandomized = false
                            )
                        )
                    },
                    onRandomBlitz = {
                        viewModel.requestStartPractice(
                            AppScreen.Practice(
                                title = "$currentTrack (Random 25 Blitz)",
                                filterMode = PracticeFilterMode.ALL,
                                itemLimit = 25.coerceAtMost(allQuestions.size),
                                isRandomized = true,
                                shuffleOptions = true
                            )
                        )
                    },
                    onConfigureCustom = {
                        activeConfigDialog = ConfigDialogState(
                            title = "$currentTrack Complete Bank",
                            subtitle = "Customize question count, shuffle, and study modes",
                            totalCount = allQuestions.size
                        )
                    }
                )
            }

            // Search & Filter Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bank_search_input"),
                        placeholder = { Text("Filter competencies by name or code (e.g. ECDIS, F1, C10)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Category Chips (F1, F2, F3 for OIC-NW; C1, C2 for GMDSS)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val categories = if (currentTrack == "OIC-NW") {
                            listOf(
                                "ALL" to "All Functions",
                                "F1" to "F1 – Navigation",
                                "F2" to "F2 – Cargo Handling",
                                "F3" to "F3 – Ship Operations"
                            )
                        } else {
                            listOf(
                                "ALL" to "All Competencies",
                                "C1" to "C1 – Subsystems & Equipment",
                                "C2" to "C2 – Radio Emergencies"
                            )
                        }

                        items(categories) { (key, label) ->
                            val isSelected = selectedFilterCategory == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilterCategory = key },
                                label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            if (currentTrack == "OIC-NW") {
                // Group by Functions: F1, F2, F3
                val allF1 = CompetencyMetadata.OIC_NW_COMPETENCIES.filter { it.functionCode == "F1" }
                val allF2 = CompetencyMetadata.OIC_NW_COMPETENCIES.filter { it.functionCode == "F2" }
                val allF3 = CompetencyMetadata.OIC_NW_COMPETENCIES.filter { it.functionCode == "F3" }

                val f1Comps = allF1.filter { comp ->
                    (selectedFilterCategory == "ALL" || selectedFilterCategory == "F1") &&
                            (searchQuery.isBlank() || comp.code.contains(searchQuery, ignoreCase = true) || comp.title.contains(searchQuery, ignoreCase = true))
                }
                val f2Comps = allF2.filter { comp ->
                    (selectedFilterCategory == "ALL" || selectedFilterCategory == "F2") &&
                            (searchQuery.isBlank() || comp.code.contains(searchQuery, ignoreCase = true) || comp.title.contains(searchQuery, ignoreCase = true))
                }
                val f3Comps = allF3.filter { comp ->
                    (selectedFilterCategory == "ALL" || selectedFilterCategory == "F3") &&
                            (searchQuery.isBlank() || comp.code.contains(searchQuery, ignoreCase = true) || comp.title.contains(searchQuery, ignoreCase = true))
                }

                if (f1Comps.isNotEmpty()) {
                    item {
                        FunctionHeader(
                            title = "F1 – Navigation at the Operational Level",
                            subtitle = "7 Competencies (C1, C2, C3, C4, C5, C7, C9) • Official Exam Quota: 125 Items"
                        )
                    }
                    items(f1Comps) { comp ->
                        val stat = statsMap[comp.code]
                        val totalQuestions = stat?.totalCount ?: allQuestions.count { it.competencyCode.equals(comp.code, ignoreCase = true) }
                        CompetencyStudyCard(
                            code = comp.code,
                            title = comp.title,
                            examQuota = comp.examQuota,
                            stat = stat,
                            totalQuestions = totalQuestions,
                            onStudyAll = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "${comp.code} – ${comp.title}",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = comp.code,
                                        itemLimit = null,
                                        isRandomized = false
                                    )
                                )
                            },
                            onRandom20 = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "${comp.code} (Random 20)",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = comp.code,
                                        itemLimit = 20.coerceAtMost(totalQuestions),
                                        isRandomized = true,
                                        shuffleOptions = true
                                    )
                                )
                            },
                            onConfigure = {
                                activeConfigDialog = ConfigDialogState(
                                    title = "${comp.code} – ${comp.title}",
                                    subtitle = "Official Exam Quota: ${comp.examQuota} items",
                                    totalCount = totalQuestions,
                                    competencyCode = comp.code
                                )
                            }
                        )
                    }
                }

                if (f2Comps.isNotEmpty()) {
                    item {
                        FunctionHeader(
                            title = "F2 – Cargo Handling and Stowage",
                            subtitle = "2 Competencies (C10, C11) • Official Exam Quota: 30 Items"
                        )
                    }
                    items(f2Comps) { comp ->
                        val stat = statsMap[comp.code]
                        val totalQuestions = stat?.totalCount ?: allQuestions.count { it.competencyCode.equals(comp.code, ignoreCase = true) }
                        CompetencyStudyCard(
                            code = comp.code,
                            title = comp.title,
                            examQuota = comp.examQuota,
                            stat = stat,
                            totalQuestions = totalQuestions,
                            onStudyAll = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "${comp.code} – ${comp.title}",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = comp.code,
                                        itemLimit = null,
                                        isRandomized = false
                                    )
                                )
                            },
                            onRandom20 = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "${comp.code} (Random 20)",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = comp.code,
                                        itemLimit = 20.coerceAtMost(totalQuestions),
                                        isRandomized = true,
                                        shuffleOptions = true
                                    )
                                )
                            },
                            onConfigure = {
                                activeConfigDialog = ConfigDialogState(
                                    title = "${comp.code} – ${comp.title}",
                                    subtitle = "Official Exam Quota: ${comp.examQuota} items",
                                    totalCount = totalQuestions,
                                    competencyCode = comp.code
                                )
                            }
                        )
                    }
                }

                if (f3Comps.isNotEmpty()) {
                    item {
                        FunctionHeader(
                            title = "F3 – Controlling Ship Operation & Persons on Board",
                            subtitle = "3 Competencies (C12, C13, C17) • Official Exam Quota: 45 Items"
                        )
                    }
                    items(f3Comps) { comp ->
                        val stat = statsMap[comp.code]
                        val totalQuestions = stat?.totalCount ?: allQuestions.count { it.competencyCode.equals(comp.code, ignoreCase = true) }
                        CompetencyStudyCard(
                            code = comp.code,
                            title = comp.title,
                            examQuota = comp.examQuota,
                            stat = stat,
                            totalQuestions = totalQuestions,
                            onStudyAll = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "${comp.code} – ${comp.title}",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = comp.code,
                                        itemLimit = null,
                                        isRandomized = false
                                    )
                                )
                            },
                            onRandom20 = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "${comp.code} (Random 20)",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = comp.code,
                                        itemLimit = 20.coerceAtMost(totalQuestions),
                                        isRandomized = true,
                                        shuffleOptions = true
                                    )
                                )
                            },
                            onConfigure = {
                                activeConfigDialog = ConfigDialogState(
                                    title = "${comp.code} – ${comp.title}",
                                    subtitle = "Official Exam Quota: ${comp.examQuota} items",
                                    totalCount = totalQuestions,
                                    competencyCode = comp.code
                                )
                            }
                        )
                    }
                }
            } else {
                // GMDSS Competencies C1 and C2 with Part sub-navigation
                if (selectedFilterCategory == "ALL" || selectedFilterCategory == "C1") {
                    item {
                        FunctionHeader(
                            title = "C1 – Subsystems & Functional Requirements",
                            subtitle = "Official Exam: 85 Items • Reviewer divided into Parts 1–10"
                        )
                    }

                    // C1 All master card
                    val c1Count = statsMap["C1"]?.totalCount ?: allQuestions.count { it.competencyCode == "C1" }
                    item {
                        GmdssMasterCard(
                            code = "C1",
                            title = "C1 – All Questions Combined",
                            subtitle = "Practice all parts combined ($c1Count Questions)",
                            count = c1Count,
                            onStudyAll = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "GMDSS C1 – All Questions",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = "C1",
                                        itemLimit = null,
                                        isRandomized = false
                                    )
                                )
                            },
                            onRandom25 = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "GMDSS C1 (Random 25)",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = "C1",
                                        itemLimit = 25.coerceAtMost(c1Count),
                                        isRandomized = true,
                                        shuffleOptions = true
                                    )
                                )
                            },
                            onConfigure = {
                                activeConfigDialog = ConfigDialogState(
                                    title = "GMDSS C1 – All Subsystems",
                                    subtitle = "Full Question Bank",
                                    totalCount = c1Count,
                                    competencyCode = "C1"
                                )
                            }
                        )
                    }

                    // C1 Parts 1..10
                    items((1..10).toList()) { part ->
                        val partQuestions = allQuestions.filter { it.competencyCode == "C1" && it.partNumber == part }
                        val partCount = partQuestions.size
                        val partAttempted = partQuestions.count { it.timesAttempted > 0 }

                        PartStudyCard(
                            competency = "C1",
                            partNumber = part,
                            count = partCount,
                            attempted = partAttempted,
                            onStudyAll = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "GMDSS C1 – Part $part",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = "C1",
                                        partNumber = part,
                                        itemLimit = null,
                                        isRandomized = false
                                    )
                                )
                            },
                            onConfigure = {
                                activeConfigDialog = ConfigDialogState(
                                    title = "GMDSS C1 – Part $part",
                                    subtitle = "$partCount questions available",
                                    totalCount = partCount,
                                    competencyCode = "C1",
                                    partNumber = part
                                )
                            }
                        )
                    }
                }

                if (selectedFilterCategory == "ALL" || selectedFilterCategory == "C2") {
                    item {
                        FunctionHeader(
                            title = "C2 – Radio Services in Emergencies",
                            subtitle = "Official Exam: 15 Items • Reviewer divided into Parts 1–2"
                        )
                    }

                    val c2Count = statsMap["C2"]?.totalCount ?: allQuestions.count { it.competencyCode == "C2" }
                    // C2 All master card
                    item {
                        GmdssMasterCard(
                            code = "C2",
                            title = "C2 – All Questions Combined",
                            subtitle = "Practice all emergency radio questions ($c2Count Questions)",
                            count = c2Count,
                            onStudyAll = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "GMDSS C2 – All Questions",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = "C2",
                                        itemLimit = null,
                                        isRandomized = false
                                    )
                                )
                            },
                            onRandom25 = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "GMDSS C2 (Random 25)",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = "C2",
                                        itemLimit = 25.coerceAtMost(c2Count),
                                        isRandomized = true,
                                        shuffleOptions = true
                                    )
                                )
                            },
                            onConfigure = {
                                activeConfigDialog = ConfigDialogState(
                                    title = "GMDSS C2 – Emergency Radiocommunications",
                                    subtitle = "Full Question Bank",
                                    totalCount = c2Count,
                                    competencyCode = "C2"
                                )
                            }
                        )
                    }

                    // C2 Parts 1..2
                    items((1..2).toList()) { part ->
                        val partQuestions = allQuestions.filter { it.competencyCode == "C2" && it.partNumber == part }
                        val partCount = partQuestions.size
                        val partAttempted = partQuestions.count { it.timesAttempted > 0 }

                        PartStudyCard(
                            competency = "C2",
                            partNumber = part,
                            count = partCount,
                            attempted = partAttempted,
                            onStudyAll = {
                                viewModel.navigateTo(
                                    AppScreen.Practice(
                                        title = "GMDSS C2 – Part $part",
                                        filterMode = PracticeFilterMode.ALL,
                                        competencyCode = "C2",
                                        partNumber = part,
                                        itemLimit = null,
                                        isRandomized = false
                                    )
                                )
                            },
                            onConfigure = {
                                activeConfigDialog = ConfigDialogState(
                                    title = "GMDSS C2 – Part $part",
                                    subtitle = "$partCount questions available",
                                    totalCount = partCount,
                                    competencyCode = "C2",
                                    partNumber = part
                                )
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Study Config Dialog Modal
    activeConfigDialog?.let { dialogState ->
        StudyConfigDialog(
            title = dialogState.title,
            subtitle = dialogState.subtitle,
            totalAvailable = dialogState.totalCount,
            onDismiss = { activeConfigDialog = null },
            onStartStudy = { limit, isRandomized, shuffleOptions, studyMode, timeLimit ->
                val titleSuffix = when {
                    limit != null && isRandomized -> " (Random $limit)"
                    limit != null -> " ($limit Items)"
                    isRandomized -> " (Shuffled)"
                    else -> " (All Questions)"
                }
                viewModel.requestStartPractice(
                    AppScreen.Practice(
                        title = "${dialogState.title}$titleSuffix",
                        filterMode = PracticeFilterMode.ALL,
                        competencyCode = dialogState.competencyCode,
                        partNumber = dialogState.partNumber,
                        itemLimit = limit,
                        isRandomized = isRandomized,
                        shuffleOptions = shuffleOptions,
                        studyMode = studyMode,
                        timeLimitSecondsPerItem = timeLimit
                    )
                )
                activeConfigDialog = null
            }
        )
    }
}

@Composable
fun MasterStudyHub(
    track: String,
    totalCount: Int,
    onStudyAll: () -> Unit,
    onRandomBlitz: () -> Unit,
    onConfigureCustom: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("master_study_hub_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "STUDY LAB OPTIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Tailor Your Review Session",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "$totalCount Items",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStudyAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("study_all_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Study All ($totalCount)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onRandomBlitz,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("random_blitz_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Random 25", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onConfigureCustom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("custom_study_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Customize Session (Limit, Order, Modes)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CompetencyStudyCard(
    code: String,
    title: String,
    examQuota: Int,
    stat: CompetencyCount?,
    totalQuestions: Int,
    onStudyAll: () -> Unit,
    onRandom20: () -> Unit,
    onConfigure: () -> Unit
) {
    val attempted = stat?.attemptedCount ?: 0
    val progress = if (totalQuestions > 0) attempted.toFloat() / totalQuestions else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onConfigure() }
            .testTag("competency_card_$code"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 2
                    )
                    Text(
                        text = "Bank: $totalQuestions questions • Official Exam: $examQuota items",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onConfigure) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Configure $code",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (totalQuestions > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Action Buttons Row on the card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStudyAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Study All ($totalQuestions)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onRandom20,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Random 20", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun GmdssMasterCard(
    code: String,
    title: String,
    subtitle: String,
    count: Int,
    onStudyAll: () -> Unit,
    onRandom25: () -> Unit,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onConfigure() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onConfigure) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Configure $code",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStudyAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Study All ($count)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onRandom25,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Random 25", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PartStudyCard(
    competency: String,
    partNumber: Int,
    count: Int,
    attempted: Int,
    onStudyAll: () -> Unit,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onConfigure() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$partNumber",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Part $partNumber",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "$count questions • $attempted reviewed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onStudyAll,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Study All", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = onConfigure) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Configure Part $partNumber",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FunctionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
