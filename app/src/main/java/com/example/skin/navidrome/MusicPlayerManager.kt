package com.example.skin.navidrome

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.skin.audiofx.AudioEffectsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentSong: Song? = null,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val volume: Float = 1.0f,
    val errorMessage: String? = null
)

class MusicPlayerManager(private val context: Context) {
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private val mediaSession: MediaSession = MediaSession.Builder(context, exoPlayer).build()
    private var audioEffectsManager: AudioEffectsManager? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            }

            @OptIn(UnstableApi::class)
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        if (audioEffectsManager == null && exoPlayer.audioSessionId != 0) {
                            audioEffectsManager = AudioEffectsManager(exoPlayer.audioSessionId)
                        }
                        _playerState.value = _playerState.value.copy(
                            duration = exoPlayer.duration.coerceAtLeast(0L)
                        )
                    }
                    Player.STATE_ENDED -> {
                        if (exoPlayer.repeatMode != Player.REPEAT_MODE_ONE) {
                            skipNext()
                        }
                    }
                    else -> {}
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = exoPlayer.currentMediaItemIndex
                val queue = _playerState.value.queue
                if (index in queue.indices) {
                    _playerState.value = _playerState.value.copy(
                        currentIndex = index,
                        currentSong = queue[index]
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _playerState.value = _playerState.value.copy(
                    isPlaying = false,
                    errorMessage = error.message ?: "Playback error"
                )
            }
        })
    }

    @OptIn(UnstableApi::class)
    fun playQueue(songs: List<Song>, startIndex: Int, serverUrl: String, user: String, pass: String) {
        if (songs.isEmpty()) return
        val validIndex = startIndex.coerceIn(0, songs.size - 1)
        val mediaItems = songs.map { song ->
            val uri = if (song.id.startsWith("/")) {
                Uri.fromFile(File(song.id))
            } else {
                Uri.parse(NavidromeClientManager.getStreamUrl(serverUrl, song.id, user, pass))
            }
            MediaItem.fromUri(uri)
        }

        exoPlayer.setMediaItems(mediaItems, validIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        if (audioEffectsManager == null && exoPlayer.audioSessionId != 0) {
            audioEffectsManager = AudioEffectsManager(exoPlayer.audioSessionId)
        }

        _playerState.value = _playerState.value.copy(
            queue = songs,
            currentIndex = validIndex,
            currentSong = songs[validIndex],
            errorMessage = null
        )
    }

    @OptIn(UnstableApi::class)
    fun playSong(song: Song, streamUrl: String) {
        val mediaItem = MediaItem.fromUri(streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        if (audioEffectsManager == null && exoPlayer.audioSessionId != 0) {
            audioEffectsManager = AudioEffectsManager(exoPlayer.audioSessionId)
        }

        _playerState.value = _playerState.value.copy(
            queue = listOf(song),
            currentIndex = 0,
            currentSong = song,
            errorMessage = null
        )
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun skipNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
            exoPlayer.prepare()
            exoPlayer.play()
        } else if (_playerState.value.repeatMode == Player.REPEAT_MODE_ALL && _playerState.value.queue.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    fun skipPrevious() {
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
            exoPlayer.prepare()
            exoPlayer.play()
        } else {
            exoPlayer.seekTo(0, 0L)
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    fun setVolume(volume: Float) {
        exoPlayer.volume = volume.coerceIn(0f, 1f)
        _playerState.value = _playerState.value.copy(volume = exoPlayer.volume)
    }

    fun toggleShuffle() {
        val newShuffle = !exoPlayer.shuffleModeEnabled
        exoPlayer.shuffleModeEnabled = newShuffle
        _playerState.value = _playerState.value.copy(isShuffleEnabled = newShuffle)
    }

    fun toggleRepeatMode() {
        val current = exoPlayer.repeatMode
        val next = when (current) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer.repeatMode = next
        _playerState.value = _playerState.value.copy(repeatMode = next)
    }

    // Audio Effects API
    fun setEqualizerPreset(preset: String) {
        audioEffectsManager?.setPreset(preset)
    }

    fun setEqualizerBandLevel(band: Short, level: Short) {
        audioEffectsManager?.setCustomBandLevel(band, level)
    }

    fun setBassBoost(strength: Short) {
        audioEffectsManager?.setBassBoost(strength)
    }

    fun setSurroundSound(enabled: Boolean) {
        audioEffectsManager?.setSurroundSound(enabled)
    }

    fun getAudioEffectsManager(): AudioEffectsManager? = audioEffectsManager

    fun getCurrentPosition(): Long = exoPlayer.currentPosition

    fun release() {
        mediaSession.release()
        audioEffectsManager?.release()
        exoPlayer.release()
    }
}
