package uz.tikoncha_parent.domain.model.policy

import kotlin.time.Instant

/** Tayyor himoya paketi (qimor ilovalari/saytlari, kattalar saytlari). */
data class ProtectionPack(
    val code: String,
    val name: Map<String, String>,
    val description: Map<String, String>?,
    val packageCount: Int,
    val siteCount: Int,
    val categoryCount: Int,
    val featureCount: Int,
    val isActive: Boolean,
    val version: Int,
) {
    fun title(lang: String): String = name[lang] ?: name["uz"] ?: code
    fun desc(lang: String): String? = description?.get(lang) ?: description?.get("uz")
}

/** `GET /v2/packs` — katalog. */
data class PackCatalog(
    val packs: List<ProtectionPack>,
    val version: Int,
    val asOf: Instant,
    /** META kategoriya (GAMES, SOCIAL…) → Play Store leaf'lari. */
    val categoriesMap: Map<String, List<String>>,
)

/** Bitta paketning shu bola uchun holati — katalog + PROTECTION jadvallaridan yig'iladi. */
data class ProtectionPackStatus(
    val pack: ProtectionPack,
    val myPolicy: Policy?,
    val enabledByCoParent: Boolean,
    val enabledByChild: Boolean,
) {
    val enabledByMe: Boolean get() = myPolicy?.isActive == true
    val enabledByAnyone: Boolean get() = enabledByMe || enabledByCoParent || enabledByChild
}