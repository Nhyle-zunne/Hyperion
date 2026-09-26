package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailyGoal
import com.example.data.model.ExamAnswer
import com.example.data.model.ExamRecord
import com.example.data.model.QuestionReport
import com.example.data.model.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exam_records WHERE track = :track ORDER BY timestamp DESC")
    fun getExamRecords(track: String): Flow<List<ExamRecord>>

    @Query("SELECT * FROM exam_records WHERE id = :id LIMIT 1")
    suspend fun getExamRecordById(id: Long): ExamRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamRecord(record: ExamRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamAnswers(answers: List<ExamAnswer>)

    @Query("SELECT * FROM exam_answers WHERE examId = :examId ORDER BY id ASC")
    suspend fun getAnswersForExam(examId: Long): List<ExamAnswer>

    @Query("DELETE FROM exam_records WHERE track = :track")
    suspend fun clearExamRecords(track: String)

    @Query("SELECT * FROM exam_records ORDER BY timestamp DESC")
    suspend fun getAllExamRecords(): List<ExamRecord>
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_sessions WHERE track = :track ORDER BY timestamp DESC")
    fun getStudySessions(track: String): Flow<List<StudySession>>

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM study_sessions WHERE track = :track")
    fun getTotalStudySeconds(track: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM study_sessions WHERE track = :track AND timestamp >= :sinceTimestamp")
    fun getStudySecondsSince(track: String, sinceTimestamp: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Query("SELECT * FROM daily_goals WHERE dateKey = :dateKey LIMIT 1")
    fun getDailyGoal(dateKey: String): Flow<DailyGoal?>

    @Query("SELECT * FROM daily_goals WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyGoalSync(dateKey: String): DailyGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyGoal(goal: DailyGoal)

    @Query("""
        UPDATE daily_goals 
        SET completedQuestions = completedQuestions + :count 
        WHERE dateKey = :dateKey
    """)
    suspend fun incrementQuestionsCompleted(dateKey: String, count: Int)

    @Query("""
        UPDATE daily_goals 
        SET completedStudySeconds = completedStudySeconds + :seconds 
        WHERE dateKey = :dateKey
    """)
    suspend fun addStudySeconds(dateKey: String, seconds: Long)
}

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: QuestionReport): Long

    @Query("SELECT * FROM question_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<QuestionReport>>

    @Query("DELETE FROM question_reports WHERE id = :id")
    suspend fun deleteReport(id: Long)
}
