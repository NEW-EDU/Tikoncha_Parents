package uz.tikoncha_parent.platform

/** Telefonning (tizimning) tili, masalan "uz", "ru", "en", "de". Ilova o'z tilini alohida saqlaydi (LanguagePrefs). */
expect fun deviceLanguageCode(): String

/** Telefon hududi (ISO 3166-1 alpha-2, masalan "UZ", "US"); noma'lum bo'lsa bo'sh. */
expect fun deviceRegionCode(): String
