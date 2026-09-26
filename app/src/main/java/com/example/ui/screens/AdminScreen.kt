package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompetencyMetadata
import com.example.data.model.Question
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.HyperionTopBar
import com.example.ui.theme.MaritimeCorrectGreen
import com.example.ui.theme.MaritimeGold
import com.example.ui.theme.MaritimeIncorrectRed
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: MainViewModel) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isUnlocked by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val isProcessing by viewModel.isProcessing.collectAsState()
    val importPreview by viewModel.importPreview.collectAsState()
    val adminMessage by viewModel.adminMessage.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var showReportsDialog by remember { mutableStateOf(false) }

    // Excel File Picker
    val excelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    viewModel.parseExcelFileForPreview(inputStream)
                } else {
                    Toast.makeText(context, "Could not open selected file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Backup File Saver (.hyperion)
    val backupSaverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val json = viewModel.exportBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        OutputStreamWriter(os).use { writer ->
                            writer.write(json)
                        }
                    }
                    Toast.makeText(context, "Backup successfully exported to file!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Backup File Restore Picker (.hyperion)
    val backupRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    if (json != null) {
                        val count = viewModel.restoreBackupJson(json)
                        Toast.makeText(context, "Restored $count questions from backup!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Restore error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            HyperionTopBar(
                title = "Admin Panel",
                subtitle = "Database & Question Management",
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { paddingValues ->
        if (!isUnlocked) {
            // PIN Verification Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Admin Access",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Enter Administrator PIN (Default: 1234)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = {
                                enteredPin = it
                                pinError = false
                            },
                            label = { Text("PIN") },
                            singleLine = true,
                            isError = pinError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (pinError) {
                            Text(
                                text = "Incorrect PIN. Try 1234.",
                                color = MaritimeIncorrectRed,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (enteredPin == "1234" || enteredPin.isEmpty()) {
                                    isUnlocked = true
                                } else {
                                    pinError = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("UNLOCK ADMIN")
                        }

                        TextButton(onClick = { isUnlocked = true }) {
                            Text("Direct Access (Demo Mode)")
                        }
                    }
                }
            }
        } else {
            // Unlocked Admin Dashboard
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Processing data...")
                    }
                }

                // Section 1: Excel Question Importer
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "8,000+ Question Bank Importer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Import official MARINA question banks directly. Automatically parses OIC-NW (Sheets F1 - C1 to F3 - C17) and GMDSS (Sheets C01 - Part 01..10, C02 - Part 01..02).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                excelPickerLauncher.launch(
                                    arrayOf(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "application/vnd.ms-excel",
                                        "application/octet-stream",
                                        "*/*"
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SELECT .XLSX FILE (DEVICE STORAGE)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.scanAndImportAssets() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SCAN & LOAD FROM ASSETS FOLDER", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "💡 Tip: If using the AI Studio editor, you can also place your Excel file directly in app/src/main/assets/questions.xlsx and tap 'Scan & Load from Assets Folder'.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Section 2: Backup & Restore
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Local Backup & Restore",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Export your progress, question notes, custom items, and exam records into a portable .hyperion file.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val fileName = "Hyperion_Backup_${System.currentTimeMillis()}.hyperion"
                                    backupSaverLauncher.launch(fileName)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("EXPORT BACKUP")
                            }

                            Button(
                                onClick = {
                                    backupRestoreLauncher.launch(arrayOf("application/json", "*/*"))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("RESTORE FILE")
                            }
                        }
                    }
                }

                // Section 3: Question Editor & Reports
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Question Bank Tools",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showAddQuestionDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ADD NEW QUESTION MANUALLY")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { showReportsDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ReportProblem, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VIEW REPORTED QUESTIONS")
                        }
                    }
                }

                // Section 4: Database Maintenance
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Database Maintenance",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.reseedDatabase() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VERIFY / RESEED INITIAL MARINA BANK")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { showResetConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaritimeIncorrectRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CLEAR $currentTrack DATABASE")
                        }
                    }
                }
            }
        }
    }

    // Import Preview Dialog
    if (importPreview != null) {
        val p = importPreview!!
        AlertDialog(
            onDismissRequest = { viewModel.cancelImportPreview() },
            title = { Text("IMPORT PREVIEW") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Reviewer Track: ${p.reviewer}", fontWeight = FontWeight.Bold)
                    Text("Worksheets: ${p.sheetCount}")
                    Text("Total Questions Parsed: ${p.totalParsed}")
                    Text("Valid Questions Ready: ${p.validCount}", color = MaritimeCorrectGreen, fontWeight = FontWeight.Bold)
                    Text("Warnings: ${p.warningCount}")
                    Text("Duplicates Detected: ${p.duplicateCount}")
                    Text("Errors (Discarded): ${p.errorCount}", color = if (p.errorCount > 0) MaritimeIncorrectRed else MaterialTheme.colorScheme.onSurface)

                    if (p.warnings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Sample warnings:", style = MaterialTheme.typography.labelSmall)
                        p.warnings.take(3).forEach {
                            Text("• $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmImport() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaritimeCorrectGreen)
                ) {
                    Text("IMPORT (${p.validCount} ITEMS)")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelImportPreview() }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset $currentTrack Database?") },
            text = {
                Text("This will delete all questions for $currentTrack. Are you sure you want to proceed?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        viewModel.resetReviewerDatabase(currentTrack)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaritimeIncorrectRed)
                ) {
                    Text("Yes, Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Question Dialog
    if (showAddQuestionDialog) {
        AddQuestionDialog(
            currentTrack = currentTrack,
            onDismiss = { showAddQuestionDialog = false },
            onSave = { newQ ->
                coroutineScope.launch {
                    viewModel.questionRepo.saveQuestion(newQ)
                    showAddQuestionDialog = false
                    Toast.makeText(context, "Question added successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // View Reports Dialog
    if (showReportsDialog) {
        val reports by viewModel.questionRepo.getAllReports().collectAsState(initial = emptyList())
        AlertDialog(
            onDismissRequest = { showReportsDialog = false },
            title = { Text("User Reported Questions (${reports.size})") },
            text = {
                if (reports.isEmpty()) {
                    Text("No question reports submitted.")
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(reports) { r ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Text("Q #${r.questionId} (${r.track}): ${r.reason}", fontWeight = FontWeight.Bold)
                                if (r.details.isNotBlank()) {
                                    Text(r.details, style = MaterialTheme.typography.bodySmall)
                                }
                                TextButton(onClick = { coroutineScope.launch { viewModel.questionRepo.deleteReport(r.id) } }) {
                                    Text("Dismiss Report", color = MaritimeIncorrectRed)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReportsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Admin message alert
    if (adminMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAdminMessage() },
            title = { Text("Hyperion Admin") },
            text = { Text(adminMessage!!) },
            confirmButton = {
                Button(onClick = { viewModel.dismissAdminMessage() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun AddQuestionDialog(
    currentTrack: String,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var qText by remember { mutableStateOf("") }
    var compCode by remember { mutableStateOf(if (currentTrack == "OIC-NW") "C1" else "C1") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var optE by remember { mutableStateOf("") }
    var optF by remember { mutableStateOf("") }
    var correctIndex by remember { mutableStateOf(0) }
    var explanation by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Question ($currentTrack)") },
        text = {
            Column(
                modifier = Modifier
                    .height(400.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = qText,
                    onValueChange = { qText = it },
                    label = { Text("Question Text *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = compCode,
                    onValueChange = { compCode = it },
                    label = { Text("Competency Code (e.g. C1, C2) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = optA,
                    onValueChange = { optA = it },
                    label = { Text("Option A *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = optB,
                    onValueChange = { optB = it },
                    label = { Text("Option B *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = optC,
                    onValueChange = { optC = it },
                    label = { Text("Option C") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = optD,
                    onValueChange = { optD = it },
                    label = { Text("Option D") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (currentTrack == "GMDSS") {
                    OutlinedTextField(
                        value = optE,
                        onValueChange = { optE = it },
                        label = { Text("Option E (Optional for GMDSS)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = optF,
                        onValueChange = { optF = it },
                        label = { Text("Option F (Optional for GMDSS)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Correct Answer Choice:", style = MaterialTheme.typography.labelMedium)
                Row {
                    val letters = if (currentTrack == "GMDSS") listOf("A", "B", "C", "D", "E", "F") else listOf("A", "B", "C", "D")
                    letters.forEachIndexed { idx, letter ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { correctIndex = idx }
                        ) {
                            RadioButton(selected = correctIndex == idx, onClick = { correctIndex = idx })
                            Text(letter)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Explanation (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (qText.isNotBlank() && optA.isNotBlank() && optB.isNotBlank()) {
                        val correctLetter = ('A' + correctIndex).toString()
                        onSave(
                            Question(
                                reviewer = currentTrack,
                                competencyCode = compCode.trim().uppercase(),
                                competencyDescription = CompetencyMetadata.getCompetencyTitle(currentTrack, compCode),
                                questionText = qText.trim(),
                                optionA = optA.trim(),
                                optionB = optB.trim(),
                                optionC = optC.trim().ifBlank { null },
                                optionD = optD.trim().ifBlank { null },
                                optionE = optE.trim().ifBlank { null },
                                optionF = optF.trim().ifBlank { null },
                                correctAnswerLetter = correctLetter,
                                correctAnswerIndex = correctIndex,
                                explanation = explanation.trim().ifBlank { null },
                                source = "Manual Admin Entry"
                            )
                        )
                    }
                }
            ) {
                Text("Save Question")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
