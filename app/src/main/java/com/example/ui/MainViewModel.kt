package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.importer.BackupManager
import com.example.data.importer.ExcelImporter
import com.example.data.importer.ImportValidationResult
import com.example.data.local.AppDatabase
import com.example.data.local.CompetencyCount
import com.example.data.local.DatabaseSeeder
import com.example.data.local.PartCount
import com.example.data.model.DailyGoal
import com.example.data.model.ExamAnswer
import com.example.data.model.ExamRecord
import com.example.data.model.PracticeSessionPayload
import com.example.data.model.ExamSessionPayload
import com.example.data.model.Question
import com.example.data.model.SavedSessionLog
import com.example.data.model.StudySession
import com.example.data.repository.ExamRepository
import com.example.data.repository.QuestionRepository
import com.example.data.repository.SessionLogRepository
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream

sealed class AppScreen {
    object TrackSelect : AppScreen()
    object Home : AppScreen()
    object QuestionBank : AppScreen()
    data class Practice(
        val title: String,
        val filterMode: PracticeFilterMode = PracticeFilterMode.ALL,
        val competencyCode: String? = null,
        val partNumber: Int? = null,
        val customQuestionIds: List<Long>? = null,
        val itemLimit: Int? = null,
        val isRandomized: Boolean = false,
        val shuffleOptions: Boolean = false,
        val studyMode: StudyMode = StudyMode.TUTOR,
        val timeLimitSecondsPerItem: Int? = null,
        val resumeSessionLogId: Long? = null,
        val initialIndex: Int = 0,
        val initialAnswers: Map<Long, Int> = emptyMap(),
        val initialResults: Map<Long, Boolean> = emptyMap(),
        val randomSeed: Int = 1
    ) : AppScreen()
    object ExamSetup : AppScreen()
    object ExamRunner : AppScreen()
    data class ExamResult(val recordId: Long) : AppScreen()
    data class ExamReview(val recordId: Long) : AppScreen()
    object Incorrect : AppScreen()
    object Favorites : AppScreen()
    object Flagged : AppScreen()
    object Search : AppScreen()
    object Stats : AppScreen()
    object StudyTimer : AppScreen()
    object Admin : AppScreen()
    data class EditQuestion(val questionId: Long = 0L) : AppScreen()
}

enum class StudyMode {
    TUTOR,       // Immediate feedback + full explanation
    DRILL_EXAM,  // Answer without hints, view final scorecard
    FLASHCARD,   // Tap-to-flip flashcard with active recall
    SPEED_RUN    // Rapid-fire timed question challenge
}

enum class PracticeFilterMode {
    ALL,
    UNANSWERED,
    INCORRECT,
    FAVORITES,
    FLAGGED,
    NEEDS_REVIEW,
    MASTERED,
    SMART_REVIEW
}

data class ActiveExamState(
    val title: String = "",
    val track: String = "OIC-NW",
    val examType: String = "OFFICIAL_SIMULATION",
    val questions: List<Question> = emptyList(),
    val userAnswers: Map<Long, Int> = emptyMap(), // questionId -> selectedIndex
    val flaggedQuestionIds: Set<Long> = emptySet(),
    val currentIndex: Int = 0,
    val timeLimitSeconds: Long = 0,
    val timeRemainingSeconds: Long = 0,
    val isSubmitted: Boolean = false,
    val completedRecordId: Long? = null,
    val savedSessionLogId: Long? = null
)

data class OngoingSessionPromptState(
    val existingSession: SavedSessionLog,
    val newSessionTitle: String,
    val onStartNew: () -> Unit
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val questionRepo = QuestionRepository(db)
    val examRepo = ExamRepository(db)
    val studyRepo = StudyRepository(db)
    val sessionLogRepo = SessionLogRepository(db)

    // Navigation Backstack
    private val screenStack = mutableListOf<AppScreen>(AppScreen.TrackSelect)
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.TrackSelect)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Selected track: "OIC-NW" or "GMDSS"
    private val _currentTrack = MutableStateFlow("OIC-NW")
    val currentTrack: StateFlow<String> = _currentTrack.asStateFlow()

    // Ongoing session logs for current track
    val sessionLogs: StateFlow<List<SavedSessionLog>> = _currentTrack.flatMapLatest { track ->
        sessionLogRepo.getLogsByTrack(track)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestOngoingSession: StateFlow<SavedSessionLog?> = sessionLogs.map { logs ->
        logs.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Dialog state for prompting when starting a new session while an ongoing one exists
    private val _ongoingSessionPrompt = MutableStateFlow<OngoingSessionPromptState?>(null)
    val ongoingSessionPrompt: StateFlow<OngoingSessionPromptState?> = _ongoingSessionPrompt.asStateFlow()

    // Questions for the active track
    val allQuestions: StateFlow<List<Question>> = _currentTrack.flatMapLatest { track ->
        questionRepo.getAllQuestions(track)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Question>> = _currentTrack.flatMapLatest { track ->
        questionRepo.getFavorites(track)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flagged: StateFlow<List<Question>> = _currentTrack.flatMapLatest { track ->
        questionRepo.getFlagged(track)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incorrect: StateFlow<List<Question>> = _currentTrack.flatMapLatest { track ->
        questionRepo.getIncorrect(track)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val competencyStats: StateFlow<List<CompetencyCount>> = _currentTrack.flatMapLatest { track ->
        questionRepo.getCompetencyStats(track)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val examHistory: StateFlow<List<ExamRecord>> = _currentTrack.flatMapLatest { track ->
        examRepo.getExamRecords(track)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayGoal: StateFlow<DailyGoal?> = studyRepo.getTodayGoal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalStudySeconds: StateFlow<Long> = _currentTrack.flatMapLatest { track ->
        studyRepo.getTotalStudySeconds(track)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Active Mock Exam State
    private val _activeExam = MutableStateFlow<ActiveExamState?>(null)
    val activeExam: StateFlow<ActiveExamState?> = _activeExam.asStateFlow()
    private var examTimerJob: Job? = null

    // Active Study Stopwatch Timer State
    private val _isStopwatchRunning = MutableStateFlow(false)
    val isStopwatchRunning: StateFlow<Boolean> = _isStopwatchRunning.asStateFlow()

    private val _stopwatchSeconds = MutableStateFlow(0L)
    val stopwatchSeconds: StateFlow<Long> = _stopwatchSeconds.asStateFlow()
    private var stopwatchJob: Job? = null

    // Excel Importer & Admin State
    private val _importPreview = MutableStateFlow<ImportValidationResult?>(null)
    val importPreview: StateFlow<ImportValidationResult?> = _importPreview.asStateFlow()

    private val _adminMessage = MutableStateFlow<String?>(null)
    val adminMessage: StateFlow<String?> = _adminMessage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    init {
        // Seed initial authentic MARINA question bank on first run
        viewModelScope.launch {
            DatabaseSeeder.seedIfEmpty(db.questionDao(), getApplication())
        }
    }

    fun selectTrack(track: String) {
        _currentTrack.value = track
        navigateTo(AppScreen.Home)
    }

    fun navigateTo(screen: AppScreen) {
        screenStack.add(screen)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
            _currentScreen.value = screenStack.last()
            return true
        }
        return false
    }

    fun popToHome() {
        while (screenStack.size > 1 && screenStack.last() !is AppScreen.Home) {
            screenStack.removeAt(screenStack.lastIndex)
        }
        _currentScreen.value = screenStack.lastOrNull() ?: AppScreen.Home
    }

    // ==========================================
    // PRACTICE ENGINE
    // ==========================================
    fun toggleFavorite(question: Question) {
        viewModelScope.launch {
            questionRepo.toggleFavorite(question.id, question.isFavorite)
        }
    }

    fun toggleFlag(question: Question) {
        viewModelScope.launch {
            questionRepo.toggleFlag(question.id, question.isFlagged)
        }
    }

    fun recordPracticeAnswer(question: Question, selectedIndex: Int) {
        viewModelScope.launch {
            questionRepo.recordAnswer(question, selectedIndex)
        }
    }

    fun removeFromIncorrect(questionId: Long) {
        viewModelScope.launch {
            questionRepo.removeFromIncorrect(questionId)
        }
    }

    fun reportQuestion(questionId: Long, reason: String, details: String) {
        viewModelScope.launch {
            questionRepo.reportQuestion(questionId, _currentTrack.value, reason, details)
        }
    }

    // ==========================================
    // EXAM ENGINE
    // ==========================================
    fun startOfficialExam() {
        viewModelScope.launch {
            _isProcessing.value = true
            val track = _currentTrack.value
            val questions = examRepo.generateOfficialExam(track)
            val timeLimitSeconds = if (track == "OIC-NW") 180 * 60L else 90 * 60L

            _activeExam.value = ActiveExamState(
                title = "$track Official MARINA Simulation",
                track = track,
                examType = "OFFICIAL_SIMULATION",
                questions = questions,
                timeLimitSeconds = timeLimitSeconds,
                timeRemainingSeconds = timeLimitSeconds
            )
            _isProcessing.value = false
            startExamTimer()
            navigateTo(AppScreen.ExamRunner)
        }
    }

    fun startCustomExam(
        selectedCompetencies: List<String>,
        questionCount: Int,
        timeLimitMinutes: Int
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            val track = _currentTrack.value
            val questions = examRepo.generateCustomExam(track, selectedCompetencies, questionCount)
            val timeLimitSeconds = timeLimitMinutes * 60L

            _activeExam.value = ActiveExamState(
                title = "$track Custom Mock Exam",
                track = track,
                examType = "CUSTOM",
                questions = questions,
                timeLimitSeconds = timeLimitSeconds,
                timeRemainingSeconds = timeLimitSeconds
            )
            _isProcessing.value = false
            startExamTimer()
            navigateTo(AppScreen.ExamRunner)
        }
    }

    private fun startExamTimer() {
        examTimerJob?.cancel()
        examTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _activeExam.value ?: break
                if (current.isSubmitted) break
                val remaining = (current.timeRemainingSeconds - 1).coerceAtLeast(0)
                _activeExam.value = current.copy(timeRemainingSeconds = remaining)
                if (remaining <= 0) {
                    submitExam()
                    break
                }
            }
        }
    }

    fun selectExamAnswer(questionId: Long, optionIndex: Int) {
        val current = _activeExam.value ?: return
        val updated = current.userAnswers.toMutableMap()
        updated[questionId] = optionIndex
        _activeExam.value = current.copy(userAnswers = updated)
    }

    fun toggleExamQuestionFlag(questionId: Long) {
        val current = _activeExam.value ?: return
        val updated = current.flaggedQuestionIds.toMutableSet()
        if (updated.contains(questionId)) updated.remove(questionId) else updated.add(questionId)
        _activeExam.value = current.copy(flaggedQuestionIds = updated)
    }

    fun setExamCurrentIndex(index: Int) {
        val current = _activeExam.value ?: return
        if (index in current.questions.indices) {
            _activeExam.value = current.copy(currentIndex = index)
        }
    }

    fun submitExam() {
        examTimerJob?.cancel()
        val current = _activeExam.value ?: return
        if (current.isSubmitted) return

        viewModelScope.launch {
            _isProcessing.value = true
            val timeUsed = current.timeLimitSeconds - current.timeRemainingSeconds
            val (record, _) = examRepo.submitExam(
                track = current.track,
                examType = current.examType,
                title = current.title,
                questions = current.questions,
                userAnswers = current.userAnswers,
                timeUsedSeconds = timeUsed.coerceAtLeast(1)
            )

            _activeExam.value = current.copy(isSubmitted = true, completedRecordId = record.id)
            _isProcessing.value = false

            // Clean up ongoing exam log since exam is submitted
            current.savedSessionLogId?.let { sessionLogRepo.deleteLog(it) }

            // Navigate to results
            navigateTo(AppScreen.ExamResult(record.id))
        }
    }

    // ==========================================
    // ONGOING SESSION MANAGEMENT & RESUME
    // ==========================================
    fun dismissOngoingSessionPrompt() {
        _ongoingSessionPrompt.value = null
    }

    fun deleteSessionLog(logId: Long) {
        viewModelScope.launch {
            sessionLogRepo.deleteLog(logId)
        }
    }

    fun clearAllSessionLogs(trackOnly: Boolean = false) {
        viewModelScope.launch {
            if (trackOnly) {
                sessionLogRepo.deleteLogsByTrack(_currentTrack.value)
            } else {
                sessionLogRepo.deleteAllLogs()
            }
        }
    }

    fun savePracticeSession(
        title: String,
        filterMode: PracticeFilterMode,
        studyMode: StudyMode,
        competencyCode: String?,
        partNumber: Int?,
        itemLimit: Int?,
        isRandomized: Boolean,
        shuffleOptions: Boolean,
        timeLimitSecondsPerItem: Int?,
        randomSeed: Int,
        questions: List<Question>,
        currentIndex: Int,
        answers: Map<Long, Int>,
        results: Map<Long, Boolean>,
        existingLogId: Long? = null
    ) {
        if (questions.isEmpty()) return
        viewModelScope.launch {
            val payload = PracticeSessionPayload(
                questionIds = questions.map { it.id },
                answers = answers,
                results = results,
                filterMode = filterMode.name,
                studyMode = studyMode.name,
                competencyCode = competencyCode,
                partNumber = partNumber,
                itemLimit = itemLimit,
                isRandomized = isRandomized,
                shuffleOptions = shuffleOptions,
                timeLimitSecondsPerItem = timeLimitSecondsPerItem,
                randomSeed = randomSeed
            )
            val log = SavedSessionLog(
                id = existingLogId ?: 0L,
                sessionType = "PRACTICE",
                track = _currentTrack.value,
                title = title,
                subtitle = "Question ${currentIndex + 1} of ${questions.size} • ${answers.size} answered",
                currentIndex = currentIndex,
                totalQuestions = questions.size,
                answeredCount = answers.size,
                lastActiveTimestamp = System.currentTimeMillis(),
                payloadJson = payload.toJson()
            )
            sessionLogRepo.saveLog(log)
        }
    }

    fun saveExamSession() {
        val exam = _activeExam.value ?: return
        if (exam.isSubmitted || exam.questions.isEmpty()) return
        examTimerJob?.cancel()

        viewModelScope.launch {
            val payload = ExamSessionPayload(
                questionIds = exam.questions.map { it.id },
                userAnswers = exam.userAnswers,
                flaggedQuestionIds = exam.flaggedQuestionIds.toList(),
                timeLimitSeconds = exam.timeLimitSeconds,
                timeRemainingSeconds = exam.timeRemainingSeconds,
                examType = exam.examType
            )
            val log = SavedSessionLog(
                id = exam.savedSessionLogId ?: 0L,
                sessionType = "EXAM",
                track = exam.track,
                title = exam.title,
                subtitle = "${exam.userAnswers.size} / ${exam.questions.size} answered • ${exam.timeRemainingSeconds / 60}m left",
                currentIndex = exam.currentIndex,
                totalQuestions = exam.questions.size,
                answeredCount = exam.userAnswers.size,
                lastActiveTimestamp = System.currentTimeMillis(),
                payloadJson = payload.toJson()
            )
            sessionLogRepo.saveLog(log)
            _activeExam.value = null
        }
    }

    fun resumeSession(log: SavedSessionLog) {
        viewModelScope.launch {
            if (log.sessionType == "PRACTICE") {
                val payload = PracticeSessionPayload.fromJson(log.payloadJson)
                val questions = questionRepo.getQuestionsByIdsOrdered(payload.questionIds)
                if (questions.isNotEmpty()) {
                    val filterMode = try {
                        PracticeFilterMode.valueOf(payload.filterMode)
                    } catch (e: Exception) {
                        PracticeFilterMode.ALL
                    }
                    val studyMode = try {
                        StudyMode.valueOf(payload.studyMode)
                    } catch (e: Exception) {
                        StudyMode.TUTOR
                    }
                    val screen = AppScreen.Practice(
                        title = log.title,
                        filterMode = filterMode,
                        competencyCode = payload.competencyCode,
                        partNumber = payload.partNumber,
                        customQuestionIds = payload.questionIds,
                        itemLimit = payload.itemLimit,
                        isRandomized = payload.isRandomized,
                        shuffleOptions = payload.shuffleOptions,
                        studyMode = studyMode,
                        timeLimitSecondsPerItem = payload.timeLimitSecondsPerItem,
                        resumeSessionLogId = log.id,
                        initialIndex = log.currentIndex,
                        initialAnswers = payload.answers,
                        initialResults = payload.results,
                        randomSeed = payload.randomSeed
                    )
                    navigateTo(screen)
                }
            } else if (log.sessionType == "EXAM") {
                val payload = ExamSessionPayload.fromJson(log.payloadJson)
                val questions = questionRepo.getQuestionsByIdsOrdered(payload.questionIds)
                if (questions.isNotEmpty()) {
                    _activeExam.value = ActiveExamState(
                        title = log.title,
                        track = log.track,
                        examType = payload.examType,
                        questions = questions,
                        userAnswers = payload.userAnswers,
                        flaggedQuestionIds = payload.flaggedQuestionIds.toSet(),
                        currentIndex = log.currentIndex,
                        timeLimitSeconds = payload.timeLimitSeconds,
                        timeRemainingSeconds = payload.timeRemainingSeconds,
                        isSubmitted = false,
                        completedRecordId = null,
                        savedSessionLogId = log.id
                    )
                    startExamTimer()
                    navigateTo(AppScreen.ExamRunner)
                }
            }
        }
    }

    fun requestStartPractice(
        args: AppScreen.Practice,
        onStartNew: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val existing = sessionLogRepo.getLatestLogByTrack(_currentTrack.value)
            if (existing != null) {
                _ongoingSessionPrompt.value = OngoingSessionPromptState(
                    existingSession = existing,
                    newSessionTitle = args.title,
                    onStartNew = {
                        _ongoingSessionPrompt.value = null
                        viewModelScope.launch {
                            sessionLogRepo.deleteLog(existing.id)
                            onStartNew?.invoke() ?: navigateTo(args)
                        }
                    }
                )
            } else {
                onStartNew?.invoke() ?: navigateTo(args)
            }
        }
    }

    fun requestStartOfficialExam() {
        viewModelScope.launch {
            val track = _currentTrack.value
            val existing = sessionLogRepo.getLatestLogByTrack(track)
            if (existing != null) {
                _ongoingSessionPrompt.value = OngoingSessionPromptState(
                    existingSession = existing,
                    newSessionTitle = "$track Official MARINA Simulation",
                    onStartNew = {
                        _ongoingSessionPrompt.value = null
                        viewModelScope.launch {
                            sessionLogRepo.deleteLog(existing.id)
                            startOfficialExam()
                        }
                    }
                )
            } else {
                startOfficialExam()
            }
        }
    }

    fun requestStartCustomExam(
        selectedCompetencies: List<String>,
        questionCount: Int,
        timeLimitMinutes: Int
    ) {
        viewModelScope.launch {
            val track = _currentTrack.value
            val existing = sessionLogRepo.getLatestLogByTrack(track)
            if (existing != null) {
                _ongoingSessionPrompt.value = OngoingSessionPromptState(
                    existingSession = existing,
                    newSessionTitle = "$track Custom Mock Exam ($questionCount items)",
                    onStartNew = {
                        _ongoingSessionPrompt.value = null
                        viewModelScope.launch {
                            sessionLogRepo.deleteLog(existing.id)
                            startCustomExam(selectedCompetencies, questionCount, timeLimitMinutes)
                        }
                    }
                )
            } else {
                startCustomExam(selectedCompetencies, questionCount, timeLimitMinutes)
            }
        }
    }

    // ==========================================
    // STOPWATCH & STUDY TIMER
    // ==========================================
    fun startStopwatch() {
        if (_isStopwatchRunning.value) return
        _isStopwatchRunning.value = true
        stopwatchJob = viewModelScope.launch {
            while (_isStopwatchRunning.value) {
                delay(1000)
                _stopwatchSeconds.value += 1
            }
        }
    }

    fun pauseStopwatch() {
        _isStopwatchRunning.value = false
        stopwatchJob?.cancel()
    }

    fun stopAndSaveStopwatch() {
        pauseStopwatch()
        val seconds = _stopwatchSeconds.value
        if (seconds > 0) {
            viewModelScope.launch {
                studyRepo.recordStudySession(_currentTrack.value, seconds)
                _stopwatchSeconds.value = 0L
            }
        }
    }

    // ==========================================
    // EXCEL IMPORT & ADMIN
    // ==========================================
    fun parseExcelFileForPreview(inputStream: InputStream, forcedReviewer: String? = null) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val existingQuestions = db.questionDao().getAllQuestionsList()
                val existingTexts = existingQuestions.map { it.questionText.lowercase() }.toSet()

                val result = ExcelImporter.processWorkbook(
                    inputStream = inputStream,
                    forcedReviewer = forcedReviewer,
                    existingQuestionTexts = existingTexts
                )
                _importPreview.value = result
            } catch (e: Exception) {
                _adminMessage.value = "Error parsing workbook: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun confirmImport() {
        val preview = _importPreview.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                questionRepo.insertBatch(preview.validatedQuestions)
                _adminMessage.value = "Successfully imported ${preview.validCount} questions for ${preview.reviewer}!"
                _importPreview.value = null
            } catch (e: Exception) {
                _adminMessage.value = "Failed to save questions: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun cancelImportPreview() {
        _importPreview.value = null
    }

    fun resetReviewerDatabase(reviewer: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                questionRepo.clearReviewer(reviewer)
                _adminMessage.value = "$reviewer questions cleared."
            } catch (e: Exception) {
                _adminMessage.value = "Reset failed: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun reseedDatabase() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                DatabaseSeeder.seedIfEmpty(db.questionDao(), getApplication())
                _adminMessage.value = "Seed questions verified/restored."
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun scanAndImportAssets() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val loaded = DatabaseSeeder.scanAndSeedFromAssets(getApplication(), db.questionDao())
                if (loaded > 0) {
                    _adminMessage.value = "Successfully imported $loaded questions from assets directory into Hyperion!"
                } else {
                    _adminMessage.value = "No .xlsx or .json files found in assets. Use 'SELECT .XLSX FILE' below to choose your file, or place questions.xlsx into app/src/main/assets/"
                }
            } catch (e: Exception) {
                _adminMessage.value = "Asset scan error: ${e.localizedMessage}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun dismissAdminMessage() {
        _adminMessage.value = null
    }

    suspend fun exportBackupJson(): String {
        return BackupManager.createBackupJson(db, includeQuestions = true)
    }

    suspend fun restoreBackupJson(json: String): Int {
        return BackupManager.restoreFromJson(db, json)
    }
}
