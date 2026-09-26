package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.CompetencyCount
import com.example.data.local.PartCount
import com.example.data.model.CompetencyMetadata
import com.example.data.model.DailyGoal
import com.example.data.model.ExamAnswer
import com.example.data.model.ExamRecord
import com.example.data.model.Question
import com.example.data.model.QuestionReport
import com.example.data.model.SavedSessionLog
import com.example.data.model.StudySession
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuestionRepository(private val db: AppDatabase) {
    private val dao = db.questionDao()

    fun getAllQuestions(reviewer: String): Flow<List<Question>> = dao.getAllQuestions(reviewer)
    fun getQuestionCountFlow(reviewer: String): Flow<Int> = dao.getQuestionCountFlow(reviewer)
    suspend fun getQuestionCount(reviewer: String): Int = dao.getQuestionCount(reviewer)
    suspend fun getQuestionById(id: Long): Question? = dao.getQuestionById(id)

    suspend fun getQuestionsByIdsOrdered(ids: List<Long>): List<Question> {
        if (ids.isEmpty()) return emptyList()
        val list = dao.getQuestionsByIds(ids)
        val map = list.associateBy { it.id }
        return ids.mapNotNull { map[it] }
    }

    fun getQuestionsByCompetency(reviewer: String, competency: String): Flow<List<Question>> =
        dao.getQuestionsByCompetency(reviewer, competency)

    fun getQuestionsByCompetencyAndPart(reviewer: String, competency: String, part: Int): Flow<List<Question>> =
        dao.getQuestionsByCompetencyAndPart(reviewer, competency, part)

    fun getFavorites(reviewer: String): Flow<List<Question>> = dao.getFavorites(reviewer)
    fun getFlagged(reviewer: String): Flow<List<Question>> = dao.getFlagged(reviewer)
    fun getIncorrect(reviewer: String): Flow<List<Question>> = dao.getIncorrect(reviewer)
    fun getUnanswered(reviewer: String): Flow<List<Question>> = dao.getUnanswered(reviewer)
    fun getMastered(reviewer: String): Flow<List<Question>> = dao.getMastered(reviewer)
    fun getNeedsReview(reviewer: String): Flow<List<Question>> = dao.getNeedsReview(reviewer)

    fun searchQuestions(reviewer: String, query: String): Flow<List<Question>> =
        dao.searchQuestions(reviewer, query)

    fun getCompetencyStats(reviewer: String): Flow<List<CompetencyCount>> =
        dao.getCompetencyStats(reviewer)

    fun getGmdssPartStats(competency: String): Flow<List<PartCount>> =
        dao.getGmdssPartStats(competency)

    suspend fun getSmartReviewQuestions(reviewer: String, limit: Int = 50): List<Question> =
        dao.getSmartReviewQuestions(reviewer, limit)

    suspend fun toggleFavorite(id: Long, current: Boolean) {
        dao.setFavorite(id, !current)
    }

    suspend fun toggleFlag(id: Long, current: Boolean) {
        dao.setFlagged(id, !current)
    }

    suspend fun recordAnswer(question: Question, selectedIndex: Int) {
        val isCorrect = selectedIndex == question.correctAnswerIndex
        val newConsecutive = if (isCorrect) question.consecutiveCorrect + 1 else 0
        val newMastery = when {
            newConsecutive >= 3 -> "MASTERED"
            isCorrect && newConsecutive >= 1 -> "IMPROVING"
            !isCorrect -> "NEEDS_REVIEW"
            else -> "NOT_ATTEMPTED"
        }

        dao.recordAnswer(
            id = question.id,
            selectedOption = selectedIndex,
            isCorrect = isCorrect,
            newConsecutive = newConsecutive,
            newMastery = newMastery
        )

        // Increment today's question count in daily goals
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db.studyDao().incrementQuestionsCompleted(todayKey, 1)
    }

    suspend fun removeFromIncorrect(id: Long) {
        dao.removeFromIncorrect(id)
    }

    suspend fun saveQuestion(question: Question): Long {
        return if (question.id == 0L) {
            dao.insertQuestion(question)
        } else {
            dao.updateQuestion(question)
            question.id
        }
    }

    suspend fun deleteQuestion(id: Long) {
        dao.deleteQuestionById(id)
    }

    suspend fun reportQuestion(questionId: Long, track: String, reason: String, details: String) {
        db.reportDao().insertReport(
            QuestionReport(
                questionId = questionId,
                track = track,
                reason = reason,
                details = details
            )
        )
    }

    fun getAllReports(): Flow<List<QuestionReport>> = db.reportDao().getAllReports()
    suspend fun deleteReport(id: Long) = db.reportDao().deleteReport(id)

    suspend fun insertBatch(questions: List<Question>): List<Long> {
        val results = mutableListOf<Long>()
        questions.chunked(200).forEach { chunk ->
            results.addAll(dao.insertQuestions(chunk))
        }
        return results
    }
    suspend fun clearReviewer(reviewer: String) = dao.clearReviewerQuestions(reviewer)
}

class ExamRepository(private val db: AppDatabase) {
    private val questionDao = db.questionDao()
    private val examDao = db.examDao()

    fun getExamRecords(track: String): Flow<List<ExamRecord>> = examDao.getExamRecords(track)
    suspend fun getExamRecord(id: Long): ExamRecord? = examDao.getExamRecordById(id)
    suspend fun getAnswersForExam(examId: Long): List<ExamAnswer> = examDao.getAnswersForExam(examId)

    /**
     * Generates an official MARINA simulation examination with standard competency quotas.
     */
    suspend fun generateOfficialExam(track: String): List<Question> {
        val selectedQuestions = mutableListOf<Question>()
        val compList = if (track == "OIC-NW") CompetencyMetadata.OIC_NW_COMPETENCIES else CompetencyMetadata.GMDSS_COMPETENCIES

        for (comp in compList) {
            val sampled = questionDao.getRandomQuestionsForCompetency(track, comp.code, comp.examQuota)
            selectedQuestions.addAll(sampled)
        }

        // If database has fewer questions than quota, supplement with remaining questions to build the full practice test
        val targetTotal = if (track == "OIC-NW") 200 else 100
        if (selectedQuestions.size < targetTotal) {
            val needed = targetTotal - selectedQuestions.size
            val currentIds = selectedQuestions.map { it.id }.toSet()
            val extras = questionDao.getRandomQuestionsForCompetency(track, compList.first().code, needed * 2)
                .filter { it.id !in currentIds }
                .take(needed)
            selectedQuestions.addAll(extras)
        }

        return selectedQuestions.shuffled()
    }

    /**
     * Generates a custom mock examination based on user preferences.
     */
    suspend fun generateCustomExam(
        track: String,
        selectedCompetencies: List<String>,
        questionCount: Int
    ): List<Question> {
        if (selectedCompetencies.isEmpty()) return emptyList()

        val perComp = (questionCount / selectedCompetencies.size).coerceAtLeast(1)
        val questions = mutableListOf<Question>()

        for (comp in selectedCompetencies) {
            val sampled = questionDao.getRandomQuestionsForCompetency(track, comp, perComp)
            questions.addAll(sampled)
        }

        return questions.shuffled().take(questionCount)
    }

    /**
     * Submits an exam session, calculates scores and competency breakdown, saves record and answers.
     */
    suspend fun submitExam(
        track: String,
        examType: String,
        title: String,
        questions: List<Question>,
        userAnswers: Map<Long, Int>, // questionId -> selectedIndex (-1 for unanswered)
        timeUsedSeconds: Long
    ): Pair<ExamRecord, List<ExamAnswer>> {
        var correctCount = 0
        val compBreakdownMap = mutableMapOf<String, Pair<Int, Int>>() // comp -> (correct, total)
        val answersList = mutableListOf<ExamAnswer>()

        for (q in questions) {
            val selected = userAnswers[q.id] ?: -1
            val isCorrect = selected == q.correctAnswerIndex
            if (isCorrect) correctCount++

            val currentComp = compBreakdownMap[q.competencyCode] ?: (0 to 0)
            compBreakdownMap[q.competencyCode] = (currentComp.first + (if (isCorrect) 1 else 0)) to (currentComp.second + 1)

            answersList.add(
                ExamAnswer(
                    examId = 0, // will update after exam record creation
                    questionId = q.id,
                    questionText = q.questionText,
                    selectedIndex = selected,
                    correctIndex = q.correctAnswerIndex,
                    isCorrect = isCorrect,
                    competencyCode = q.competencyCode
                )
            )

            // Also record individual answer in master question database
            if (selected >= 0) {
                val newConsecutive = if (isCorrect) q.consecutiveCorrect + 1 else 0
                val newMastery = when {
                    newConsecutive >= 3 -> "MASTERED"
                    isCorrect && newConsecutive >= 1 -> "IMPROVING"
                    !isCorrect -> "NEEDS_REVIEW"
                    else -> "NOT_ATTEMPTED"
                }
                questionDao.recordAnswer(
                    id = q.id,
                    selectedOption = selected,
                    isCorrect = isCorrect,
                    newConsecutive = newConsecutive,
                    newMastery = newMastery
                )
            }
        }

        val percentage = if (questions.isNotEmpty()) (correctCount.toFloat() / questions.size) * 100f else 0f
        val passed = percentage >= 70.0f

        val breakdownJson = JSONObject().apply {
            for ((comp, pair) in compBreakdownMap) {
                put(comp, "${pair.first}/${pair.second}")
            }
        }.toString()

        val record = ExamRecord(
            track = track,
            examType = examType,
            title = title,
            totalQuestions = questions.size,
            score = correctCount,
            percentage = percentage,
            passed = passed,
            timeUsedSeconds = timeUsedSeconds,
            competencyBreakdownJson = breakdownJson
        )

        val recordId = examDao.insertExamRecord(record)
        val answersWithId = answersList.map { it.copy(examId = recordId) }
        examDao.insertExamAnswers(answersWithId)

        return record.copy(id = recordId) to answersWithId
    }
}

class StudyRepository(private val db: AppDatabase) {
    private val studyDao = db.studyDao()

    fun getSessions(track: String): Flow<List<StudySession>> = studyDao.getStudySessions(track)
    fun getTotalStudySeconds(track: String): Flow<Long> = studyDao.getTotalStudySeconds(track)

    fun getStudySecondsSince(track: String, since: Long): Flow<Long> =
        studyDao.getStudySecondsSince(track, since)

    suspend fun recordStudySession(track: String, durationSeconds: Long, questionsAnswered: Int = 0) {
        if (durationSeconds <= 0) return
        studyDao.insertSession(
            StudySession(
                track = track,
                durationSeconds = durationSeconds,
                questionsAnswered = questionsAnswered
            )
        )

        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        studyDao.addStudySeconds(todayKey, durationSeconds)
    }

    fun getTodayGoal(): Flow<DailyGoal?> {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return studyDao.getDailyGoal(todayKey)
    }

    suspend fun saveDailyGoal(goal: DailyGoal) {
        studyDao.saveDailyGoal(goal)
    }
}

class SessionLogRepository(private val db: AppDatabase) {
    private val dao = db.sessionLogDao()

    fun getAllLogs(): Flow<List<SavedSessionLog>> = dao.getAllLogs()

    fun getLogsByTrack(track: String): Flow<List<SavedSessionLog>> = dao.getLogsByTrack(track)

    suspend fun getLatestLogByType(track: String, sessionType: String): SavedSessionLog? =
        dao.getLatestLogByType(track, sessionType)

    suspend fun getLatestLogByTrack(track: String): SavedSessionLog? =
        dao.getLatestLogByTrack(track)

    suspend fun getLogById(id: Long): SavedSessionLog? = dao.getLogById(id)

    suspend fun saveLog(log: SavedSessionLog): Long = dao.insertOrUpdate(log)

    suspend fun deleteLog(id: Long) = dao.deleteLogById(id)

    suspend fun deleteLogsByTrack(track: String) = dao.deleteLogsByTrack(track)

    suspend fun deleteAllLogs() = dao.deleteAllLogs()
}
