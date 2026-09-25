package uz.tikoncha_parent.presentation.policy.policy_list

import cafe.adriel.voyager.core.lifecycle.JavaSerializable
import kotlinx.serialization.Serializable
import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.policy.LaunchLimit
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyEffectiveState
import uz.tikoncha_parent.domain.model.policy.PolicyKind
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.model.policy.WifiCondition
import uz.tikoncha_parent.presentation.policy.limit_rule.LimitRuleUi
import uz.tikoncha_parent.presentation.policy.time_rule.TimeRuleUi
import kotlin.time.Instant

@Serializable
data class PolicyItemUi(
    val policyId: String,
    val policyName: String,
    val action: PolicyAction,
    val kind: PolicyKind,
    val preset: PolicyPreset?,
    val packCode: String?,
    val policyType: PolicyType,
    val actorUserId: String?,
    /** Men yaratganmi (PARENT_CHILD va actorUserId == userId). */
    val isMine: Boolean,
    val canEdit: Boolean,
    /** Server `is_active` — tugma holati. */
    val isActive: Boolean,
    val effectiveState: PolicyEffectiveState,
    val pausedUntil: Instant?,
    val expiresAt: Instant?,
    val targets: PolicyTargets,
    val timeRule: List<TimeRuleUi>,
    val limitRule: List<LimitRuleUi>,
    val locationRule: LocationRule?,
    /** 2+ lokatsiya — UI ko'rsatmaydi, tahrirda qaytarib yuboriladi. */
    val extraLocations: List<LocationRule> = emptyList(),
    val wifiRule: List<WifiCondition> = emptyList(),
    val launchLimits: List<LaunchLimit> = emptyList(),
) : JavaSerializable {

    val hasTimeRule: Boolean get() = timeRule.isNotEmpty()
    val hasLimitRule: Boolean get() = limitRule.isNotEmpty()
    val hasLocationRule: Boolean get() = locationRule != null

    val appCount: Int get() = targets.packages.size + targets.features.size
    val webCount: Int get() = targets.sites.size
    val categoryCount: Int get() = targets.categories.size
    val featureCount: Int get() = targets.features.size
    val iosCount: Int get() = targets.iosSelectionIds.size
    val packCount: Int get() = targets.packs.size

    /** Shartsiz — doimiy amal qiladi. */
    val isAlways: Boolean
        get() = timeRule.isEmpty() && limitRule.isEmpty() && locationRule == null && wifiRule.isEmpty()
}