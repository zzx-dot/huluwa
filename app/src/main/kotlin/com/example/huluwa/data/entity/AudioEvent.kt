package com.example.huluwa.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "audio_event",
    foreignKeys = [
        ForeignKey(
            entity = SleepSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AudioEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val startTime: Long, // 相对于session开始的时间，单位：毫秒
    val endTime: Long, // 相对于session开始的时间，单位：毫秒
    val duration: Long, // 单位：毫秒
    val peakVolume: Int, // 峰值音量，0-100
    val segmentPath: String? = null
)