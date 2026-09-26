package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Question
import kotlinx.coroutines.flow.Flow

data class CompetencyCount(
    val competencyCode: String,
    val totalCount: Int,
    val attemptedCount: Int,
    val correctCount: Int
)

data class PartCount(
    val partNumber: Int,
    val totalCount: Int,
    val attemptedCount: Int,
    val correctCount: Int
)

@Dao
interface QuestionDao {

    @Query("SELECT * FROM questions WHERE reviewer = :reviewer ORDER BY id ASC")
    fun getAllQuestions(reviewer: String): Flow<List<Question>>

    @Query("SELECT COUNT(*) FROM questions WHERE reviewer = :reviewer")
    fun getQuestionCountFlow(reviewer: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE reviewer = :reviewer")
    suspend fun getQuestionCount(reviewer: String): Int

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getTotalQuestionCount(): Int

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: Long): Question?

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getQuestionsByIds(ids: List<Long>): List<Question>

    @Query("SELECT * FROM questions WHERE reviewer = :reviewer AND competencyCode = :competencyCode ORDER BY id ASC")
    fun getQuestionsByCompetency(reviewer: String, competencyCode: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE reviewer = :reviewer AND competencyCode = :competencyCode AND partNumber = :partNumber ORDER BY id ASC")
    fun getQuestionsByCompetencyAndPart(reviewer: String, competencyCode: String, partNumber: Int): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE reviewer = :reviewer AND isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavorites(reviewer: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE reviewer = :reviewer AND isFlagged = 1 ORDER BY updatedAt DESC")
    fun getFlagged(reviewer: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE reviewer = :reviewer AND timesIncorrect > 0 ORDER BY timesIncorrect DESC, updatedAt DESC")
    fun getIncorrect(reviewer: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE reviewer = :reviewer AND timesAttempted = 0 ORDER BY id ASC")
    fun getUnanswered(reviewer: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE reviewer = :reviewer AND masteryStatus = 'MASTERED' ORDER BY id ASC")
    fun getMastered(reviewer: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE reviewer = :reviewer AND (masteryStatus = 'NEEDS_REVIEW' OR timesIncorrect > 0) ORDER BY timesIncorrect DESC")
    fun getNeedsReview(reviewer: String): Flow<List<Question>>

    // Search query
    @Query("""
        SELECT * FROM questions 
        WHERE reviewer = :reviewer 
        AND (
            questionText LIKE '%' || :query || '%' 
            OR optionA LIKE '%' || :query || '%'
            OR optionB LIKE '%' || :query || '%'
            OR optionC LIKE '%' || :query || '%'
            OR optionD LIKE '%' || :query || '%'
            OR competencyCode LIKE '%' || :query || '%'
            OR sourceSheet LIKE '%' || :query || '%'
            OR CAST(questionNumber AS TEXT) = :query
        )
        ORDER BY id ASC LIMIT 200
    """)
    fun searchQuestions(reviewer: String, query: String): Flow<List<Question>>

    // Exam generation: Random selection per competency
    @Query("SELECT * FROM questions WHERE reviewer = :reviewer AND competencyCode = :competencyCode ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsForCompetency(reviewer: String, competencyCode: String, limit: Int): List<Question>

    // Smart Review prioritization
    @Query("""
        SELECT * FROM questions 
        WHERE reviewer = :reviewer 
        ORDER BY 
            timesIncorrect DESC, 
            CASE WHEN timesAttempted = 0 THEN 0 ELSE 1 END,
            CASE WHEN masteryStatus = 'NEEDS_REVIEW' THEN 0 ELSE 1 END,
            lastAnsweredTimestamp ASC 
        LIMIT :limit
    """)
    suspend fun getSmartReviewQuestions(reviewer: String, limit: Int): List<Question>

    // Competency summaries
    @Query("""
        SELECT competencyCode, 
               COUNT(*) as totalCount, 
               SUM(CASE WHEN timesAttempted > 0 THEN 1 ELSE 0 END) as attemptedCount,
               SUM(CASE WHEN timesCorrect > 0 AND timesIncorrect = 0 THEN 1 ELSE 0 END) as correctCount
        FROM questions 
        WHERE reviewer = :reviewer 
        GROUP BY competencyCode
    """)
    fun getCompetencyStats(reviewer: String): Flow<List<CompetencyCount>>

    // GMDSS Part summaries
    @Query("""
        SELECT COALESCE(partNumber, 0) as partNumber, 
               COUNT(*) as totalCount, 
               SUM(CASE WHEN timesAttempted > 0 THEN 1 ELSE 0 END) as attemptedCount,
               SUM(CASE WHEN timesCorrect > 0 AND timesIncorrect = 0 THEN 1 ELSE 0 END) as correctCount
        FROM questions 
        WHERE reviewer = 'GMDSS' AND competencyCode = :competencyCode AND partNumber IS NOT NULL
        GROUP BY partNumber
        ORDER BY partNumber ASC
    """)
    fun getGmdssPartStats(competencyCode: String): Flow<List<PartCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Long)

    @Query("UPDATE questions SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE questions SET isFlagged = :isFlagged, updatedAt = :timestamp WHERE id = :id")
    suspend fun setFlagged(id: Long, isFlagged: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE questions SET 
            timesAttempted = timesAttempted + 1,
            timesCorrect = timesCorrect + (CASE WHEN :isCorrect THEN 1 ELSE 0 END),
            timesIncorrect = timesIncorrect + (CASE WHEN :isCorrect THEN 0 ELSE 1 END),
            consecutiveCorrect = :newConsecutive,
            masteryStatus = :newMastery,
            lastAnsweredTimestamp = :timestamp,
            lastSelectedOption = :selectedOption,
            updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun recordAnswer(
        id: Long,
        selectedOption: Int,
        isCorrect: Boolean,
        newConsecutive: Int,
        newMastery: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE questions SET timesIncorrect = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun removeFromIncorrect(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM questions WHERE reviewer = :reviewer")
    suspend fun clearReviewerQuestions(reviewer: String)

    @Query("SELECT * FROM questions ORDER BY id ASC")
    suspend fun getAllQuestionsList(): List<Question>
}
