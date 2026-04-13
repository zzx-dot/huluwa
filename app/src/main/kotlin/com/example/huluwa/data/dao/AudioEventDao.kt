package com.example.huluwa.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.huluwa.data.entity.AudioEvent

@Dao
interface AudioEventDao {
    @Insert
    suspend fun insert(event: AudioEvent)

    @Insert
    suspend fun insertAll(events: List<AudioEvent>)

    @Delete
    suspend fun delete(event: AudioEvent)

    @Query("SELECT * FROM audio_event WHERE sessionId = :sessionId ORDER BY startTime ASC")
    suspend fun getEventsBySessionId(sessionId: Long): List<AudioEvent>

    @Query("DELETE FROM audio_event WHERE sessionId = :sessionId")
    suspend fun deleteEventsBySessionId(sessionId: Long)
}