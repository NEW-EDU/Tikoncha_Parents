package uz.tikoncha_parent.presentation.voice_help

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * "Ovozli yordam" (Profil → Köriniş va til). Standart: yoqiq. Yoqiq bölsa: ekranga birinçi kirişda tuşuntiriş
 * özi ijro etiladi, keyin "silkiting" maslahati, telefon silkitilsa — «Bu ekran haqida» varag'i ([VoiceHelpLayer]).
 * Öçirilsa — ijro töxtaydi, avto-ijro, maslahat va silkitiş öçadi.
 */
object VoiceHelpPrefs {
    private const val KEY = "voice_help_enabled"
    /** Tuşuntirişi bir marta eşittirilgan ekranlar ([VoiceScreen.id], vergul bilan). */
    private const val KEY_SEEN = "voice_help_seen"
    /** "Silkiting" maslahati necha marta körsatildi. */
    private const val KEY_SHAKE_HINTS = "voice_help_shake_hints"
    private const val MAX_SHAKE_HINTS = 3

    private val settings: Settings = Settings() // Android: SharedPrefs, iOS: UserDefaults

    private val _enabled = MutableStateFlow(settings.getBoolean(KEY, true))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    /** Shu seansda avto-ijrosi muvaffaqiyatsiz bölgan ekranlar (internet yöq, 404) — keyingi işga tuşirişda qayta. */
    private val failedThisSession = mutableSetOf<VoiceScreen>()

    fun set(value: Boolean) {
        if (_enabled.value == value) return
        _enabled.value = value
        settings[KEY] = value
        if (!value) VoiceHelpPlayback.stopAll()
    }

    private fun seenIds(): Set<String> =
        settings.getString(KEY_SEEN, "").split(',').filter { it.isNotBlank() }.toSet()

    /** Ekranga birinçi kirişda tuşuntiriş özi boşlansinmi. */
    internal fun shouldAutoPlay(screen: VoiceScreen): Boolean =
        _enabled.value && screen !in failedThisSession && screen.id !in seenIds()

    /** Ijro haqiqatan boşlandi — endi bu ekranda özi boşlanmaydi. */
    internal fun markSeen(screen: VoiceScreen) {
        val seen = seenIds()
        if (screen.id !in seen) settings[KEY_SEEN] = (seen + screen.id).joinToString(",")
    }

    internal fun markAutoPlayFailed(screen: VoiceScreen) {
        failedThisSession += screen
    }

    /** "Silkiting" maslahati uçun navbat: jami [MAX_SHAKE_HINTS] marta; true — körsatiş mumkin (hisoblandi). */
    internal fun takeShakeHint(): Boolean {
        val shown = settings.getInt(KEY_SHAKE_HINTS, 0)
        if (shown >= MAX_SHAKE_HINTS) return false
        settings[KEY_SHAKE_HINTS] = shown + 1
        return true
    }

    /** Foydalanuvçi silkitişni bildi — maslahat endi kerak emas. */
    internal fun markShakeUsed() {
        settings[KEY_SHAKE_HINTS] = MAX_SHAKE_HINTS
    }

    /** Sinov uçun: hamma ekran yana "birinçi kiriş", maslahat yana 3 marta (debug: `-hoglt.resethelp`). */
    fun resetFirstVisit() {
        settings.remove(KEY_SEEN)
        settings.remove(KEY_SHAKE_HINTS)
        failedThisSession.clear()
    }
}
