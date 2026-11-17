package uz.tikoncha_parent.presentation.assistant.voice

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

/** Yozib olingan ovozli savol: m4a (AAC, 16 kHz, mono). */
class RecordedAudio(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String,
    val durationMs: Long,
)

/**
 * Yordamçiga ovozli savol: AAC / MPEG-4 (.m4a), 16 kHz, mono, ~32 kbps — vaqtinçalik faylga, keyin baytlar
 * serverga (`POST /ai/stt`). Asosiy (UI) oqimdan çaqiriladi. Mikrofon ruxsati oldindan söralgan bölişi kerak.
 */
interface VoiceRecorder {
    /** Mikrofon sathi 0..1 (jonli halqa va jimlikni aniqlaş uçun); yozmayotganda 0. */
    val level: StateFlow<Float>

    /** Yozişni darhol boşlaydi; bölmasa (mikrofon band, ruxsat yöq) — false. */
    fun start(): Boolean

    /** Töxtatadi va yozuvni qaytaradi (fayl öçiriladi); böş yoki xato bölsa — null. */
    fun stop(): RecordedAudio?

    /** Töxtatadi va yozuvni taşlaydi. */
    fun cancel()
}

expect fun createVoiceRecorder(): VoiceRecorder

/**
 * Mikrofon ruxsatini söraydi (tizim oynasi); allaqachon berilgan bölsa darhol true.
 * Rad etilgan bölsa (yoki "boşqa söralmasin") — false: sozlamalarga yöllaymiz.
 */
@Composable
expect fun rememberMicPermissionRequest(): suspend () -> Boolean

/** Tizimda "harakatni kamaytiriş" yoqilgan (Android: animatsiyalar öçiq, iOS: Reduce Motion). */
expect fun isReducedMotion(): Boolean

/** Audio baytlarni vaqtinçalik (keş) faylga yozadi va faylning töliq yölini qaytaradi; bölmasa — null. */
expect fun writeTempAudio(bytes: ByteArray, fileName: String): String?

/**
 * Yordamçi javobini ovoz bilan öqiş: vaqtinçalik MP3 faylni ijro etadi (ovoz serverdan avtorizatsiya bilan
 * yuklab olinadi — URL'dan oqim pleyeri sarlavha yubora olmaydi). Asosiy oqimdan çaqiriladi.
 */
interface AnswerPlayer {
    /** [path] dagi faylni ijro etadi; boşlanmasa — false. Oxiriga yetganda yoki xatoda [onEnd] çaqiriladi. */
    fun play(path: String, onEnd: () -> Unit): Boolean

    /** Töxtatadi ([onEnd] çaqirilmaydi). */
    fun stop()
}

expect fun createAnswerPlayer(): AnswerPlayer
