package com.example.huluwa.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.huluwa.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = HuluwaRepository.getInstance(context)
    val latestSession = remember { mutableStateOf(repository.getLatestSession()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            latestSession.value = repository.getLatestSession()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "呼噜娃",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            )
        }
    ) {paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 开始监测按钮
            Button(
                onClick = {
                    navController.navigate(Screen.Recording.route)
                },
                modifier = Modifier
                    .padding(vertical = 32.dp)
            ) {
                Text(
                    text = "开始监测",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            // 最近一次监测摘要
            latestSession.value?.let {session ->
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "最近一次监测",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "日期: ${session.startTime}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "时长: ${formatDuration(session.duration)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "疑似片段数: ${session.eventCount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 历史记录入口
            Button(
                onClick = {
                    navController.navigate(Screen.History.route)
                },
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "查看历史记录",
                    style = MaterialTheme.typography.bodyLarge
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