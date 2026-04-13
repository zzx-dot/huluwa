package com.example.huluwa.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.huluwa.data.entity.AppSetting

@Dao
interface AppSettingDao {
    @Insert
    suspend fun insert(setting: AppSetting)

    @Query("SELECT * FROM app_setting WHERE key = :key")
    suspend fun getSettingByKey(key: String): AppSetting?

    @Query("UPDATE app_setting SET value = :value WHERE key = :key")
    suspend fun updateSetting(key: String, value: String)

    @Query("DELETE FROM app_setting WHERE key = :key")
    suspend fun deleteSetting(key: String)
}