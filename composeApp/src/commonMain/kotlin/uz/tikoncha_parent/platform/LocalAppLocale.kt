package uz.tikoncha_parent.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import uz.tikoncha_parent.presentation.domain.model.LanguageType

var customAppLocale by mutableStateOf<String?>(null)

expect object LocalAppLocale {
    val current: String
        @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

@Composable
fun AppEnvironment(content: @Composable () -> Unit) {
    // Arab, fors, urdu — o'ngdan çapga; Android ham, iOS ham tizim emas, ilova tiliga qaraydi
    val direction = if (LanguageType.isRtlTag(customAppLocale)) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalAppLocale provides customAppLocale) {
        CompositionLocalProvider(LocalLayoutDirection provides direction) {
            content()
        }
    }
}