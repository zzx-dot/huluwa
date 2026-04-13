package com.example.huluwa.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.huluwa.analysis.AudioAnalyzer
import com.example.huluwa.data.HuluwaRepository
import com.example.huluwa.data.entity.AudioEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, sessionId: Long) {
    val context = LocalContext.current
    val repository = HuluwaRepository.getInstance(context)
    val session = remember { mutableStateOf(repository.getSessionById(sessionId)) }
    val events = remember { mutableStateOf<List<AudioEvent>>(emptyList()) }
    val volumeThreshold = remember { mutableStateOf(60f) }
    val isAnalyzing = remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        withContext(Dispatchers.IO) {
            session.value = repository.getSessionById(sessionId)
            events.value = repository.getEventsBySessionId(sessionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "分析结果",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
        ) {
            // 监测摘要
            session.value?.let {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "监测摘要",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "监测日期: ${it.startTime}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "开始时间: ${it.startTime}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "结束时间: ${it.endTime}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "总时长: ${formatDuration(it.duration)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "疑似片段总数: ${it.eventCount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 阈值滑杆
            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "音量阈值: ${volumeThreshold.value.toInt()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = volumeThreshold.value,
                    onValueChange = { volumeThreshold.value = it },
                    valueRange = 0f..100f,
                    steps = 100
                )
            }

            // 重新分析按钮
            Button(
                onClick = {
                    isAnalyzing.value = true
                    // 重新分析音频
                    val analyzer = AudioAnalyzer(context)
                    session.value?.let {
                        val newEvents = analyzer.analyzeAudio(it.audioPath)
                        events.value = newEvents
                    }
                    isAnalyzing.value = false
                },
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "重新分析",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // 完整录音操作
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        // 播放完整录音
                    }
                ) {
                    Text(
                        text = "播放完整录音",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Button(
                    onClick = {
                        // 导出完整录音
                    }
                ) {
                    Text(
                        text = "导出完整录音",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // 疑似片段列表
            Text(
                text = "疑似片段列表",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
            LazyColumn {
                items(events.value) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "片段 ${events.value.indexOf(it) + 1}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "开始时间: ${formatTime(it.startTime)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "持续时长: ${formatDuration(it.duration)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "峰值音量: ${it.peakVolume}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        // 播放片段
                                    }
                                ) {
                                    Text(
                                        text = "播放",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Button(
                                    onClick = {
                                        // 导出片段
                                    }
                                ) {
                                    Text(
                                        text = "导出",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    return "${minutes}m ${seconds}s"
}

private fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60)) % 24
    return "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}