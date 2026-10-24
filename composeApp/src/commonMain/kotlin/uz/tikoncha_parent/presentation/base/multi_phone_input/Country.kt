package uz.tikoncha_parent.presentation.base.multi_phone_input

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.country_uz
import tikoncha_parents.composeapp.generated.resources.country_ru
import tikoncha_parents.composeapp.generated.resources.country_kz
import tikoncha_parents.composeapp.generated.resources.country_kg
import tikoncha_parents.composeapp.generated.resources.country_tj
import tikoncha_parents.composeapp.generated.resources.country_tm
import tikoncha_parents.composeapp.generated.resources.country_tr
import tikoncha_parents.composeapp.generated.resources.country_af
import tikoncha_parents.composeapp.generated.resources.country_ae
import tikoncha_parents.composeapp.generated.resources.country_us
import tikoncha_parents.composeapp.generated.resources.country_gb
import tikoncha_parents.composeapp.generated.resources.country_de
import tikoncha_parents.composeapp.generated.resources.country_kr
import tikoncha_parents.composeapp.generated.resources.country_fr
import tikoncha_parents.composeapp.generated.resources.country_it
import tikoncha_parents.composeapp.generated.resources.country_es
import tikoncha_parents.composeapp.generated.resources.country_nl
import tikoncha_parents.composeapp.generated.resources.country_pl
import tikoncha_parents.composeapp.generated.resources.country_ua
import tikoncha_parents.composeapp.generated.resources.country_az
import tikoncha_parents.composeapp.generated.resources.country_in
import tikoncha_parents.composeapp.generated.resources.country_cn
import tikoncha_parents.composeapp.generated.resources.country_jp
import tikoncha_parents.composeapp.generated.resources.country_ca
import tikoncha_parents.composeapp.generated.resources.country_au

/**
 * Bitta davlat uchun ma'lumot.
 *
 * @param iso     ISO 3166-1 alpha-2 kodi ("UZ", "RU"...). Unique identifier sifatida ishlatiladi.
 * @param nameRes Ko'rinadigan nomi — string resource (uz/ru/en avtomatik tanlanadi).
 * @param dial    Xalqaro kod ("+998").
 * @param flag    Emoji bayroq.
 * @param mask    Raqam formati. '#' — bitta raqam o'rni, qolgan belgilar (probel) — ajratuvchi.
 *                Masalan "## ### ## ##" -> 90 123 45 67
 */
@Immutable
data class Country(
    val iso: String,
    val nameRes: StringResource,
    val dial: String,
    val flag: String,
    val mask: String,
    val knownPrefixes: Set<String> = emptySet()
) {
    /** Mask bo'yicha kiritish mumkin bo'lgan maksimal raqamlar soni. */
    val maxDigits: Int get() = mask.count { it == '#' }

    fun isComplete(digits: String): Boolean =
        digits.length == maxDigits && digits.all { it.isDigit() }

    /** Prefiks ma'lum ro'yxatda yo'q — foydalanuvchiga izoh ko'rsatiladi. */
    fun hasUnknownPrefix(digits: String): Boolean =
        knownPrefixes.isNotEmpty() &&
                digits.length >= 2 &&
                knownPrefixes.none { digits.startsWith(it) }
}

/**
 * Qo'llab-quvvatlanadigan davlatlar.
 * Nomlar string resource orqali keladi -> til (uz/ru/en) avtomatik moslashadi.
 */
val Countries: List<Country> = listOf(
    Country("UZ", Res.string.country_uz, "+998", "🇺🇿", "## ### ## ##",
        knownPrefixes = setOf("33", "50", "55", "77", "88", "90", "91", "93", "94", "95", "97", "98", "99", "11")),
    Country("RU", Res.string.country_ru, "+7",   "🇷🇺", "### ### ## ##"),
    Country("KZ", Res.string.country_kz, "+7",   "🇰🇿", "### ### ## ##"),
    Country("KG", Res.string.country_kg, "+996", "🇰🇬", "### ### ###"),
    Country("TJ", Res.string.country_tj, "+992", "🇹🇯", "## ### ####"),
    Country("TM", Res.string.country_tm, "+993", "🇹🇲", "## ## ## ##"),
    Country("TR", Res.string.country_tr, "+90",  "🇹🇷", "### ### ## ##"),
    Country("AF", Res.string.country_af, "+93",  "🇦🇫", "## ### ####"),
    Country("AE", Res.string.country_ae, "+971", "🇦🇪", "## ### ####"),
    Country("US", Res.string.country_us, "+1",   "🇺🇸", "### ### ####"),
    Country("GB", Res.string.country_gb, "+44",  "🇬🇧", "#### ######"),
    Country("DE", Res.string.country_de, "+49",  "🇩🇪", "### #######"),
    Country("KR", Res.string.country_kr, "+82",  "🇰🇷", "## #### ####"),
    Country("AZ", Res.string.country_az, "+994", "🇦🇿", "## ### ## ##"),
    Country("UA", Res.string.country_ua, "+380", "🇺🇦", "## ### ## ##"),
    Country("FR", Res.string.country_fr, "+33",  "🇫🇷", "# ## ## ## ##"),
    Country("IT", Res.string.country_it, "+39",  "🇮🇹", "### ### ####"),
    Country("ES", Res.string.country_es, "+34",  "🇪🇸", "### ### ###"),
    Country("NL", Res.string.country_nl, "+31",  "🇳🇱", "# ## ## ## ##"),
    Country("PL", Res.string.country_pl, "+48",  "🇵🇱", "### ### ###"),
    Country("IN", Res.string.country_in, "+91",  "🇮🇳", "##### #####"),
    Country("CN", Res.string.country_cn, "+86",  "🇨🇳", "### #### ####"),
    Country("JP", Res.string.country_jp, "+81",  "🇯🇵", "## #### ####"),
    Country("CA", Res.string.country_ca, "+1",   "🇨🇦", "### ### ####"),
    Country("AU", Res.string.country_au, "+61",  "🇦🇺", "### ### ###"),
)

/** Standart (boshlang'ich) davlat. */
val DefaultCountry: Country = Countries.first()

/** Berilgan ISO kodi bo'yicha davlatni topadi, topilmasa default qaytaradi. */
fun countryOf(iso: String): Country =
    Countries.firstOrNull { it.iso.equals(iso, ignoreCase = true) } ?: DefaultCountry

/**
 * Telefon raqami maydonining boshlang'ich davlati — telefon hududi bo'yicha (masalan, AQShdagi ota-onaga +1),
 * ro'yxatda bo'lmasa ilova tili bo'yicha: rus → Rossiya, o'zbek → O'zbekiston, boshqa → AQSh.
 */
fun initialPhoneCountry(): Country {
    val region = uz.tikoncha_parent.platform.deviceRegionCode()
    Countries.firstOrNull { it.iso.equals(region, ignoreCase = true) }?.let { return it }
    return when (uz.tikoncha_parent.presentation.profile.language.LanguagePrefs.loadOrDefault().languageCode) {
        "uz" -> DefaultCountry
        "ru" -> countryOf("RU")
        else -> countryOf("US")
    }
}
