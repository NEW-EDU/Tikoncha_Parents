package uz.tikoncha_parent.domain.model.policy

/**
 * Jadval nomi — ekranda QAYSI tilda chiqishi uchun.
 *
 * Server nomni yaratgan odamning tilida saqlaydi: ota-ona ilovasi inglizcha
 * bo'lsa "Bedtime", ruscha bo'lsa "Время сна" — bolaga ham shunday kelardi.
 * Tayyor jadval, tezkor blok va standart nomlar ("Yangi jadval", eski
 * ilovalardagi "Jadval", "Toifalar bo'yicha bloklash") esa hamma uchun bir
 * xil ma'noda — ular ko'rayotgan odamning ilova tilida chiqadi
 * (`PolicyName.text()`, UI). Odam o'zi yozgan nom o'zgarmaydi.
 *
 * Tayyor jadvallarni ilovalarda qayta nomlab bo'lmaydi, shuning uchun
 * `preset` bo'lsa nom doim tarjima qilinadi. `preset` noma'lum joylarda
 * (tarix, eski ma'lumot) nom uch tildagi ma'lum variantlar bilan solishtiriladi.
 */
data class PolicyName(val raw: String, val kind: Kind) {

    enum class Kind { CUSTOM, SLEEP, APP_LIMIT, SCHOOL, CONTENT_PROTECTION, QUICK_BLOCK, NEW_SCHEDULE, SCHEDULE, CATEGORY_BLOCK }

    companion object {
        fun custom(raw: String) = PolicyName(raw, Kind.CUSTOM)

        fun of(raw: String, kind: PolicyKind = PolicyKind.STANDARD, preset: PolicyPreset? = null): PolicyName {
            val k = when {
                kind == PolicyKind.QUICK_BLOCK -> Kind.QUICK_BLOCK
                preset == PolicyPreset.SLEEP -> Kind.SLEEP
                preset == PolicyPreset.APP_LIMIT -> Kind.APP_LIMIT
                preset == PolicyPreset.SCHOOL -> Kind.SCHOOL
                preset == PolicyPreset.PROTECTION -> Kind.CONTENT_PROTECTION
                else -> KNOWN[normalize(raw)] ?: Kind.CUSTOM
            }
            return PolicyName(raw, k)
        }

        // KNOWN dan OLDIN — companion maydonlari yozilgan tartibda ishga tushadi
        private val APOSTROPHES = Regex("[‘’ʻʼ`´]")
        private val SPACES = Regex("\\s+")

        /** Ilovalar (uz/ru/en) va server bergan standart nomlar. Matn emas — faqat tanish uchun. */
        private val KNOWN: Map<String, Kind> = buildMap {
            fun put(kind: Kind, vararg names: String) = names.forEach { put(normalize(it), kind) }
            put(Kind.SLEEP, "Uyqu vaqti", "Время сна", "Bedtime")
            put(Kind.APP_LIMIT, "Vaqt limiti", "Лимит времени", "Time limit")
            put(Kind.SCHOOL, "Dars vaqti", "Время уроков", "School hours")
            put(Kind.CONTENT_PROTECTION, "Kontent himoya", "Защита контента", "Content protection")
            put(Kind.QUICK_BLOCK, "Tezkor blok", "Быстрая блокировка", "Quick block")
            put(Kind.NEW_SCHEDULE, "Yangi jadval", "Новое расписание", "New schedule")
            // Server: v1 → v2 ko'chirishda nomsiz qoidalar (`v1_copy.DEFAULT_NAME`)
            put(Kind.SCHEDULE, "Jadval", "Расписание", "Schedule")
            // Eski ilovalar kategoriya qoidasiga shu nomni berardi
            put(Kind.CATEGORY_BLOCK, "Toifalar bo'yicha bloklash", "Блокировка по категориям", "Block by categories")
        }

        private fun normalize(s: String): String =
            s.trim().replace(APOSTROPHES, "'").replace(SPACES, " ").lowercase()
    }
}
