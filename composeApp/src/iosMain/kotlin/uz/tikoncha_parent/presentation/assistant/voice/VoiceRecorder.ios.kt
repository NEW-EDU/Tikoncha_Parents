@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package uz.tikoncha_parent.presentation.assistant.voice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioQualityMedium
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionCategorySoloAmbient
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVEncoderBitRateKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled
import platform.posix.memcpy
import uz.tikoncha_parent.platform.Logger
import kotlin.coroutines.resume

actual fun createVoiceRecorder(): VoiceRecorder = IosVoiceRecorder()

/**
 * AVAudioRecorder: AAC / MPEG-4 (.m4a), 16 kHz, mono, 32 kbps — tmp papkaga. Yoziş paytida AVAudioSession
 * `.playAndRecord` (karnay orqali), töxtaganda oldingi toifa qaytariladi. Sath — `averagePowerForChannel`.
 */
private class IosVoiceRecorder : VoiceRecorder {
    private val _level = MutableStateFlow(0f)
    override val level: StateFlow<Float> = _level.asStateFlow()

    private val scope = MainScope()
    private var recorder: AVAudioRecorder? = null
    private var url: NSURL? = null
    private var poll: Job? = null
    private var previousCategory: String? = null

    override fun start(): Boolean {
        release(deleteFile = true)
        val session = AVAudioSession.sharedInstance()
        previousCategory = session.category
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, withOptions = AVAudioSessionCategoryOptionDefaultToSpeaker, error = null)
        session.setActive(true, error = null)

        val path = NSTemporaryDirectory() + "voice_${kotlin.random.Random.nextLong(1, Long.MAX_VALUE)}.m4a"
        val fileUrl = NSURL.fileURLWithPath(path)
        val settings: Map<Any?, Any?> = mapOf(
            AVFormatIDKey to NSNumber(unsignedInt = kAudioFormatMPEG4AAC),
            AVSampleRateKey to NSNumber(double = 16_000.0),
            AVNumberOfChannelsKey to NSNumber(int = 1),
            AVEncoderBitRateKey to NSNumber(int = 32_000),
            AVEncoderAudioQualityKey to NSNumber(long = AVAudioQualityMedium),
        )
        val rec = AVAudioRecorder(uRL = fileUrl, settings = settings, error = null)
        rec.meteringEnabled = true
        if (!rec.prepareToRecord() || !rec.record()) {
            Logger.e(TAG, "recorder start failed", null)
            rec.stop()
            NSFileManager.defaultManager.removeItemAtURL(fileUrl, error = null)
            restoreSession()
            return false
        }
        recorder = rec
        url = fileUrl
        poll = scope.launch {
            while (isActive && recorder === rec) {
                rec.updateMeters()
                // dBFS: −50 (jim xona) → 0, −10 (yaqindan gap) → 1
                val db = rec.averagePowerForChannel(0u)
                _level.value = ((db + 50f) / 40f).coerceIn(0f, 1f)
                delay(POLL_MS)
            }
        }
        return true
    }

    override fun stop(): RecordedAudio? {
        val rec = recorder ?: return null
        val fileUrl = url
        val duration = (rec.currentTime * 1000).toLong()
        poll?.cancel()
        poll = null
        _level.value = 0f
        rec.stop()
        recorder = null
        url = null
        restoreSession()
        if (fileUrl == null) return null
        val bytes = NSData.dataWithContentsOfURL(fileUrl)?.toByteArray()
        NSFileManager.defaultManager.removeItemAtURL(fileUrl, error = null)
        return bytes?.takeIf { it.isNotEmpty() }?.let { RecordedAudio(it, "voice.m4a", "audio/mp4", duration) }
    }

    override fun cancel() = release(deleteFile = true)

    private fun release(deleteFile: Boolean) {
        poll?.cancel()
        poll = null
        _level.value = 0f
        val rec = recorder
        if (rec != null) {
            rec.stop()
            restoreSession()
        }
        recorder = null
        if (deleteFile) url?.let { NSFileManager.defaultManager.removeItemAtURL(it, error = null) }
        url = null
    }

    private fun restoreSession() {
        val session = AVAudioSession.sharedInstance()
        session.setActive(false, withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, error = null)
        session.setCategory(previousCategory ?: AVAudioSessionCategorySoloAmbient, error = null)
    }

    private companion object {
        const val TAG = "VoiceRecorder"
        const val POLL_MS = 60L
    }
}

private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val out = ByteArray(size)
    if (size > 0) out.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return out
}

@Composable
actual fun rememberMicPermissionRequest(): suspend () -> Boolean = remember {
    suspend {
        val session = AVAudioSession.sharedInstance()
        when (session.recordPermission) {
            AVAudioSessionRecordPermissionGranted -> true
            AVAudioSessionRecordPermissionDenied -> false
            else -> suspendCancellableCoroutine { cont ->
                session.requestRecordPermission { granted -> if (cont.isActive) cont.resume(granted) }
            }
        }
    }
}

actual fun isReducedMotion(): Boolean = UIAccessibilityIsReduceMotionEnabled()

actual fun writeTempAudio(bytes: ByteArray, fileName: String): String? {
    if (bytes.isEmpty()) return null
    val path = NSTemporaryDirectory() + fileName
    val data = bytes.usePinned { NSData.dataWithBytes(it.addressOf(0), bytes.size.toULong()) }
    return if (data.writeToFile(path, atomically = true)) path else null
}

actual fun createAnswerPlayer(): AnswerPlayer = IosAnswerPlayer()

/**
 * AVAudioPlayer — tmp'dagi MP3. Ijro paytida AVAudioSession `.playback` (telefon ovozsiz rejimda bölsa ham
 * eşitiladi), töxtaganda SoloAmbient. Tugaşi 0,2 s'da bir tekşiriladi.
 */
private class IosAnswerPlayer : AnswerPlayer {
    private val scope = MainScope()
    private var player: AVAudioPlayer? = null
    private var poll: Job? = null

    override fun play(path: String, onEnd: () -> Unit): Boolean {
        stop()
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)
        val p = AVAudioPlayer(contentsOfURL = NSURL.fileURLWithPath(path), error = null)
        if (!p.prepareToPlay() || !p.play()) {
            session.setCategory(AVAudioSessionCategorySoloAmbient, error = null)
            return false
        }
        player = p
        poll = scope.launch {
            delay(300)
            while (isActive && player === p && p.playing) delay(200)
            if (player === p) {
                stop()
                onEnd()
            }
        }
        return true
    }

    override fun stop() {
        poll?.cancel()
        poll = null
        val p = player ?: return
        player = null
        p.stop()
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategorySoloAmbient, error = null)
    }
}
