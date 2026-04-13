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
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var audioFile: File
    private var startTime: Date = Date()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private var saveTimer: Timer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        // 检测并恢复上一条未完成记录
        val audioDir = File(filesDir, "audio")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        
        // 查找未完成的录音文件（文件名包含in_progress）
        val inProgressFiles = audioDir.listFiles { file ->
            file.name.contains("in_progress")
        }
        
        if (inProgressFiles?.isNotEmpty() == true) {
            // 恢复最近的未完成录音
            audioFile = inProgressFiles.maxByOrNull { it.lastModified() }!!
        } else {
            // 创建新的录音文件
            audioFile = File(audioDir, "recording_${System.currentTimeMillis()}_in_progress.m4a")
        }

        // 初始化MediaRecorder
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16000)
            setAudioChannels(1)
            setAudioEncodingBitRate(48000)
            setOutputFile(audioFile.absolutePath)
            prepare()
            start()
        }

        // 获取WakeLock防止系统休眠
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "huluwa:recording"
        ).apply {
            acquire()
        }

        // 启动定期落盘定时器（每60秒）
        saveTimer = Timer()
        saveTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // 定期保存录音状态
                // MediaRecorder会自动处理缓冲区写入，这里主要是确保文件系统缓存刷新
                try {
                    // 可以在这里添加其他状态保存逻辑
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 60000, 60000)

        // 启动前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder.stop()
            mediaRecorder.release()
        } catch (e: Exception) {
            // 处理异常，确保文件保存
        }

        // 取消定期落盘定时器
        saveTimer?.cancel()
        saveTimer = null

        // 释放WakeLock
        wakeLock?.release()

        // 停止前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()

        // 重命名临时文件为正式文件
        val finalAudioFile = File(audioFile.parent, audioFile.name.replace("_in_progress", ""))
        if (audioFile.exists()) {
            audioFile.renameTo(finalAudioFile)
        }

        // 保存睡眠会话并分析音频
        val endTime = Date()
        val duration = endTime.time - startTime.time

        serviceScope.launch {
            val repository = HuluwaRepository.getInstance(applicationContext)
            val session = SleepSession(
                startTime = startTime,
                endTime = endTime,
                duration = duration,
                audioPath = finalAudioFile.absolutePath
            )
            val sessionId = repository.insertSession(session)

            // 分析音频
            val analyzer = AudioAnalyzer(applicationContext)
            val events = analyzer.analyzeAudio(finalAudioFile.absolutePath)
            if (events.isNotEmpty()) {
                repository.insertEvents(events.map { it.copy(sessionId = sessionId) })
                // 更新会话的事件数量
                val updatedSession = session.copy(eventCount = events.size)
                repository.insertSession(updatedSession)
            }

            // 回调通知录音完成
            onRecordingStopped?.invoke(sessionId)
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
            context.startService(intent)
        }
    }
}