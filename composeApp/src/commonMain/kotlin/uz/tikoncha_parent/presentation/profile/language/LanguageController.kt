package uz.tikoncha_parent.presentation.profile.language

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uz.tikoncha_parent.platform.customAppLocale

import uz.tikoncha_parent.presentation.domain.model.LanguageType

@Stable
class LanguageController {

    private val _current = MutableStateFlow(LanguagePrefs.loadOrDefault())
    val current: StateFlow<LanguageType> = _current

    init {
        customAppLocale = current.value.localeTag
    }
    fun select(newLang: LanguageType) {
        if (newLang == _current.value) return
        _current.value = newLang
        LanguagePrefs.save(newLang)
        customAppLocale = newLang.localeTag
    }
}

/** Ekranlarda controllerga Compose orqali kirish uchun Local */
val LocalLanguageController = staticCompositionLocalOf<LanguageController> {
    error("LanguageController is not provided")
}