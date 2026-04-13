package com.example.huluwa.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_setting")
data class AppSetting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String
)