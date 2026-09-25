package uz.tikoncha_parent.presentation.policy.list

import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.policy.model.ContentProtectionUi
import uz.tikoncha_parent.presentation.policy.model.PayWallReason
import uz.tikoncha_parent.presentation.policy.model.PolicyCardUi
import uz.tikoncha_parent.presentation.policy.model.PolicyListInfo
import uz.tikoncha_parent.presentation.policy.model.PolicyTab
import uz.tikoncha_parent.presentation.policy.model.PresetKind
import uz.tikoncha_parent.presentation.policy.model.PresetPolicyUi
import uz.tikoncha_parent.presentation.policy.model.QuickBlockUi

/**
 * Faqat ma'lumot. Hisoblangan qiymatlar (`tabs`, `enabledCount`, `info`) reducer'da
 * bir marta chiqadi — ekran ularni bir rekompozitsiyada bir necha marta o'qiydi.
 */
data class PolicyListState(
    /* ----- Farzand ----- */
    val children: List<UserInfo> = emptyList(),
    val selectedChild: UserInfo? = null,

    /* ----- Ro'yxat ----- */
    val tab: PolicyTab = PolicyTab.TEMPLATES,
    val tabs: List<PolicyTab> = listOf(PolicyTab.TEMPLATES, PolicyTab.MINE),
    val presets: List<PresetPolicyUi> = PresetKind.entries.map { PresetPolicyUi(it) },
    val protection: ContentProtectionUi = ContentProtectionUi(),
    val quickBlock: QuickBlockUi = QuickBlockUi(),
    val mine: List<PolicyCardUi> = emptyList(),
    val child: List<PolicyCardUi> = emptyList(),
    val childQuick: List<PolicyCardUi> = emptyList(),
    val coParent: List<PolicyCardUi> = emptyList(),
    val coParentQuick: List<PolicyCardUi> = emptyList(),
    val school: List<PolicyCardUi> = emptyList(),
    val info: PolicyListInfo = PolicyListInfo(),
    val enabledCount: Int = 0,
    /** Bolaning tarifi; null — noma'lum (server hal qiladi). */
    val paid: Boolean? = null,

    /* ----- So'rovlar ----- */
    val loaded: Boolean = false,
    val isRefreshing: Boolean = false,
    /** So'rovi qaytmagan nishonlar: policyId, "preset:SLEEP", "protection", "quick". */
    val busy: Set<String> = emptySet(),
    val payWall: PayWallReason? = null,
    val error: Outcome.Failure? = null,
) {
    fun isBusy(key: String): Boolean = key in busy
}

sealed interface PolicyListEvent {
    data object Load : PolicyListEvent
    data object Refresh : PolicyListEvent
    data object Resumed : PolicyListEvent
    data class ChildSelected(val child: UserInfo) : PolicyListEvent
    data class TabSelected(val tab: PolicyTab) : PolicyListEvent

    /** Tayyor jadval switch'i: bazada yo'q bo'lsa yaratadi, bor bo'lsa is_active. */
    data class PresetToggled(val kind: PresetKind, val enabled: Boolean, val title: String) : PolicyListEvent
    data class PresetClicked(val kind: PresetKind) : PolicyListEvent

    /** "Tezkor blok" shabloni: yoqish/o'chirish (ro'yxat bo'sh bo'lsa — ekran ochiladi). */
    data class QuickBlockToggled(val enabled: Boolean) : PolicyListEvent
    data object QuickBlockClicked : PolicyListEvent

    data class ProtectionToggled(val enabled: Boolean) : PolicyListEvent
    data object ProtectionClicked : PolicyListEvent

    data class PolicyToggled(val policyId: String, val enabled: Boolean) : PolicyListEvent
    data class PolicyClicked(val policyId: String) : PolicyListEvent

    data object CreateClicked : PolicyListEvent
    data object PayWallDismissed : PolicyListEvent
    data object ErrorDismissed : PolicyListEvent
}

sealed interface PolicyListEffect {
    data class OpenPreset(val childId: String, val kind: PresetKind) : PolicyListEffect
    data class OpenProtection(val childId: String) : PolicyListEffect
    data class OpenQuickBlock(val childId: String) : PolicyListEffect
    data class OpenPolicy(val childId: String, val policyId: String) : PolicyListEffect
    data class OpenCreate(val childId: String) : PolicyListEffect
}
