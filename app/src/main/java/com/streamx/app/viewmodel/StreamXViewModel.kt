package com.streamx.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.streamx.app.data.AppDatabase
import com.streamx.app.data.Series
import com.streamx.app.data.WatchHistoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StreamXViewModel(private val database: AppDatabase) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val seriesCollection = db.collection("series")

    private val _seriesList = MutableStateFlow<List<Series>>(emptyList())
    val seriesList: StateFlow<List<Series>> = _seriesList

    val watchHistory = database.watchHistoryDao().getAllHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        fetchSeries()
    }

    private fun fetchSeries() {
        seriesCollection.orderBy("uploadTimestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(Series::class.java)?.copy(id = it.id) }
                    _seriesList.value = list
                }
            }
    }

    fun markEpisodeWatched(seriesId: String, seasonNum: Int, episodeNum: Int) {
        viewModelScope.launch {
            database.watchHistoryDao().insertHistory(
                WatchHistoryEntity(
                    seriesId = seriesId,
                    seasonNum = seasonNum,
                    episodeNum = episodeNum,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun isEpisodeWatched(seriesId: String, seasonNum: Int, episodeNum: Int): Boolean {
        return database.watchHistoryDao().getWatchedEpisode(seriesId, seasonNum, episodeNum) != null
    }

    companion object {
        fun provideFactory(database: AppDatabase): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StreamXViewModel::class.java)) {
                    return StreamXViewModel(database) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
