package uz.tikoncha_parent.presentation.voice_help

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * "Ovozli yordam" — har ekrandagi «Tinglaş» tugmasi körinsinmi (Profil → Köriniş va til). Standart: yoqiq.
 * Öçiq bölsa hamma «Tinglaş» tugmalari (Bosh sahifa oroli, [uz.tikoncha_parent.presentation.base.CustomHeader]
 * oroli, xarita) yaşirinadi va joy egallamaydi.
 */
object VoiceHelpPrefs {
    private const val KEY = "voice_help_enabled"
    private val settings: Settings = Settings() // Android: SharedPrefs, iOS: UserDefaults

    private val _enabled = MutableStateFlow(settings.getBoolean(KEY, true))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun set(value: Boolean) {
        if (_enabled.value == value) return
        _enabled.value = value
        settings[KEY] = value
    }
}
