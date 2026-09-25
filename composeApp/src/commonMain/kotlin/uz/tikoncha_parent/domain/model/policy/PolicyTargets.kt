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
): JavaSerializable {
    val isEmpty: Boolean get() = packages.isEmpty() && categories.isEmpty() && sites.isEmpty() && features.isEmpty() && iosSelectionIds.isEmpty() && packs.isEmpty()
    val serverAppCount: Int get() = packages.size + iosSelectionIds.size
}
