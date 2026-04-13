package com.example.huluwa.ui.history

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
import com.example.huluwa.data.HuluwaRepository
import com.example.huluwa.data.entity.SleepSession
import com.example.huluwa.navigation.Screen
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = HuluwaRepository.getInstance(context)
    val sessions = remember { mutableStateOf<List<SleepSession>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            sessions.value = repository.getAllSessions()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "历史记录",
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
            if (sessions.value.isEmpty()) {
                Text(
                    text = "暂无历史记录",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp)
                )
            } else {
                LazyColumn {
                    items(sessions.value) {
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
                                    text = "${it.startTime}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "时长: ${formatDuration(it.duration)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "疑似片段数: ${it.eventCount}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(
                                    modifier = Modifier
                                        .padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = {
                                            navController.navigate(Screen.Result.route + "/${it.id}")
                                        }
                                    ) {
                                        Text(
                                            text = "查看",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            // 删除记录
                                            val updatedSessions = sessions.value.toMutableList()
                                            updatedSessions.remove(it)
                                            sessions.value = updatedSessions
                                            // 从数据库删除
                                            val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
                                            scope.launch {
                                                // 删除本地音频文件
                                                val audioFile = File(it.audioPath)
                                                if (audioFile.exists()) {
                                                    audioFile.delete()
                                                }
                                                // 删除数据库记录
                                                repository.deleteSession(it)
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = "删除",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            // 导出记录
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
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60)) % 24
    return "${hours}h ${minutes}m ${seconds}s"
}