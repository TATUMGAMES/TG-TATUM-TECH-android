/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tatumgames.tatumtech.android.ui.components.screens.games

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.theme.Black
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.ui.utils.GameMediaResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Plays bundled `raw:` video segments back-to-back as one continuous video.
 *
 * A single [ExoPlayer] owns a playlist of every segment; the UI hides segment boundaries by
 * showing one play/pause control and one progress bar across the whole playlist.
 * Segments must share resolution, frame rate, and codec profile so transitions stay seamless.
 */
@Composable
fun GameVideoPlaylistPlayer(
    segmentRefs: List<String>,
    gameTitle: String,
    modifier: Modifier = Modifier,
    aspectRatio: Float = DEFAULT_ASPECT_RATIO
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uris = remember(segmentRefs) {
        segmentRefs.mapNotNull { GameMediaResolver.resolveRawVideo(context, it) }
    }
    if (uris.isEmpty()) return

    val resumePoint = rememberSaveable(saver = PlaylistResumePoint.Saver) { PlaylistResumePoint() }
    val player = remember(uris) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItems(
                uris.map(MediaItem::fromUri),
                resumePoint.index.coerceIn(0, uris.lastIndex),
                resumePoint.positionMs
            )
            prepare()
        }
    }
    val segmentDurationsMs by produceState<List<Long>?>(initialValue = null, uris) {
        value = withContext(Dispatchers.IO) { uris.map { readDurationMs(context, it) } }
    }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    DisposableEffect(player, lifecycleOwner) {
        resumePoint.player = player
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.pause()
        }
        player.addListener(listener)
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.removeListener(listener)
            resumePoint.capture()
            resumePoint.player = null
            player.release()
        }
    }

    LaunchedEffect(player, isPlaying, segmentDurationsMs) {
        while (true) {
            progress = playlistProgress(player, segmentDurationsMs)
            if (!isPlaying) break
            delay(PROGRESS_TICK_MS)
        }
    }

    val videoDescription = stringResource(R.string.games_video_cd, gameTitle)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .background(Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        this.player = player
                    }
                },
                update = { it.player = player },
                onRelease = { it.player = null },
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = videoDescription }
                    .clickable { togglePlayback(player) }
            )
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Black.copy(alpha = 0.72f)),
                    contentAlignment = Alignment.Center
                ) {
                    StandardText(
                        text = "▶",
                        style = MaterialTheme.typography.headlineMedium.copy(color = White)
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp),
                trackColor = White.copy(alpha = 0.24f),
                drawStopIndicator = {}
            )
        }
    }
}

private fun togglePlayback(player: Player) {
    when {
        player.playbackState == Player.STATE_ENDED -> {
            player.seekTo(0, 0L)
            player.play()
        }

        player.isPlaying -> player.pause()
        else -> player.play()
    }
}

private fun playlistProgress(player: Player, segmentDurationsMs: List<Long>?): Float {
    if (player.playbackState == Player.STATE_ENDED) return 1f
    val durations = segmentDurationsMs ?: return 0f
    val totalMs = durations.sum()
    if (totalMs <= 0L) return 0f
    val index = player.currentMediaItemIndex.coerceIn(0, durations.lastIndex)
    val elapsedMs = durations.take(index).sum() + player.currentPosition
    return (elapsedMs.toFloat() / totalMs).coerceIn(0f, 1f)
}

private fun readDurationMs(context: Context, uri: Uri): Long {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
    } catch (_: RuntimeException) {
        0L
    } finally {
        retriever.release()
    }
}

/**
 * Survives configuration changes; [player] is read at save time so the position is current
 * even though saving runs before this composable's dispose callbacks.
 */
private class PlaylistResumePoint(
    var index: Int = 0,
    var positionMs: Long = 0L
) {
    var player: Player? = null

    fun capture() {
        player?.let {
            index = it.currentMediaItemIndex
            positionMs = it.currentPosition
        }
    }

    companion object {
        val Saver = listSaver<PlaylistResumePoint, Long>(
            save = {
                it.capture()
                listOf(it.index.toLong(), it.positionMs)
            },
            restore = { PlaylistResumePoint(it[0].toInt(), it[1]) }
        )
    }
}

private const val DEFAULT_ASPECT_RATIO = 616f / 256f
private const val PROGRESS_TICK_MS = 100L
