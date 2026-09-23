package com.streamx.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.streamx.app.ui.player.VideoPlayerScreen
import com.streamx.app.ui.screens.SeriesDetailScreen
import com.streamx.app.viewmodel.StreamXViewModel

@Composable
fun AppNavGraph(viewModel: StreamXViewModel, isPipMode: Boolean) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainAppScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            "detail/{seriesId}",
            arguments = listOf(navArgument("seriesId") { type = NavType.StringType })
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: return@composable
            SeriesDetailScreen(navController = navController, viewModel = viewModel, seriesId = seriesId)
        }
        composable(
            "player/{videoUrl}/{title}/{seriesId}/{seasonNum}/{episodeNum}",
            arguments = listOf(
                navArgument("videoUrl") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("seasonNum") { type = NavType.IntType },
                navArgument("episodeNum") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: ""
            val seasonNum = backStackEntry.arguments?.getInt("seasonNum") ?: 1
            val episodeNum = backStackEntry.arguments?.getInt("episodeNum") ?: 1
            
            VideoPlayerScreen(
                navController = navController,
                viewModel = viewModel,
                videoUrl = videoUrl,
                title = title,
                seriesId = seriesId,
                seasonNum = seasonNum,
                episodeNum = episodeNum,
                isPipMode = isPipMode
            )
        }
    }
}
