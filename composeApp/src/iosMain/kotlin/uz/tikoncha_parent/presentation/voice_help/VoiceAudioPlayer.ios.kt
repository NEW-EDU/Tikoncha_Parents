@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package uz.tikoncha_parent.presentation.voice_help

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionCategorySoloAmbient
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemFailedToPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.error
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.status
import platform.AVFoundation.timeControlStatus
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import uz.tikoncha_parent.platform.Logger

internal actual fun createVoiceAudioPlayer(): VoiceAudioPlayer = AvVoiceAudioPlayer()

/**
 * AVPlayer bilan URL'dan oqim. Holat 0,2 s'da bir tekşiriladi (KVO o'rniga — PlayerEngine'dagidek oddiy):
 * element "failed" (404, internet yöq) → Error; ijro boşlandi → Playing; oxiriga yetdi (bildiriş) → Idle.
 * Ijro paytida AVAudioSession `.playback` — iPhone ovozsiz rejimda bölsa ham tuşuntiriş eşitiladi
 * (keksa ota-onalar telefonni köpinça ovozsiz rejimda tutadi). Töxtaganda odatiy SoloAmbient'ga qaytariladi.
 */
private class AvVoiceAudioPlayer : VoiceAudioPlayer {
    private val _state = MutableStateFlow(VoiceState.Idle)
    override val state: StateFlow<VoiceState> = _state.asStateFlow()
    private val scope = MainScope()
    private var player: AVPlayer? = null
    private var observers: List<Any> = emptyList()
    private var poll: Job? = null

    override fun play(url: String) {
        release()
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            _state.value = VoiceState.Error
            return
        }
        _state.value = VoiceState.Loading
        AVAudioSession.sharedInstance().run {
            setCategory(AVAudioSessionCategoryPlayback, error = null)
            setActive(true, error = null)
        }
        val item = AVPlayerItem(uRL = nsUrl)
        val p = AVPlayer()
        p.replaceCurrentItemWithPlayerItem(item)
        player = p

        val center = NSNotificationCenter.defaultCenter
        observers = listOf(
            center.addObserverForName(
                name = AVPlayerItemDidPlayToEndTimeNotification,
                `object` = item,
                queue = NSOperationQueue.mainQueue,
            ) { _ ->
                if (player === p) stop(failed = false)
            },
            center.addObserverForName(
                name = AVPlayerItemFailedToPlayToEndTimeNotification,
                `object` = item,
                queue = NSOperationQueue.mainQueue,
            ) { _ ->
                if (player === p) stop(failed = true)
            },
        )

        poll = scope.launch {
            while (isActive && player === p) {
                if (item.status == AVPlayerItemStatusFailed || p.error != null) {
                    Logger.e(TAG, "voice playback failed: $url ${item.error?.localizedDescription ?: p.error?.localizedDescription}", null)
                    stop(failed = true)
                    break
                }
                if (p.timeControlStatus == AVPlayerTimeControlStatusPlaying && _state.value == VoiceState.Loading) {
                    _state.value = VoiceState.Playing
                }
                delay(200)
            }
        }
        p.play()
    }

    override fun stop(failed: Boolean) {
        release()
        _state.value = if (failed) VoiceState.Error else VoiceState.Idle
    }

    private fun release() {
        poll?.cancel()
        poll = null
        val center = NSNotificationCenter.defaultCenter
        observers.forEach { center.removeObserver(it) }
        observers = emptyList()
        player?.let {
            it.pause()
            it.replaceCurrentItemWithPlayerItem(null)
            AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategorySoloAmbient, error = null)
        }
        player = null
    }

    private companion object {
        const val TAG = "VoiceHelp"
    }
}
