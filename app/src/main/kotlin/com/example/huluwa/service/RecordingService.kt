package com.example.huluwa.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import java.util.Timer
import java.util.TimerTask
import androidx.core.app.NotificationCompat
import com.example.huluwa.data.HuluwaRepository
import com.example.huluwa.data.entity.SleepSession
import com.example.huluwa.analysis.AudioAnalyzer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class RecordingService : Service() {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var startTime: Date? = null
    private var isRecording = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var wakeLock: PowerManager.WakeLock? = null
    private var saveTimer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        if (isRecording) return

        val audioDir = File(filesDir, "audio")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }

        val inProgressFiles = audioDir.listFiles { file ->
            file.name.contains("in_progress")
        }

        val file = if (inProgressFiles?.isNotEmpty() == true) {
            inProgressFiles.maxByOrNull { it.lastModified() }!!
        } else {
            File(audioDir, "recording_${System.currentTimeMillis()}_in_progress.m4a")
        }

        try {
            val recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioChannels(1)
                setAudioEncodingBitRate(48000)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            audioFile = file
            startTime = Date()
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            mediaRecorder?.release()
            mediaRecorder = null
            audioFile = null
            stopForegroundCompat()
            stopSelf()
            return
        }

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "huluwa:recording"
        ).apply {
            acquire()
        }

        saveTimer = Timer()
        saveTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    // placeholder for periodic state save
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 60000, 60000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }

    private fun stopRecording() {
        if (!isRecording) {
            stopForegroundCompat()
            stopSelf()
            return
        }

        val recorder = mediaRecorder
        val file = audioFile
        val start = startTime

        isRecording = false

        if (recorder != null) {
            try {
                recorder.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    recorder.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        mediaRecorder = null

        saveTimer?.cancel()
        saveTimer = null

        if (wakeLock?.isHeld == true) {
            try {
                wakeLock?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        wakeLock = null

        stopForegroundCompat()
        stopSelf()

        if (file == null || !file.exists() || start == null) {
            return
        }

        val finalAudioFile = File(file.parent, file.name.replace("_in_progress", ""))
        var renameOk = false
        try {
            renameOk = file.renameTo(finalAudioFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val savedFile = if (renameOk) finalAudioFile else file
        val endTime = Date()
        val duration = endTime.time - start.time

        serviceScope.launch {
            try {
                val repository = HuluwaRepository.getInstance(applicationContext)
                val session = SleepSession(
                    startTime = start,
                    endTime = endTime,
                    duration = duration,
                    audioPath = savedFile.absolutePath
                )
                val sessionId = repository.insertSession(session)

                val analyzer = AudioAnalyzer(applicationContext)
                val events = analyzer.analyzeAudio(savedFile.absolutePath)
                if (events.isNotEmpty()) {
                    repository.insertEvents(events.map { it.copy(sessionId = sessionId) })
                    val updatedSession = session.copy(eventCount = events.size)
                    repository.insertSession(updatedSession)
                }

                onRecordingStopped?.invoke(sessionId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "录音服务",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("呼噜娃")
            .setContentText("正在监测睡眠")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "recording_channel"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_START = "start"
        private const val ACTION_STOP = "stop"

        var onRecordingStopped: ((Long) -> Unit)? = null

        fun startRecording(context: Context) {
            val intent = Intent(context, RecordingService::class.java)
            intent.action = ACTION_START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopRecording(context: Context, callback: (Long) -> Unit) {
            onRecordingStopped = callback
            val intent = Intent(context, RecordingService::class.java)
            intent.action = ACTION_STOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
