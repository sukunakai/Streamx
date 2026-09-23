package com.streamx.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.streamx.app.viewmodel.StreamXViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(viewModel: StreamXViewModel) {
    val history by viewModel.watchHistory.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Me / Watch History", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Text("No watch history yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history) { record ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Series ID: ${record.seriesId}", style = MaterialTheme.typography.titleMedium)
                            Text("Season ${record.seasonNum} • Episode ${record.episodeNum}")
                            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            Text(sdf.format(Date(record.timestamp)), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
