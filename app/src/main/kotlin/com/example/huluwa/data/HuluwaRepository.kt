package com.example.huluwa.data

import android.content.Context
import com.example.huluwa.data.dao.AudioEventDao
import com.example.huluwa.data.dao.AppSettingDao
import com.example.huluwa.data.dao.SleepSessionDao
import com.example.huluwa.data.entity.AudioEvent
import com.example.huluwa.data.entity.AppSetting
import com.example.huluwa.data.entity.SleepSession

class HuluwaRepository(context: Context) {
    private val database = HuluwaDatabase.getInstance(context)
    private val sleepSessionDao: SleepSessionDao = database.sleepSessionDao()
    private val audioEventDao: AudioEventDao = database.audioEventDao()
    private val appSettingDao: AppSettingDao = database.appSettingDao()

    suspend fun insertSession(session: SleepSession): Long {
        return sleepSessionDao.insert(session)
    }

    suspend fun deleteSession(session: SleepSession) {
        sleepSessionDao.delete(session)
    }

    suspend fun getAllSessions(): List<SleepSession> {
        return sleepSessionDao.getAllSessions()
    }

    suspend fun getSessionById(id: Long): SleepSession? {
        return sleepSessionDao.getSessionById(id)
    }

    suspend fun getLatestSession(): SleepSession? {
        return sleepSessionDao.getLatestSession()
    }

    suspend fun insertEvents(events: List<AudioEvent>) {
        audioEventDao.insertAll(events)
    }

    suspend fun getEventsBySessionId(sessionId: Long): List<AudioEvent> {
        return audioEventDao.getEventsBySessionId(sessionId)
    }

    suspend fun deleteEventsBySessionId(sessionId: Long) {
        audioEventDao.deleteEventsBySessionId(sessionId)
    }

    suspend fun getSetting(key: String): String? {
        return appSettingDao.getSettingByKey(key)?.value
    }

    suspend fun setSetting(key: String, value: String) {
        val existing = appSettingDao.getSettingByKey(key)
        if (existing != null) {
            appSettingDao.updateSetting(key, value)
        } else {
            appSettingDao.insert(AppSetting(key = key, value = value))
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HuluwaRepository? = null

        fun getInstance(context: Context): HuluwaRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HuluwaRepository(context).also {
                    INSTANCE = it
                }
            }
        }
    }
}