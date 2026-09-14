package uz.tikoncha_parent.presentation.policy.policy_list

import uz.tikoncha_parent.domain.model.UserInfo

sealed class PolicyEvent {
    data object RefreshPolicies : PolicyEvent()
    data class OnChildSelected(val child: UserInfo) : PolicyEvent()
    data object GetChildren : PolicyEvent()
    data class OnTypeSelected(val index: Int) : PolicyEvent()

    data class TogglePolicy(val policyId: String, val enabled: Boolean) : PolicyEvent()
    /** `null` — menyuni yopish. */
    data class OpenPauseSheet(val policyId: String?) : PolicyEvent()
    data class PausePolicy(val policyId: String, val option: PauseOption) : PolicyEvent()
    data class ResumePolicy(val policyId: String) : PolicyEvent()
    data class RemoveQuickBlock(val packageName: String) : PolicyEvent()
    /** Holat yorliqlarini qayta hisoblash (pauza tugaganini ushlash uchun). */
    data object Tick : PolicyEvent()
}