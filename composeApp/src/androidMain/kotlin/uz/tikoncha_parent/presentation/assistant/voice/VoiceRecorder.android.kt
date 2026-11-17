package uz.tikoncha_parent.presentation.assistant.voice

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.platform.appContext
import java.io.File
import kotlin.math.log10

actual fun createVoiceRecorder(): VoiceRecorder = AndroidVoiceRecorder()

/** MediaRecorder: AAC / MPEG-4, 16 kHz, mono, 32 kbps — keş papkasidagi vaqtinçalik faylga. */
private class AndroidVoiceRecorder : VoiceRecorder {
    private val _level = MutableStateFlow(0f)
    override val level: StateFlow<Float> = _level.asStateFlow()

    private val scope = MainScope()
    private var recorder: MediaRecorder? = null
    private var file: File? = null
    private var startedAt = 0L
    private var poll: Job? = null

    override fun start(): Boolean {
        release(deleteFile = true)
        val context = appContext ?: return false
        val f = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        return try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioSamplingRate(16_000)
            r.setAudioChannels(1)
            r.setAudioEncodingBitRate(32_000)
            r.setOutputFile(f.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            file = f
            startedAt = SystemClock.elapsedRealtime()
            poll = scope.launch {
                while (isActive && recorder === r) {
                    // maxAmplitude — oxirgi çaqiruvdan beri eng baland nuqta (0..32767)
                    _level.value = normalize(runCatching { r.maxAmplitude }.getOrDefault(0))
                    delay(POLL_MS)
                }
            }
            true
        } catch (e: Exception) {
            Logger.e(TAG, "recorder start failed", e)
            runCatching { r.release() }
            f.delete()
            false
        }
    }

    override fun stop(): RecordedAudio? {
        val r = recorder ?: return null
        val f = file
        val duration = SystemClock.elapsedRealtime() - startedAt
        poll?.cancel()
        poll = null
        _level.value = 0f
        // Juda qisqa yozuvda stop() RuntimeException beradi — fayl yaroqsiz
        val stopped = runCatching { r.stop() }.isSuccess
        runCatching { r.release() }
        recorder = null
        file = null
        if (!stopped || f == null || !f.exists()) {
            f?.delete()
            return null
        }
        val bytes = runCatching { f.readBytes() }.getOrNull()
        f.delete()
        return bytes?.takeIf { it.isNotEmpty() }?.let { RecordedAudio(it, "voice.m4a", "audio/mp4", duration) }
    }

    override fun cancel() = release(deleteFile = true)

    private fun release(deleteFile: Boolean) {
        poll?.cancel()
        poll = null
        _level.value = 0f
        recorder?.let { r ->
            runCatching { r.stop() }
            runCatching { r.release() }
        }
        recorder = null
        if (deleteFile) file?.delete()
        file = null
    }

    /** dBFS → 0..1: −45 dB (jim xona) → 0, −5 dB (yaqindan baland gap) → 1. */
    private fun normalize(amplitude: Int): Float {
        if (amplitude <= 0) return 0f
        val db = 20f * log10(amplitude / 32767f)
        return ((db + 45f) / 40f).coerceIn(0f, 1f)
    }

    private companion object {
        const val TAG = "VoiceRecorder"
        const val POLL_MS = 60L
    }
}

@Composable
actual fun rememberMicPermissionRequest(): suspend () -> Boolean {
    val context = LocalContext.current
    val pending = remember { arrayOfNulls<CompletableDeferred<Boolean>>(1) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pending[0]?.complete(granted)
        pending[0] = null
    }
    return remember(launcher, context) {
        suspend {
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                val result = CompletableDeferred<Boolean>()
                pending[0]?.complete(false)
                pending[0] = result
                launcher.launch(Manifest.permission.RECORD_AUDIO)
                result.await()
            }
        }
    }
}

actual fun isReducedMotion(): Boolean {
    val context = appContext ?: return false
    return runCatching {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }.getOrDefault(false)
}

actual fun writeTempAudio(bytes: ByteArray, fileName: String): String? {
    val context = appContext ?: return null
    return runCatching {
        val f = File(context.cacheDir, fileName)
        f.writeBytes(bytes)
        f.absolutePath
    }.getOrNull()
}

actual fun createAnswerPlayer(): AnswerPlayer = AndroidAnswerPlayer()

/** MediaPlayer — keşdagi MP3 (nutq sifatida: USAGE_MEDIA / CONTENT_TYPE_SPEECH). */
private class AndroidAnswerPlayer : AnswerPlayer {
    private var player: MediaPlayer? = null

    override fun play(path: String, onEnd: () -> Unit): Boolean {
        stop()
        val mp = MediaPlayer()
        return try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            mp.setDataSource(path)
            mp.setOnCompletionListener {
                if (player === mp) {
                    stop()
                    onEnd()
                }
            }
            mp.setOnErrorListener { _, _, _ ->
                if (player === mp) {
                    stop()
                    onEnd()
                }
                true
            }
            mp.prepare()
            player = mp
            mp.start()
            true
        } catch (e: Exception) {
            Logger.e("AnswerPlayer", "play failed", e)
            if (player === mp) player = null
            runCatching { mp.release() }
            false
        }
    }

    override fun stop() {
        val mp = player ?: return
        player = null
        runCatching { mp.stop() }
        runCatching { mp.release() }
    }
}
