package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DailyGoal
import com.example.data.model.ExamAnswer
import com.example.data.model.ExamRecord
import com.example.data.model.Question
import com.example.data.model.QuestionReport
import com.example.data.model.SavedSessionLog
import com.example.data.model.StudySession

@Database(
    entities = [
        Question::class,
        ExamRecord::class,
        ExamAnswer::class,
        StudySession::class,
        DailyGoal::class,
        QuestionReport::class,
        SavedSessionLog::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun examDao(): ExamDao
    abstract fun studyDao(): StudyDao
    abstract fun reportDao(): ReportDao
    abstract fun sessionLogDao(): SessionLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hyperion_master.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
