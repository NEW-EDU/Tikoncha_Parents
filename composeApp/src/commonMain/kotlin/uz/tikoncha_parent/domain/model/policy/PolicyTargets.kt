package uz.tikoncha_parent.domain.model.policy

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable

@Serializable
data class PolicyTargets(
    val packages: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val sites: List<String> = emptyList(),
    val features: List<String> = emptyList(),
    val iosSelectionIds: List<String> = emptyList(),
    val packs: List<String> = emptyList(),
    /** Faqat DENY: `"*"` yoki kategoriyadan istisno ("ochiq qoladi" / "hisoblanmaydi"). */
    val excludePackages: List<String> = emptyList(),
): JavaSerializable {
    /** Istisnolar nishon emas — faqat istisnodan iborat jadval bo'sh hisoblanadi (server ham shunday). */
    val isEmpty: Boolean get() = packages.isEmpty() && categories.isEmpty() && sites.isEmpty() && features.isEmpty() && iosSelectionIds.isEmpty() && packs.isEmpty()
    val serverAppCount: Int get() = packages.size + iosSelectionIds.size

    /** `packages = ["*"]` — barcha ilovalar (Uyqu vaqti, Dars vaqti). */
    val isAllApps: Boolean get() = ALL_APPS in packages

    companion object {
        const val ALL_APPS = "*"
    }
}
