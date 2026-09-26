package com.example.data.importer

import com.example.data.local.AppDatabase
import com.example.data.model.DailyGoal
import com.example.data.model.ExamAnswer
import com.example.data.model.ExamRecord
import com.example.data.model.Question
import com.example.data.model.StudySession
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    /**
     * Creates a JSON export string (.hyperion backup format) of all user data.
     */
    suspend fun createBackupJson(db: AppDatabase, includeQuestions: Boolean = true): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "Hyperion")
        root.put("timestamp", System.currentTimeMillis())

        // Exam records
        val exams = db.examDao().getAllExamRecords()
        val examArray = JSONArray()
        for (e in exams) {
            val eo = JSONObject()
            eo.put("id", e.id)
            eo.put("track", e.track)
            eo.put("examType", e.examType)
            eo.put("title", e.title)
            eo.put("timestamp", e.timestamp)
            eo.put("totalQuestions", e.totalQuestions)
            eo.put("score", e.score)
            eo.put("percentage", e.percentage.toDouble())
            eo.put("passed", e.passed)
            eo.put("timeUsedSeconds", e.timeUsedSeconds)
            eo.put("competencyBreakdownJson", e.competencyBreakdownJson)
            examArray.put(eo)
        }
        root.put("examRecords", examArray)

        if (includeQuestions) {
            val questions = db.questionDao().getAllQuestionsList()
            val qArray = JSONArray()
            for (q in questions) {
                val qo = JSONObject()
                qo.put("reviewer", q.reviewer)
                qo.put("function", q.function ?: "")
                qo.put("competencyCode", q.competencyCode)
                qo.put("competencyDescription", q.competencyDescription)
                qo.put("partNumber", q.partNumber ?: -1)
                qo.put("questionNumber", q.questionNumber ?: -1)
                qo.put("questionText", q.questionText)
                qo.put("questionType", q.questionType)
                qo.put("optionA", q.optionA)
                qo.put("optionB", q.optionB)
                qo.put("optionC", q.optionC ?: "")
                qo.put("optionD", q.optionD ?: "")
                qo.put("optionE", q.optionE ?: "")
                qo.put("optionF", q.optionF ?: "")
                qo.put("correctAnswerLetter", q.correctAnswerLetter)
                qo.put("correctAnswerIndex", q.correctAnswerIndex)
                qo.put("explanation", q.explanation ?: "")
                qo.put("point", q.point)
                qo.put("section", q.section ?: "")
                qo.put("sourceSheet", q.sourceSheet ?: "")
                qo.put("isFavorite", q.isFavorite)
                qo.put("isFlagged", q.isFlagged)
                qo.put("masteryStatus", q.masteryStatus)
                qo.put("consecutiveCorrect", q.consecutiveCorrect)
                qo.put("timesAttempted", q.timesAttempted)
                qo.put("timesCorrect", q.timesCorrect)
                qo.put("timesIncorrect", q.timesIncorrect)
                qArray.put(qo)
            }
            root.put("questions", qArray)
        }

        return root.toString(2)
    }

    /**
     * Restores data from a .hyperion JSON backup string.
     */
    suspend fun restoreFromJson(db: AppDatabase, jsonString: String): Int {
        val root = JSONObject(jsonString)
        var restoredCount = 0

        if (root.has("questions")) {
            val qArray = root.getJSONArray("questions")
            val questionsToInsert = mutableListOf<Question>()
            for (i in 0 until qArray.length()) {
                val qo = qArray.getJSONObject(i)
                val partNum = qo.optInt("partNumber", -1).let { if (it == -1) null else it }
                val qNum = qo.optInt("questionNumber", -1).let { if (it == -1) null else it }

                questionsToInsert.add(
                    Question(
                        reviewer = qo.getString("reviewer"),
                        function = qo.optString("function").ifBlank { null },
                        competencyCode = qo.getString("competencyCode"),
                        competencyDescription = qo.optString("competencyDescription"),
                        partNumber = partNum,
                        questionNumber = qNum,
                        questionText = qo.getString("questionText"),
                        questionType = qo.optString("questionType", "Multiple Choice"),
                        optionA = qo.getString("optionA"),
                        optionB = qo.getString("optionB"),
                        optionC = qo.optString("optionC").ifBlank { null },
                        optionD = qo.optString("optionD").ifBlank { null },
                        optionE = qo.optString("optionE").ifBlank { null },
                        optionF = qo.optString("optionF").ifBlank { null },
                        correctAnswerLetter = qo.getString("correctAnswerLetter"),
                        correctAnswerIndex = qo.getInt("correctAnswerIndex"),
                        explanation = qo.optString("explanation").ifBlank { null },
                        point = qo.optInt("point", 1),
                        section = qo.optString("section").ifBlank { null },
                        sourceSheet = qo.optString("sourceSheet").ifBlank { null },
                        isFavorite = qo.optBoolean("isFavorite", false),
                        isFlagged = qo.optBoolean("isFlagged", false),
                        masteryStatus = qo.optString("masteryStatus", "NOT_ATTEMPTED"),
                        consecutiveCorrect = qo.optInt("consecutiveCorrect", 0),
                        timesAttempted = qo.optInt("timesAttempted", 0),
                        timesCorrect = qo.optInt("timesCorrect", 0),
                        timesIncorrect = qo.optInt("timesIncorrect", 0)
                    )
                )
            }
            if (questionsToInsert.isNotEmpty()) {
                db.questionDao().insertQuestions(questionsToInsert)
                restoredCount = questionsToInsert.size
            }
        }

        if (root.has("examRecords")) {
            val examArray = root.getJSONArray("examRecords")
            for (i in 0 until examArray.length()) {
                val eo = examArray.getJSONObject(i)
                val record = ExamRecord(
                    track = eo.getString("track"),
                    examType = eo.getString("examType"),
                    title = eo.getString("title"),
                    timestamp = eo.getLong("timestamp"),
                    totalQuestions = eo.getInt("totalQuestions"),
                    score = eo.getInt("score"),
                    percentage = eo.getDouble("percentage").toFloat(),
                    passed = eo.getBoolean("passed"),
                    timeUsedSeconds = eo.getLong("timeUsedSeconds"),
                    competencyBreakdownJson = eo.optString("competencyBreakdownJson", "{}")
                )
                db.examDao().insertExamRecord(record)
            }
        }

        return restoredCount
    }
}
