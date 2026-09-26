package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["reviewer", "competencyCode"]),
        Index(value = ["reviewer", "isFavorite"]),
        Index(value = ["reviewer", "isFlagged"]),
        Index(value = ["reviewer", "timesIncorrect"]),
        Index(value = ["reviewer", "masteryStatus"])
    ]
)
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reviewer: String, // "OIC-NW" or "GMDSS"
    val function: String? = null, // "F1", "F2", "F3"
    val competencyCode: String, // "C1", "C2", ... "C17"
    val competencyDescription: String = "",
    val partNumber: Int? = null, // e.g. 1..10
    val questionNumber: Int? = null,
    val questionText: String,
    val questionType: String = "Multiple Choice",
    val optionA: String,
    val optionB: String,
    val optionC: String? = null,
    val optionD: String? = null,
    val optionE: String? = null,
    val optionF: String? = null,
    val correctAnswerLetter: String, // "A", "B", "C", "D", "E", "F"
    val correctAnswerIndex: Int, // 0..5
    val explanation: String? = null,
    val point: Int = 1,
    val section: String? = null,
    val sourceSheet: String? = null,
    val source: String? = null,
    val isFavorite: Boolean = false,
    val isFlagged: Boolean = false,
    val userNotes: String? = null,
    val masteryStatus: String = "NOT_ATTEMPTED", // NOT_ATTEMPTED, NEEDS_REVIEW, IMPROVING, MASTERED
    val consecutiveCorrect: Int = 0,
    val timesAttempted: Int = 0,
    val timesCorrect: Int = 0,
    val timesIncorrect: Int = 0,
    val lastAnsweredTimestamp: Long? = null,
    val lastSelectedOption: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getOptionsList(): List<String> {
        val list = mutableListOf<String>()
        if (optionA.isNotBlank()) list.add(optionA)
        if (optionB.isNotBlank()) list.add(optionB)
        if (!optionC.isNullOrBlank()) list.add(optionC)
        if (!optionD.isNullOrBlank()) list.add(optionD)
        if (!optionE.isNullOrBlank()) list.add(optionE)
        if (!optionF.isNullOrBlank()) list.add(optionF)
        return list
    }

    fun getOptionsCount(): Int = getOptionsList().size

    fun getOptionLetter(index: Int): String {
        return when (index) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            4 -> "E"
            5 -> "F"
            else -> "?"
        }
    }
}

@Entity(tableName = "exam_records")
data class ExamRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val track: String, // "OIC-NW" or "GMDSS"
    val examType: String, // "OFFICIAL_SIMULATION" or "CUSTOM"
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalQuestions: Int,
    val score: Int,
    val percentage: Float,
    val passed: Boolean,
    val timeUsedSeconds: Long,
    val competencyBreakdownJson: String = "{}"
)

@Entity(
    tableName = "exam_answers",
    indices = [Index(value = ["examId"])]
)
data class ExamAnswer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: Long,
    val questionId: Long,
    val questionText: String,
    val selectedIndex: Int, // -1 if skipped
    val correctIndex: Int,
    val isCorrect: Boolean,
    val competencyCode: String
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val track: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long,
    val questionsAnswered: Int = 0
)

@Entity(tableName = "daily_goals")
data class DailyGoal(
    @PrimaryKey
    val dateKey: String, // YYYY-MM-DD
    val targetQuestions: Int = 100,
    val completedQuestions: Int = 0,
    val targetStudySeconds: Long = 3600, // 1 hour
    val completedStudySeconds: Long = 0,
    val targetAccuracy: Int = 70
)

@Entity(tableName = "question_reports")
data class QuestionReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: Long,
    val track: String,
    val reason: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "saved_session_logs",
    indices = [
        Index(value = ["track"]),
        Index(value = ["sessionType"])
    ]
)
data class SavedSessionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionType: String, // "PRACTICE" or "EXAM"
    val track: String, // "OIC-NW" or "GMDSS"
    val title: String,
    val subtitle: String = "",
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0,
    val answeredCount: Int = 0,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val payloadJson: String = "{}"
)
