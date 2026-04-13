package com.example.huluwa.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = HuluwaRepository.getInstance(context)
    
    // 默认值
    val defaultVolumeThreshold = 60
    val defaultMinEventDuration = 500
    val defaultMergeInterval = 1000
    val defaultAutoAnalyze = true
    
    // 状态变量
    val volumeThreshold = remember { mutableStateOf(defaultVolumeThreshold.toFloat()) }
    val minEventDuration = remember { mutableStateOf(defaultMinEventDuration.toFloat()) }
    val mergeInterval = remember { mutableStateOf(defaultMergeInterval.toFloat()) }
    val autoAnalyze = remember { mutableStateOf(defaultAutoAnalyze) }

    // 加载设置
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            repository.getSetting("volume_threshold")?.toIntOrNull()?.let {
                volumeThreshold.value = it.toFloat()
            }
            repository.getSetting("min_event_duration")?.toLongOrNull()?.let {
                minEventDuration.value = it.toFloat()
            }
            repository.getSetting("merge_interval")?.toLongOrNull()?.let {
                mergeInterval.value = it.toFloat()
            }
            repository.getSetting("auto_analyze")?.toBoolean()?.let {
                autoAnalyze.value = it
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "设置",
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
            // 默认阈值设置
            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "默认音量阈值: ${volumeThreshold.value.toInt()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = volumeThreshold.value,
                    onValueChange = { volumeThreshold.value = it },
                    valueRange = 0f..100f,
                    steps = 100
                )
            }

            // 最短事件时长设置
            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "最短事件时长: ${minEventDuration.value.toLong()}ms",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = minEventDuration.value,
                    onValueChange = { minEventDuration.value = it },
                    valueRange = 100f..5000f,
                    steps = 49
                )
            }

            // 合并间隔设置
            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "合并间隔: ${mergeInterval.value.toLong()}ms",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = mergeInterval.value,
                    onValueChange = { mergeInterval.value = it },
                    valueRange = 100f..5000f,
                    steps = 49
                )
            }

            // 自动分析开关
            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "自动分析",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = autoAnalyze.value,
                    onCheckedChange = { autoAnalyze.value = it }
                )
            }

            // 保存按钮
            Button(
                onClick = {
                    val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
                    scope.launch {
                        repository.setSetting("volume_threshold", volumeThreshold.value.toInt().toString())
                        repository.setSetting("min_event_duration", minEventDuration.value.toLong().toString())
                        repository.setSetting("merge_interval", mergeInterval.value.toLong().toString())
                        repository.setSetting("auto_analyze", autoAnalyze.value.toString())
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 32.dp)
            ) {
                Text(
                    text = "保存设置",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}