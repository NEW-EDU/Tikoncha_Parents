package uz.tikoncha_parent.platform

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleIdentifier
import platform.Foundation.preferredLanguages

actual fun languageDisplayName(tag: String, inTag: String): String =
    NSLocale(localeIdentifier = inTag).displayNameForKey(NSLocaleIdentifier, tag).orEmpty()

actual fun platformMonthNames(tag: String): List<String> {
    val f = NSDateFormatter()
    f.locale = NSLocale(localeIdentifier = tag)
    val names = f.standaloneMonthSymbols.mapNotNull { it as? String }
    return if (names.size == 12 && names.none { n -> n.startsWith("M0") || n.startsWith("M1") }) names else emptyList()
}

actual fun deviceLanguageTags(): List<String> = NSLocale.preferredLanguages.mapNotNull { it as? String }
