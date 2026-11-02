package uz.tikoncha_parent.presentation.live

import android.content.Context
import android.media.AudioManager
import uz.tikoncha_parent.AppHolder
import uz.tikoncha_parent.platform.Logger

/**
 * Android: WebRTC JavaAudioDeviceModule odatda ovozni earpiece'ga beradi. Sessiya davomida
 * `MODE_IN_COMMUNICATION` + speakerphone yoqib, karnayga yo'naltiramiz; tugaganda tiklaymiz.
 */
actual object LiveAudio {

    private fun audioManager(): AudioManager =
        AppHolder.app.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    actual fun startPlayback() {
        runCatching {
            audioManager().apply {
                mode = AudioManager.MODE_IN_COMMUNICATION
                @Suppress("DEPRECATION")
                isSpeakerphoneOn = true
            }
        }.onFailure { Logger.e("ParentLive", "audio startPlayback failed", it) }
    }

    actual fun stop() {
        runCatching {
            audioManager().apply {
                @Suppress("DEPRECATION")
                isSpeakerphoneOn = false
                mode = AudioManager.MODE_NORMAL
            }
        }.onFailure { Logger.e("ParentLive", "audio stop failed", it) }
    }
}
