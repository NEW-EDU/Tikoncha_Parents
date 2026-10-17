package uz.tikoncha_parent.platform

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun deviceLanguageCode(): String =
    (NSLocale.preferredLanguages.firstOrNull() as? String)?.substringBefore('-')?.lowercase().orEmpty()
