package com.streamx.app.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.util.Rational
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.streamx.app.viewmodel.StreamXViewModel
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    navController: NavController,
    viewModel: StreamXViewModel,
    videoUrl: String,
    title: String,
    seriesId: String,
    seasonNum: Int,
    episodeNum: Int,
    isPipMode: Boolean
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val activity = context as? Activity
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingState: Boolean) {
                    isPlaying = isPlayingState
                }
            })
        }
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        viewModel.markEpisodeWatched(seriesId, seasonNum, episodeNum)

        onDispose {
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }
    
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (exoPlayer.duration > 0) {
                progress = exoPlayer.currentPosition.toFloat() / exoPlayer.duration.toFloat()
            }
            delay(1000)
        }
    }

    BackHandler {
        if (!isPipMode) navController.popBackStack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls },
                    onDoubleTap = { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val width = size.width
                        when {
                            offset.x < width / 3 -> exoPlayer.seekTo(max(0, exoPlayer.currentPosition - 10000))
                            offset.x > width * 2 / 3 -> exoPlayer.seekTo(min(exoPlayer.duration, exoPlayer.currentPosition + 10000))
                            else -> {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var isBrightnessDrag = false
                var startVal = 0f
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isBrightnessDrag = offset.x < size.width / 2
                        startVal = if (isBrightnessDrag) {
                            activity?.window?.attributes?.screenBrightness ?: 0.5f
                        } else {
                            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() /
                                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        val delta = -(dragAmount / size.height)
                        if (isBrightnessDrag) {
                            val newBrightness = (startVal + delta).coerceIn(0f, 1f)
                            activity?.let {
                                val attrs = it.window.attributes
                                attrs.screenBrightness = newBrightness
                                it.window.attributes = attrs
                            }
                        } else {
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val newVol = ((startVal + delta) * maxVol).toInt().coerceIn(0, maxVol)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                        }
                    }
                )
            }
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { it.resizeMode = resizeMode },
            modifier = Modifier.fillMaxSize()
        )

        if (showControls && !isPipMode) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(text = title, color = Color.White)
                    }
                    Row {
                        IconButton(onClick = {
                            resizeMode = when (resizeMode) {
                                AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }) {
                            Icon(Icons.Filled.Crop, contentDescription = "Crop", tint = Color.White)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            IconButton(onClick = {
                                activity?.enterPictureInPictureMode(
                                    PictureInPictureParams.Builder()
                                        .setAspectRatio(Rational(16, 9))
                                        .build()
                                )
                            }) {
                                Icon(Icons.Filled.PictureInPictureAlt, contentDescription = "PiP", tint = Color.White)
                            }
                        }
                    }
                }

                // Center Play/Pause
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Bottom Bar
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = progress,
                        onValueChange = { newProgress ->
                            progress = newProgress
                            exoPlayer.seekTo((newProgress * exoPlayer.duration).toLong())
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
