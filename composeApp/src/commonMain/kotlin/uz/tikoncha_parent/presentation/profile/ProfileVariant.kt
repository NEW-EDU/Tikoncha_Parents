package uz.tikoncha_parent.presentation.profile

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Profil menyusining körinişi — ota-onalar bilan suhbatlarda (customer development) ikkala variantni sinaş uçun.
 * [FLAT] (A) — standart: ixçam ro'yxat, kiçik tekis ikonkalar. [ICONS_3D] (B) — keng qatorlar, 60dp 3D rasmlar.
 * Yaşirin almaştiriş: Profil pastidagi versiya matnini uzoq bosiş (release/TestFlight'da ham işlaydi).
 */
enum class ProfileVariant { FLAT, ICONS_3D }

object ProfileVariantController {
    private const val KEY = "profile_variant"
    private val settings: Settings = Settings() // Android: SharedPrefs, iOS: UserDefaults

    private val _variant = MutableStateFlow(load())
    val variant: StateFlow<ProfileVariant> = _variant.asStateFlow()

    private fun load(): ProfileVariant =
        settings.getStringOrNull(KEY)?.let { code -> runCatching { ProfileVariant.valueOf(code) }.getOrNull() }
            ?: ProfileVariant.FLAT

    fun set(value: ProfileVariant) {
        if (_variant.value == value) return
        _variant.value = value
        settings[KEY] = value.name
    }

    /** A ↔ B; yangi variantni qaytaradi. */
    fun toggle(): ProfileVariant {
        val next = if (_variant.value == ProfileVariant.FLAT) ProfileVariant.ICONS_3D else ProfileVariant.FLAT
        set(next)
        return next
    }
}
