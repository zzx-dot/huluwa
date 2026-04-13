package com.example.huluwa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.huluwa.data.dao.AudioEventDao
import com.example.huluwa.data.dao.AppSettingDao
import com.example.huluwa.data.dao.SleepSessionDao
import com.example.huluwa.data.entity.AudioEvent
import com.example.huluwa.data.entity.AppSetting
import com.example.huluwa.data.entity.SleepSession

@Database(
    entities = [SleepSession::class, AudioEvent::class, AppSetting::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTypeConverter::class)
abstract class HuluwaDatabase : RoomDatabase() {
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun audioEventDao(): AudioEventDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        private const val DATABASE_NAME = "huluwa.db"

        @Volatile
        private var INSTANCE: HuluwaDatabase? = null

        fun getInstance(context: Context): HuluwaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HuluwaDatabase::class.java,
                    DATABASE_NAME
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}