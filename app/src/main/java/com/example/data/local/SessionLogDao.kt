package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SavedSessionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionLogDao {

    @Query("SELECT * FROM saved_session_logs ORDER BY lastActiveTimestamp DESC")
    fun getAllLogs(): Flow<List<SavedSessionLog>>

    @Query("SELECT * FROM saved_session_logs WHERE track = :track ORDER BY lastActiveTimestamp DESC")
    fun getLogsByTrack(track: String): Flow<List<SavedSessionLog>>

    @Query("SELECT * FROM saved_session_logs WHERE track = :track AND sessionType = :sessionType ORDER BY lastActiveTimestamp DESC LIMIT 1")
    suspend fun getLatestLogByType(track: String, sessionType: String): SavedSessionLog?

    @Query("SELECT * FROM saved_session_logs WHERE track = :track ORDER BY lastActiveTimestamp DESC LIMIT 1")
    suspend fun getLatestLogByTrack(track: String): SavedSessionLog?

    @Query("SELECT * FROM saved_session_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): SavedSessionLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: SavedSessionLog): Long

    @Query("DELETE FROM saved_session_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM saved_session_logs WHERE track = :track")
    suspend fun deleteLogsByTrack(track: String)

    @Query("DELETE FROM saved_session_logs")
    suspend fun deleteAllLogs()
}
