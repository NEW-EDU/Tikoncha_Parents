@file:OptIn(ExperimentalForeignApi::class)

package uz.tikoncha_parent.presentation.live

import WebRTC.RTCAudioSession
import WebRTC.RTCAudioSessionConfiguration
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSessionCategoryOptionAllowBluetooth
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeVideoChat
import uz.tikoncha_parent.platform.Logger

/**
 * iOS: WebRTC o'z `RTCAudioSession`'ini boshqaradi. Standart kategoriya ovozni earpiece'ga
 * yo'naltiradi — shu sababli kategoriyani `playAndRecord` + `defaultToSpeaker` qilamiz va
 * ham joriy sessiyaga, ham WebRTC'ning kelgusi (qayta) aktivatsiyalariga (`setWebRTCConfiguration`)
 * yozib qo'yamiz, aks holda ADM yoqilganda sozlama tiklanib qolardi.
 */
actual object LiveAudio {

    actual fun startPlayback() {
        runCatching {
            val options =
                AVAudioSessionCategoryOptionDefaultToSpeaker or AVAudioSessionCategoryOptionAllowBluetooth
            val category = AVAudioSessionCategoryPlayAndRecord
            val mode = AVAudioSessionModeVideoChat

            // WebRTC keyinroq audio'ni (qayta) yoqqanda ham karnayda qolishi uchun:
            val config = RTCAudioSessionConfiguration.webRTCConfiguration()
            if (category != null) config.category = category
            if (mode != null) config.mode = mode
            config.categoryOptions = options
            RTCAudioSessionConfiguration.setWebRTCConfiguration(config)

            val session = RTCAudioSession.sharedInstance()
            session.lockForConfiguration()
            try {
                session.setCategory(category, mode = mode, options = options, error = null)
                session.setActive(true, error = null)
            } finally {
                session.unlockForConfiguration()
            }
        }.onFailure { Logger.e("ParentLive", "audio startPlayback failed", it) }
    }

    actual fun stop() {
        runCatching {
            val session = RTCAudioSession.sharedInstance()
            session.lockForConfiguration()
            try {
                session.setActive(false, error = null)
            } finally {
                session.unlockForConfiguration()
            }
        }.onFailure { Logger.e("ParentLive", "audio stop failed", it) }
    }
}
