package uz.tikoncha_parent.platform

import android.content.res.Resources
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

actual fun languageDisplayName(tag: String, inTag: String): String {
    val loc = Locale.forLanguageTag(tag)
    val name = loc.getDisplayName(Locale.forLanguageTag(inTag))
    return if (name.isBlank() || name.equals(loc.toLanguageTag(), ignoreCase = true)) "" else name
}

actual fun platformMonthNames(tag: String): List<String> {
    val loc = Locale.forLanguageTag(tag)
    val names = Month.entries.map { it.getDisplayName(TextStyle.FULL_STANDALONE, loc) }
    // Tizimda til ma'lumoti bo'lmasa raqam ("1") yoki "M01" qaytadi — unda bo'sh ro'yxat
    return if (names.any { n -> n.isBlank() || n.all { it.isDigit() } || n.startsWith("M0") || n.startsWith("M1") })
        emptyList() else names
}

actual fun deviceLanguageTags(): List<String> {
    val list = Resources.getSystem().configuration.locales
    return (0 until list.size()).map { list[it].toLanguageTag() }
}
