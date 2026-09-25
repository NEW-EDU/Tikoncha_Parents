package uz.tikoncha_parent.presentation.policy.policy_setup

import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.policy.policy_list.PolicyItemUi
import uz.tikoncha_parent.presentation.policy.limit_rule.LimitRuleUi
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedState
import uz.tikoncha_parent.presentation.policy.time_rule.TimeRuleUi

sealed interface PolicySetupEvent {
    data class SavePolicy(val sharedState: PolicySharedState) : PolicySetupEvent
    data class DeletePolicy(val policyId: String) : PolicySetupEvent
    data object ResetResponseState : PolicySetupEvent
}