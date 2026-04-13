package com.example.huluwa.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.huluwa.ui.home.HomeScreen
import com.example.huluwa.ui.recording.RecordingScreen
import com.example.huluwa.ui.result.ResultScreen
import com.example.huluwa.ui.history.HistoryScreen
import com.example.huluwa.ui.setting.SettingScreen

@Composable
fun HuluwaNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Recording.route) {
            RecordingScreen(navController = navController)
        }
        composable(Screen.Result.route + "/{sessionId}") {backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")?.toLongOrNull()
            if (sessionId != null) {
                ResultScreen(navController = navController, sessionId = sessionId)
            }
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
        composable(Screen.Setting.route) {
            SettingScreen(navController = navController)
        }
    }
}

enum class Screen(val route: String) {
    Home("home"),
    Recording("recording"),
    Result("result"),
    History("history"),
    Setting("setting")
}