package uz.tikoncha_parent.presentation.tutorial_clips

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.bajarilgan_vazifalar
import tikoncha_parents.composeapp.generated.resources.cheklovlar
import tikoncha_parents.composeapp.generated.resources.farzand_himoyasi
import tikoncha_parents.composeapp.generated.resources.farzand_qo_shish
import tikoncha_parents.composeapp.generated.resources.statistika
import tikoncha_parents.composeapp.generated.resources.suhbat
import tikoncha_parents.composeapp.generated.resources.topshiriqlar
import tikoncha_parents.composeapp.generated.resources.ulash_usuli_kod
import tikoncha_parents.composeapp.generated.resources.vazifa_qo_shish
import uz.tikoncha_parent.presentation.profile.language.LanguagePrefs

/**
 * Ilovaga qo'shilgan qisqa qo'llanma kliplari (haqiqiy ekranlar + qayerga bosish), `files/tutorials/<file>-<til>.mp4`.
 * Har klip uçun mavjud tillar: uz (lotin), uzc (kirill), ru. Boşqa tillarda eng yaqini körsatiladi — ekrandagi
 * matn boşqa tilda bölsa ham, qayerga bosiş körinib turadi.
 */
enum class TutorialClip(val file: String, val title: StringResource, val langs: Set<String>) {
    ADD_CHILD("add-child", Res.string.farzand_qo_shish, setOf("uzc")),
    ADD_CHILD_CODE("add-child-code", Res.string.ulash_usuli_kod, setOf("uzc")),
    GIVE_TASK("give-task", Res.string.vazifa_qo_shish, setOf("uz", "uzc", "ru")),
    CHECK_TASK("check-task", Res.string.bajarilgan_vazifalar, setOf("uzc")),
    TASKS("tasks", Res.string.topshiriqlar, setOf("uz", "uzc", "ru")),
    CHAT("chat", Res.string.suhbat, setOf("uz", "uzc", "ru")),
    PROTECTION("protection", Res.string.farzand_himoyasi, setOf("uz", "uzc", "ru")),
    LIMITS("limits", Res.string.cheklovlar, setOf("uz", "uzc", "ru")),
    SCREEN_TIME("screentime", Res.string.statistika, setOf("uz", "uzc", "ru"));

    /** Ilova tiliga eng mos klip fayli (`files/tutorials/...`). */
    fun path(appLanguage: String = LanguagePrefs.loadOrDefault().languageCode): String =
        "files/tutorials/$file-${pickLang(appLanguage, langs)}.mp4"

    @OptIn(ExperimentalResourceApi::class)
    fun uri(): String = Res.getUri(path())

    companion object {
        /** Qo'llanmalar ro'yxati tartibi: avval boşlaş, keyin har kungi işlar. */
        val all: List<TutorialClip> = entries
    }
}

/** Ilova tili → klip tili: aniq mos, keyin yaqin til (qoraqalpoq → özbek, MDH kirill tillari → rus), keyin bor bölgani. */
internal fun pickLang(appLanguage: String, available: Set<String>): String {
    val wanted = when (appLanguage) {
        "uz" -> listOf("uz", "uzc", "ru")
        "uz-Cyrl" -> listOf("uzc", "uz", "ru")
        "kaa", "tk" -> listOf("uz", "uzc", "ru")
        "ru", "kk", "ky", "tg", "uk", "mn", "hy", "ka", "az" -> listOf("ru", "uzc", "uz")
        else -> listOf("ru", "uz", "uzc")
    }
    return wanted.firstOrNull { it in available } ?: available.first()
}
