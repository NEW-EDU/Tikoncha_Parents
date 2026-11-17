package uz.tikoncha_parent.presentation.assistant

/**
 * Ovozli savol matni (STT) özbekçada eski lotinda keladi: o', g', sh, ch. Ota-ona pufagida u ilova yozuvida
 * körsatiladi — kirill ilovada kirillça, lotin ilovada 2026 lotini (ö ğ ş ç). Yordamçiga esa asl matn ketadi.
 *
 * Faqat lotin harfli sözlar özgaradi. Brend va çet sözlar (YouTube, TikTok, w / c bor sözlar) tegilmaydi.
 * "ts" ataylab "ц" ga aylantirilmaydi: "aytsa", "ketsa" kabi özbek sözlari buzilardi.
 */
internal object UzbekScript {

    private const val APOS = "'‘’ʻʼ`"
    private val WORD = Regex("[A-Za-z]+(?:['‘’ʻʼ`][A-Za-z]+)*")

    /** [language] — STT tili (ISO 639-3, "uzb"); [appLang] — ilova tili ("uz", "uz-Cyrl" …). */
    fun forDisplay(text: String, language: String?, appLang: String): String {
        val uzbek = language == null || language.startsWith("uz")
        if (!uzbek) return text
        return when (appLang) {
            "uz-Cyrl" -> toCyrillic(text)
            "uz" -> toReformedLatin(text)
            else -> text
        }
    }

    fun toReformedLatin(text: String): String = WORD.replace(text) { m ->
        val w = m.value
        if (isForeign(w)) w else convert(w, ::latinPiece)
    }

    fun toCyrillic(text: String): String = WORD.replace(text) { m ->
        val w = m.value
        if (isForeign(w)) w else convert(w, ::cyrillicPiece)
    }

    /** Ichida katta harf bor (YouTube), w yoki "h" siz c bor — özbekça emas. */
    private fun isForeign(w: String): Boolean {
        val letters = w.filter { it.isLetter() }
        val allCaps = letters.length > 1 && letters.all { it.isUpperCase() }
        if (!allCaps && letters.drop(1).any { it.isUpperCase() }) return true
        val lower = w.lowercase()
        if ('w' in lower) return true
        return lower.indices.any { lower[it] == 'c' && lower.getOrNull(it + 1) != 'h' }
    }

    /** Söz böyiça: har bölakni [piece] beradi (matn, neçta harf olindi), katta harf saqlanadi. */
    private fun convert(w: String, piece: (String, Int) -> Pair<String, Int>): String {
        val letters = w.filter { it.isLetter() }
        val allCaps = letters.length > 1 && letters.all { it.isUpperCase() }
        val out = StringBuilder()
        var i = 0
        while (i < w.length) {
            val (text, len) = piece(w, i)
            out.append(if (allCaps || w[i].isUpperCase()) text.uppercase() else text)
            i += len.coerceAtLeast(1)
        }
        return out.toString()
    }

    private fun latinPiece(w: String, i: Int): Pair<String, Int> {
        val c = w[i].lowercaseChar()
        val n = w.getOrNull(i + 1)?.lowercaseChar()
        return when {
            c == 'o' && n != null && n in APOS -> "ö" to 2
            c == 'g' && n != null && n in APOS -> "ğ" to 2
            c == 's' && n == 'h' -> "ş" to 2
            c == 'c' && n == 'h' -> "ç" to 2
            else -> w[i].lowercase() to 1
        }
    }

    private fun cyrillicPiece(w: String, i: Int): Pair<String, Int> {
        val c = w[i].lowercaseChar()
        val n = w.getOrNull(i + 1)?.lowercaseChar()
        val n2 = w.getOrNull(i + 2)
        return when {
            c == 'o' && n != null && n in APOS -> "ў" to 2
            c == 'g' && n != null && n in APOS -> "ғ" to 2
            c == 's' && n == 'h' -> "ш" to 2
            c == 'c' && n == 'h' -> "ч" to 2
            // "yo'q" — й + ў, "yoz" — ё
            c == 'y' && n == 'o' && (n2 == null || n2 !in APOS) -> "ё" to 2
            c == 'y' && n == 'u' -> "ю" to 2
            c == 'y' && n == 'a' -> "я" to 2
            c == 'y' && n == 'e' -> "е" to 2
            c == 'e' && i == 0 -> "э" to 1
            c in APOS -> "ъ" to 1
            else -> (CYRILLIC[c] ?: w[i].lowercase()) to 1
        }
    }

    private val CYRILLIC = mapOf(
        'a' to "а", 'b' to "б", 'd' to "д", 'e' to "е", 'f' to "ф", 'g' to "г", 'h' to "ҳ", 'i' to "и",
        'j' to "ж", 'k' to "к", 'l' to "л", 'm' to "м", 'n' to "н", 'o' to "о", 'p' to "п", 'q' to "қ",
        'r' to "р", 's' to "с", 't' to "т", 'u' to "у", 'v' to "в", 'x' to "х", 'y' to "й", 'z' to "з",
    )
}
