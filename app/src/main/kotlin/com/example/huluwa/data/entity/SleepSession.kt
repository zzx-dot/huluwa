package com.example.huluwa.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sleep_session")
data class SleepSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Date,
    val endTime: Date,
    val duration: Long, // 单位：毫秒
    val audioPath: String,
    val eventCount: Int = 0,
    val createdAt: Date = Date()
)