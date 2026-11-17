package uz.tikoncha_parent.presentation.voice_help

import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs

/**
 * Ovozli tuşuntiriş bor ekranlar. [id] — serverdagi fayl nomi: `<VOICE_BASE_URL>/<til>/<id>.mp3`.
 * Ovoz ilovaga qöşilmaydi (hajm) — serverdan oqim bilan olinadi.
 */
enum class VoiceScreen(val id: String) {
    HOME("home"),
    PROTECTION("protection"),
    RULES("rules"),
    TASKS("tasks"),
    TASK_CREATE("task_create"),
    MAP("map"),
    CHAT("chat"),
    STATS("stats"),
    ADD_CHILD("add_child"),
}

const val VOICE_BASE_URL = "https://api.tikoncha.uz/static/voice/v1"

/**
 * Ilova tili → ovoz tili (serverda: uz, ru, en, kk, ky, tg, az). Qoraqalpoq va turkman → özbek,
 * MDH'ning boşqa tillari → rus, qolganlari → ingliz.
 */
internal fun voiceLang(appLanguage: String): String = when (appLanguage) {
    "uz", "uz-Cyrl", "kaa", "tk" -> "uz"
    "kk" -> "kk"
    "ky" -> "ky"
    "tg" -> "tg"
    "az" -> "az"
    "ru", "uk", "mn", "hy", "ka" -> "ru"
    else -> "en"
}

/** Ekran tuşuntirişining manzili — ilovaning hozirgi tilida. */
fun voiceUrl(
    screen: VoiceScreen,
    appLanguage: String = LanguagePrefs.loadOrDefault().languageCode,
): String = "$VOICE_BASE_URL/${voiceLang(appLanguage)}/${screen.id}.mp3"
