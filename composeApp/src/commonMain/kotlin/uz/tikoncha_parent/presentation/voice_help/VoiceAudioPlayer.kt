package uz.tikoncha_parent.presentation.voice_help

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class VoiceState { Idle, Loading, Playing, Error }

/** URL'dagi bitta qisqa audio (mp3). Tugasa — [VoiceState.Idle]; 404, tarmoq xatosi va h.k. — [VoiceState.Error]. */
internal interface VoiceAudioPlayer {
    val state: StateFlow<VoiceState>
    fun play(url: String)
    /** Töxtatadi va pleyerni böşatadi: [VoiceState.Idle], [failed] bölsa — [VoiceState.Error]. */
    fun stop(failed: Boolean)
}

/** Android — media3 ExoPlayer, iOS — AVPlayer. Asosiy (UI) oqimdan çaqiriladi. */
internal expect fun createVoiceAudioPlayer(): VoiceAudioPlayer

/**
 * Ilova böyiça bitta ovozli tuşuntiriş: yangisi boşlansa, eskisi töxtaydi. [owner] — hozir qaysi tugma ijro
 * etyapti (har tugma nusxasining öz belgisi), şuning uçun faqat öşa tugma "Töxtatiş" körsatadi va faqat u
 * ekrandan çiqqanda ovozni töxtatadi.
 */
internal object VoiceHelpPlayback {
    /** Server javob bermasa ham tugma abadiy "yuklanmoqda"da qolmasin. */
    private const val LOAD_TIMEOUT_MS = 20_000L

    private val player: VoiceAudioPlayer by lazy { createVoiceAudioPlayer() }
    private val scope = MainScope()
    private var watchdog: Job? = null

    private val _owner = MutableStateFlow<Any?>(null)
    val owner: StateFlow<Any?> = _owner.asStateFlow()
    val state: StateFlow<VoiceState> get() = player.state

    fun play(owner: Any, url: String) {
        watchdog?.cancel()
        _owner.value = owner
        player.play(url)
        watchdog = scope.launch {
            delay(LOAD_TIMEOUT_MS)
            if (_owner.value === owner && player.state.value == VoiceState.Loading) player.stop(failed = true)
        }
    }

    /** Faqat [owner] ijro etayotgan bölsa töxtatadi (boşqa ekranning ovoziga tegmaydi). */
    fun stop(owner: Any) {
        if (_owner.value !== owner) return
        watchdog?.cancel()
        watchdog = null
        _owner.value = null
        player.stop(failed = false)
    }
}
