package com.streamx.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.streamx.app.data.Series
import com.streamx.app.viewmodel.StreamXViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: StreamXViewModel, navController: NavController) {
    val seriesList by viewModel.seriesList.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Anime", "K-Drama", "Sci-Fi", "Action")
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Latest Releases", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(seriesList.take(5)) { series ->
                SeriesCard(series, isHorizontal = true, onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate("detail/${series.id}")
                })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCategory = category
                    },
                    label = { Text(category) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        val filteredList = if (selectedCategory == "All") seriesList else seriesList.filter { it.category == selectedCategory }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredList) { series ->
                SeriesCard(series, isHorizontal = false, onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate("detail/${series.id}")
                })
            }
        }
    }
}

@Composable
fun SeriesCard(series: Series, isHorizontal: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(if (isHorizontal) 160.dp else Dp.Unspecified)
            .height(if (isHorizontal) 220.dp else 240.dp)
    ) {
        Column {
            AsyncImage(
                model = series.thumbnailUrl.ifEmpty { "https://via.placeholder.com/300x450" },
                contentDescription = series.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
            Text(
                text = series.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp),
                maxLines = 1
            )
        }
    }
}
