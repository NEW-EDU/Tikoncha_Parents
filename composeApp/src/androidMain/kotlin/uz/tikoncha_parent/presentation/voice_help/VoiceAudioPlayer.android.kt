package uz.tikoncha_parent.presentation.voice_help

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.platform.appContext

internal actual fun createVoiceAudioPlayer(): VoiceAudioPlayer = ExoVoiceAudioPlayer()

/**
 * Har tuşuntiriş uçun alohida ExoPlayer (ilovaning umumiy PlayerEngine'iga va qöllanma kliplariga tegmaydi);
 * tugasa yoki töxtatilsa — darhol böşatiladi. Audio fokus olinadi: musiqa pauza qilinadi, qöng'iroqda töxtaydi.
 */
private class ExoVoiceAudioPlayer : VoiceAudioPlayer {
    private val _state = MutableStateFlow(VoiceState.Idle)
    override val state: StateFlow<VoiceState> = _state.asStateFlow()
    private var player: ExoPlayer? = null

    @OptIn(UnstableApi::class)
    override fun play(url: String) {
        release()
        val context = appContext
        if (context == null) {
            _state.value = VoiceState.Error
            return
        }
        _state.value = VoiceState.Loading
        val p = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .build()
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (player === p && isPlaying) _state.value = VoiceState.Playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (player === p && playbackState == Player.STATE_ENDED) stop(failed = false)
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                // Audio fokus butunlay olindi (qöng'iroq, boşqa pleyer) — tugma "Tinglaş"ga qaytadi
                if (player === p && !playWhenReady) stop(failed = false)
            }

            override fun onPlayerError(error: PlaybackException) {
                if (player !== p) return
                // 404 (fayl hali yuklanmagan), internet yöq va h.k. — barçasi "hozir yöq"
                Logger.e(TAG, "voice playback failed: $url", error)
                stop(failed = true)
            }
        })
        player = p
        p.setMediaItem(MediaItem.fromUri(url))
        p.playWhenReady = true
        p.prepare()
    }

    override fun stop(failed: Boolean) {
        release()
        _state.value = if (failed) VoiceState.Error else VoiceState.Idle
    }

    private fun release() {
        val p = player ?: return
        player = null
        p.release()
    }

    private companion object {
        const val TAG = "VoiceHelp"
    }
}
