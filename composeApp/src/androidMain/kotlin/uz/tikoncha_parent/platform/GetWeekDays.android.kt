@file:Suppress("DEPRECATION")

package uz.tikoncha_parent.platform

import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import uz.tikoncha_parent.presentation.domain.model.LanguageType

actual fun getWeekDays(language: LanguageType): List<String> {
    val locale = Locale.forLanguageTag(language.localeTag)

    return DayOfWeek.entries.map { day ->
        day.getDisplayName(TextStyle.NARROW,locale)
            .replace(".","")
            .replaceFirstChar { it.uppercaseChar() }
    }
}