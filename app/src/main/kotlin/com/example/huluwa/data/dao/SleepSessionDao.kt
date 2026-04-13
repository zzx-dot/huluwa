package com.example.huluwa.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.huluwa.data.entity.SleepSession

@Dao
interface SleepSessionDao {
    @Insert
    suspend fun insert(session: SleepSession): Long

    @Delete
    suspend fun delete(session: SleepSession)

    @Query("SELECT * FROM sleep_session ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<SleepSession>

    @Query("SELECT * FROM sleep_session WHERE id = :id")
    suspend fun getSessionById(id: Long): SleepSession?

    @Query("SELECT * FROM sleep_session ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): SleepSession?
}