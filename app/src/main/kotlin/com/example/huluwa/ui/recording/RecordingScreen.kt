package com.example.huluwa.ui.recording

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.example.huluwa.navigation.Screen
import com.example.huluwa.service.RecordingService
import kotlinx.coroutines.delay
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(navController: NavController) {
    val context = LocalContext.current
    val startTime = remember { Date() }
    val elapsedTime = remember { mutableStateOf(0L) }

    // 启动录音服务
    LaunchedEffect(Unit) {
        RecordingService.startRecording(context)
    }

    // 计算已录时长
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedTime.value = Date().time - startTime.time
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "监测中",
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "当前状态: 正在录音",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            
            Text(
                text = "开始时间: ${startTime}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
            
            Text(
                text = "已录时长: ${formatDuration(elapsedTime.value)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
            
            Button(
                onClick = {
                    // 停止录音服务
                    RecordingService.stopRecording(context) {
                        // 导航到分析结果页
                        navController.navigate(Screen.Result.route + "/${it}")
                    }
                },
                modifier = Modifier
                    .padding(top = 32.dp)
            ) {
                Text(
                    text = "停止监测",
                    style = MaterialTheme.typography.headlineSmall
                )
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