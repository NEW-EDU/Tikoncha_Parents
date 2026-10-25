package uz.tikoncha_parent.presentation.profile.language

import com.russhwolf.settings.Settings
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.platform.deviceLanguageTags
import com.russhwolf.settings.set
import uz.tikoncha_parent.presentation.domain.model.LanguageType

object LanguagePrefs {

    private const val KEY = "app_language"
    private val settings: Settings = Settings() // no-arg: Android=SharedPrefs, iOS=UserDefaults

    // Faqat kodni saqlash/olish:
    private fun saveCode(code: String) { settings[KEY] = code }
    private fun loadCodeOrNull(): String? = settings.getStringOrNull(KEY)
    fun clear() { settings.remove(KEY) }

    // Qulaylik uchun LanguageType bilan ishlaydigan helperlar:
    fun save(type: LanguageType) = saveCode(type.languageCode)
    fun loadOrDefault(default: LanguageType = LanguageType.UZ): LanguageType {
        loadCodeOrNull()?.let { return LanguageType.getLangType(it) }
        // Til hali tanlanmagan. Avval kirgan foydalanuvchida avvalgidek o'zbek. Yangi o'rnatishda — telefon tili:
        // o'zbek yoki rus, boshqa tillarda ingliz (global brend Hoglt). Bir marta saqlanadi.
        if (AppSettings.hasUserLogin) return default
        val device = LanguageType.bestMatch(deviceLanguageTags())
        save(device)
        return device
    }


}