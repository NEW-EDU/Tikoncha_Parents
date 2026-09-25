package uz.tikoncha_parent.presentation.policy.protection

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.ProtectionPackStatus

/**
 * Kontent himoya ichki ekrani (Student `ContentProtectionState` bilan bir xil): bitta umumiy
 * switch — ota-onaning O'Z himoyasi; ichida nimalar himoya qilinishi — faqat ma'lumot.
 * Farzand yoki ikkinchi ota-ona yoqqan paketlar alohida belgilanadi (ularni men o'chira olmayman).
 */
data class ContentProtectionState(
    val childId: String = "",
    val loaded: Boolean = false,
    val packs: List<ProtectionPackStatus> = emptyList(),
    /** Bola obunasi pullikmi; null — noma'lum (server hal qiladi). */
    val paid: Boolean? = null,
    /** Tavsifi ochilgan paket kodlari. */
    val expanded: Set<String> = emptySet(),
    val busy: Boolean = false,
    val showPayWall: Boolean = false,
    val error: Outcome.Failure? = null,
) {
    val isEnabled: Boolean get() = packs.any { it.enabledByMe }
    val byOthers: Boolean get() = packs.any { it.enabledByCoParent || it.enabledByChild }
    val isAvailable: Boolean get() = packs.isNotEmpty()
}

sealed interface ContentProtectionEvent {
    data class Init(val childId: String) : ContentProtectionEvent
    data class Toggled(val enabled: Boolean) : ContentProtectionEvent
    data class PackClicked(val code: String) : ContentProtectionEvent
    data object PayWallDismissed : ContentProtectionEvent
    data object ErrorDismissed : ContentProtectionEvent
}
