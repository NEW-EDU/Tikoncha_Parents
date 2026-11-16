package uz.tikoncha_parent.ui.theme

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Ilova ichidagi matn o'lchami (telefon sozlamasidagi shrift o'lchamiga QO'SHIMCHA ko'paytiriladi).
 * Ko'p ota-onalar telefon sozlamasini topa olmaydi — shu sabab ilovaning o'zida ham bor.
 */
enum class TextSize(val scale: Float) {
    SMALL(0.9f),
    STANDARD(1.0f),
    LARGE(1.15f),
    EXTRA_LARGE(1.3f),
}

object TextSizeController {
    private const val KEY = "text_size"
    private val settings: Settings = Settings()

    private val _size = MutableStateFlow(load())
    val size: StateFlow<TextSize> = _size.asStateFlow()

    private fun load(): TextSize =
        settings.getStringOrNull(KEY)?.let { code -> runCatching { TextSize.valueOf(code) }.getOrNull() } ?: TextSize.STANDARD

    fun set(value: TextSize) {
        if (_size.value == value) return
        _size.value = value
        settings[KEY] = value.name
    }
}
