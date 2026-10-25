package uz.tikoncha_parent.presentation.domain.model

import org.jetbrains.compose.resources.DrawableResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.lang_en
import tikoncha_parents.composeapp.generated.resources.lang_ru
import tikoncha_parents.composeapp.generated.resources.lang_uz

/**
 * Ilova tillari (2026-10-25: 30 ta). [languageCode] — serverga (`LANG-CODE`) va saqlashga; [localeTag] — resurs
 * tanlash uçun (Compose resurslari yozuvni bilmaydi: özbek kirill `values-uz-rUZ` da, lotin — `values-uz`).
 * Matnlar `composeResources/values-<til>` da — `Shared_materials/i18n/apply.py` yaratadi.
 */
enum class LanguageType(
    val languageCode: String,
    val languageName: String,
    val flag: String,
    val greeting: String,
    val tagline: String,
    val todayWord: String,
    val isRtl: Boolean = false,
    val iconId: DrawableResource? = null,
    val localeTag: String = languageCode,
) {
    UZ("uz", "Özbekça", "🇺🇿", "Assalomu alaykum!", "Ilova özbek tilida", "Bugun", iconId = Res.drawable.lang_uz),
    UZ_CYRL("uz-Cyrl", "Ўзбекча", "🇺🇿", "Ассалому алайкум!", "Илова ўзбек тилида", "Бугун", localeTag = "uz-Cyrl-UZ"),
    RU("ru", "Русский", "🇷🇺", "Здравствуйте!", "Приложение на русском", "Сегодня", iconId = Res.drawable.lang_ru),
    KAA("kaa", "Qaraqalpaqsha", "🇺🇿", "Assalawma áleykum!", "Qosımsha qaraqalpaq tilinde", "Búgin"),
    EN("en", "English", "🇬🇧", "Hello!", "The app is in English", "Today", iconId = Res.drawable.lang_en),
    KK("kk", "Қазақша", "🇰🇿", "Сәлеметсіз бе!", "Қолданба қазақ тілінде", "Бүгін"),
    KY("ky", "Кыргызча", "🇰🇬", "Саламатсызбы!", "Тиркеме кыргыз тилинде", "Бүгүн"),
    TG("tg", "Тоҷикӣ", "🇹🇯", "Салом!", "Барнома бо забони тоҷикӣ", "Имрӯз"),
    TK("tk", "Türkmençe", "🇹🇲", "Salam!", "Programma türkmen dilinde", "Şu gün"),
    AZ("az", "Azərbaycanca", "🇦🇿", "Salam!", "Tətbiq Azərbaycan dilindədir", "Bu gün"),
    TR("tr", "Türkçe", "🇹🇷", "Merhaba!", "Uygulama Türkçe", "Bugün"),
    FA("fa", "فارسی", "🇮🇷", "سلام!", "برنامه به زبان فارسی است", "امروز", isRtl = true),
    AR("ar", "العربية", "🇸🇦", "مرحبًا!", "التطبيق باللغة العربية", "اليوم", isRtl = true),
    UR("ur", "اردو", "🇵🇰", "السلام علیکم!", "ایپ اردو میں ہے", "آج", isRtl = true),
    UK("uk", "Українська", "🇺🇦", "Вітаємо!", "Застосунок українською", "Сьогодні"),
    KA("ka", "ქართული", "🇬🇪", "გამარჯობა!", "აპი ქართულ ენაზეა", "დღეს"),
    HY("hy", "Հայերեն", "🇦🇲", "Բարև ձեզ!", "Հավելվածը հայերեն է", "Այսօր"),
    MN("mn", "Монгол", "🇲🇳", "Сайн байна уу!", "Апп монгол хэл дээр", "Өнөөдөр"),
    ES("es", "Español", "🇪🇸", "¡Hola!", "La app está en español", "Hoy"),
    PT_BR("pt-BR", "Português (Brasil)", "🇧🇷", "Olá!", "O app está em português", "Hoje"),
    HI("hi", "हिन्दी", "🇮🇳", "नमस्ते!", "ऐप हिन्दी में है", "आज"),
    BN("bn", "বাংলা", "🇧🇩", "নমস্কার!", "অ্যাপটি বাংলায়", "আজ"),
    ID("id", "Bahasa Indonesia", "🇮🇩", "Halo!", "Aplikasi dalam Bahasa Indonesia", "Hari ini"),
    MS("ms", "Bahasa Melayu", "🇲🇾", "Helo!", "Aplikasi dalam Bahasa Melayu", "Hari ini"),
    VI("vi", "Tiếng Việt", "🇻🇳", "Xin chào!", "Ứng dụng bằng tiếng Việt", "Hôm nay"),
    FR("fr", "Français", "🇫🇷", "Bonjour !", "L'application est en français", "Aujourd'hui"),
    DE("de", "Deutsch", "🇩🇪", "Hallo!", "Die App ist auf Deutsch", "Heute"),
    ZH_HANS("zh-Hans", "简体中文", "🇨🇳", "你好！", "应用为简体中文", "今天"),
    JA("ja", "日本語", "🇯🇵", "こんにちは！", "アプリは日本語です", "今日"),
    KO("ko", "한국어", "🇰🇷", "안녕하세요!", "앱이 한국어로 표시됩니다", "오늘");

    /** Özbek oilasi (lotin, kirill, qoraqalpoq) — Tikoncha brendi. */
    val isUzbekFamily: Boolean get() = this == UZ || this == UZ_CYRL || this == KAA

    companion object {
        fun getLangType(code: String?): LanguageType = fromTag(code) ?: UZ

        /** Teg (uz, uz-Cyrl, uz-Cyrl-UZ, pt-BR, pt, zh-Hans, zh-CN, in, id …) → til; topilmasa null. */
        fun fromTag(tag: String?): LanguageType? {
            if (tag.isNullOrBlank()) return null
            val t = tag.replace('_', '-').lowercase()
            entries.firstOrNull { it.languageCode.lowercase() == t || it.localeTag.lowercase() == t }?.let { return it }
            val parts = t.split('-')
            val lang = parts[0].let { if (it == "in") "id" else it }
            return when {
                lang == "uz" && parts.any { it == "cyrl" } -> UZ_CYRL
                lang == "uz" -> UZ
                lang == "pt" -> PT_BR
                lang == "zh" -> ZH_HANS
                lang == "prs" -> FA
                else -> entries.firstOrNull { it.languageCode == lang }
            }
        }

        /** Telefon tillari ro'yxatidan eng mosi; hech biri mos kelmasa — ingliz (global brend Hoglt). */
        fun bestMatch(tags: List<String>): LanguageType = tags.firstNotNullOfOrNull { fromTag(it) } ?: EN

        fun isRtlTag(tag: String?): Boolean = fromTag(tag)?.isRtl == true
    }
}
