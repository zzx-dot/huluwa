package com.example.huluwa.analysis
import android.media.MediaPlayer
import android.content.Context
import com.example.huluwa.data.HuluwaRepository
import com.example.huluwa.data.entity.AudioEvent
import java.io.File

class AudioAnalyzer(private val context: Context) {
    private val repository = HuluwaRepository.getInstance(context)
    
    // 默认配置
    private val DEFAULT_VOLUME_THRESHOLD = 60
    private val DEFAULT_MIN_EVENT_DURATION = 800L
    private val DEFAULT_MERGE_INTERVAL = 1500L
    private val WINDOW_SIZE = 200
    
    suspend fun analyzeAudio(audioPath: String): List<AudioEvent> {
        // 获取配置参数
        val volumeThreshold = repository.getSetting("volume_threshold")?.toIntOrNull() ?: DEFAULT_VOLUME_THRESHOLD
        val minEventDuration = repository.getSetting("min_event_duration")?.toLongOrNull() ?: DEFAULT_MIN_EVENT_DURATION
        val mergeInterval = repository.getSetting("merge_interval")?.toLongOrNull() ?: DEFAULT_MERGE_INTERVAL
        
        // 分析音频
        val rawEvents = detectEvents(audioPath, volumeThreshold)
        val filteredEvents = filterShortEvents(rawEvents, minEventDuration)
        val mergedEvents = mergeNearbyEvents(filteredEvents, mergeInterval)
        
        return mergedEvents
    }
    
    private fun detectEvents(audioPath: String, threshold: Int): List<AudioEvent> {
        val events = mutableListOf<AudioEvent>()
        val player = MediaPlayer()
        
        try {
            player.setDataSource(audioPath)
            player.prepare()
            
            val totalDuration = player.duration.toLong()
            var currentTime = 0L
            var inEvent = false
            var eventStartTime = 0L
            var eventPeakVolume = 0

            while (currentTime < totalDuration) {
                val volume = calculateVolume(audioPath, currentTime.toInt(), WINDOW_SIZE)
                
                if (volume > threshold) {
                    if (!inEvent) {
                        inEvent = true
                        eventStartTime = currentTime
                        eventPeakVolume = volume
                    } else {
                        // 更新峰值音量
                        if (volume > eventPeakVolume) {
                            eventPeakVolume = volume
                        }
                    }
                } else {
                    if (inEvent) {
                        inEvent = false
                        val eventEndTime = currentTime
                        val eventDuration = eventEndTime - eventStartTime
                        
                        events.add(
                            AudioEvent(
                                sessionId = 0, // 临时值，后续会更新
                                startTime = eventStartTime,
                                endTime = eventEndTime,
                                duration = eventDuration,
                                peakVolume = eventPeakVolume
                            )
                        )
                    }
                }
                
                currentTime += WINDOW_SIZE.toLong()
            }
            
            // 处理最后一个事件
            if (inEvent) {
                val eventEndTime = totalDuration.toLong()
                val eventDuration = eventEndTime - eventStartTime
                
                events.add(
                    AudioEvent(
                        sessionId = 0, // 临时值，后续会更新
                        startTime = eventStartTime,
                        endTime = eventEndTime,
                        duration = eventDuration,
                        peakVolume = eventPeakVolume
                    )
                )
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            player.release()
        }
        
        return events
    }
    
    private fun calculateVolume(audioPath: String, startTime: Int, windowSize: Int): Int {
        // 简化实现，实际项目中需要更精确的音频分析
        // 这里使用MediaRecorder的最大振幅作为音量近似值
        return 70 // 模拟值，实际项目中需要计算真实音量
    }
    
    private fun filterShortEvents(events: List<AudioEvent>, minDuration: Long): List<AudioEvent> {
        return events.filter { it.duration >= minDuration }
    }
    
    private fun mergeNearbyEvents(events: List<AudioEvent>, mergeInterval: Long): List<AudioEvent> {
        if (events.isEmpty()) return emptyList()
        
        val mergedEvents = mutableListOf<AudioEvent>()
        var currentEvent = events[0]
        
        for (i in 1 until events.size) {
            val nextEvent = events[i]
            if (nextEvent.startTime - currentEvent.endTime <= mergeInterval) {
                // 合并事件
                currentEvent = currentEvent.copy(
                    endTime = nextEvent.endTime,
                    duration = nextEvent.endTime - currentEvent.startTime,
                    peakVolume = maxOf(currentEvent.peakVolume, nextEvent.peakVolume)
                )
            } else {
                // 添加当前事件并开始新事件
                mergedEvents.add(currentEvent)
                currentEvent = nextEvent
            }
        }
        
        // 添加最后一个事件
        mergedEvents.add(currentEvent)
        
        return mergedEvents
    }
    
    fun generateEventSegment(audioPath: String, event: AudioEvent): File? {
        // 生成事件片段文件
        val audioDir = File(context.filesDir, "audio")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        
        val segmentFile = File(audioDir, "event_${System.currentTimeMillis()}.m4a")
        
        // 实际项目中需要使用MediaExtractor和MediaMuxer来提取音频片段
        // 这里简化实现
        return segmentFile
    }
}