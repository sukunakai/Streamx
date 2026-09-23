package com.streamx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.streamx.app.viewmodel.StreamXViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(navController: NavController, viewModel: StreamXViewModel, seriesId: String) {
    val seriesList by viewModel.seriesList.collectAsState()
    val series = seriesList.find { it.id == seriesId }
    val haptic = LocalHapticFeedback.current

    var selectedSeasonNum by remember { mutableIntStateOf(1) }

    if (series == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(series.title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            AsyncImage(
                model = series.thumbnailUrl.ifEmpty { "https://via.placeholder.com/800x450" },
                contentDescription = "Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))

            ScrollableTabRow(
                selectedTabIndex = selectedSeasonNum - 1,
                edgePadding = 0.dp
            ) {
                series.seasons.forEachIndexed { index, season ->
                    Tab(
                        selected = selectedSeasonNum == season.seasonNum,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedSeasonNum = season.seasonNum
                        },
                        text = { Text("Season ${season.seasonNum}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            val currentSeason = series.seasons.find { it.seasonNum == selectedSeasonNum }
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentSeason?.episodes ?: emptyList()) { episode ->
                    var isWatched by remember { mutableStateOf(false) }
                    LaunchedEffect(episode) {
                        isWatched = viewModel.isEpisodeWatched(series.id, selectedSeasonNum, episode.episodeNum)
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isWatched) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val encodedUrl = URLEncoder.encode(episode.videoUrl.ifEmpty { "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8" }, StandardCharsets.UTF_8.toString())
                                val encodedTitle = URLEncoder.encode("S${selectedSeasonNum} E${episode.episodeNum} - ${episode.title}", StandardCharsets.UTF_8.toString())
                                navController.navigate("player/$encodedUrl/$encodedTitle/${series.id}/$selectedSeasonNum/${episode.episodeNum}")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "E${episode.episodeNum}",
                            fontWeight = FontWeight.Bold,
                            color = if (isWatched) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
