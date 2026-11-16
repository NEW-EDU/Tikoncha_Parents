package uz.tikoncha_parent.ui.theme

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

object ThemePrefs {
    private const val KEY = "theme_mode"
    private val settings: Settings = Settings() // Android: SharedPrefs, iOS: UserDefaults

    fun load(): ThemeMode {
        // Standart — yorug' mavzu: telefoni qorong'i rejimda turgan (ko'pincha o'zi bilmagan holda) kattalarga qorong'i
        // ekran noqulay. "Tizim" va "Qorong'i" Sozlamalar → Mavzu orqali tanlanadi.
        val code = settings.getStringOrNull(KEY) ?: return ThemeMode.LIGHT
        return runCatching { ThemeMode.valueOf(code) }.getOrDefault(ThemeMode.LIGHT)
    }

    fun save(mode: ThemeMode) {
        settings[KEY] = mode.name
    }
}