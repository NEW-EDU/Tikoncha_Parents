package uz.tikoncha_parent.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeController {
    private val _mode = MutableStateFlow(ThemePrefs.load())
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    init {
        // App startida iOS window style ni to'g'rilash (Android uchun no-op)
        PlatformThemeBridge.applyInitial(_mode.value)
    }

    fun setMode(newMode: ThemeMode) {
        if (_mode.value == newMode) return
        _mode.value = newMode
        ThemePrefs.save(newMode)
        PlatformThemeBridge.onModeChanged(newMode)
    }
}

/**
 * Qorong'i fonli ekranlar (tanishtiruv) ochiq turganda status bar belgilari oq bo'lishi uchun.
 * Hisoblagich — bir nechta shunday ekran ketma-ket ochilsa ham to'g'ri ishlaydi.
 */
object SystemBarOverride {
    var lightIconScreens by androidx.compose.runtime.mutableIntStateOf(0)
}
