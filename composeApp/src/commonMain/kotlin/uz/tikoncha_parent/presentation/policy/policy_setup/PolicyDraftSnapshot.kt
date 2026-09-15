package uz.tikoncha_parent.presentation.policy.policy_setup

import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.presentation.policy.limit_rule.LimitRuleUi
import uz.tikoncha_parent.presentation.policy.time_rule.TimeRuleUi
import kotlin.time.Instant

data class PolicyDraftSnapshot(
    val title: String,
    val action: PolicyAction,
    val timeList: List<TimeRuleUi>,
    val limitList: List<LimitRuleUi>,
    val locationRule: LocationRule?,
    val packages: List<String>,
    val categories: List<String>,
    val sites: List<String>,
    val features: List<String>,
    val expiresAt: Instant?,
    val expiryOption: ExpiryOption?,
)