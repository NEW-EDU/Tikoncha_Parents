package uz.tikoncha_parent.platform

/** [tag] tilining nomi [inTag] tilida ("ru" → "rus tili" / "Russian"); topilmasa bo'sh. */
expect fun languageDisplayName(tag: String, inTag: String): String

/** Oy nomlari (yanvar…dekabr, mustaqil shakl) [tag] tilida; tizim bilmasa bo'sh ro'yxat. */
expect fun platformMonthNames(tag: String): List<String>

/** Telefon tillari, afzallik tartibida (BCP-47). */
expect fun deviceLanguageTags(): List<String>
