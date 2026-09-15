package uz.tikoncha_parent.presentation.policy.shared

import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.presentation.policy.limit_rule.LimitRuleUi
import uz.tikoncha_parent.presentation.policy.policy_list.PolicyItemUi
import uz.tikoncha_parent.presentation.policy.policy_setup.ExpiryOption
import uz.tikoncha_parent.presentation.policy.time_rule.TimeRuleUi

sealed interface PolicySharedEvent {

    data class SetSelectedChild(val child: UserInfo) : PolicySharedEvent

    // ── Qoidalar ─────────────────────────────
    data class SetTimeRule(val list: List<TimeRuleUi>) :
        PolicySharedEvent

    data class SetLimitRule(val list: List<LimitRuleUi>) :
        PolicySharedEvent

    data class SetLocationRule(val locationRule: LocationRule?) :
        PolicySharedEvent

    // ── Ilovalar ─────────────────────────────
    data class ToggleApp(
        val packageName: String,
        val category: String?,
        val categoryAppPackages: List<String> = emptyList(),
    ) : PolicySharedEvent

    data class SetSelectedPkgs(val pkgs: Set<String>) :
        PolicySharedEvent

    // ── Kategoriyalar ────────────────────────
    data class ToggleCategory(
        val categoryName: String,
        val appPackages: List<String>,
    ) : PolicySharedEvent

    data class SetSelectedCategories(val categories: Set<String>) :
        PolicySharedEvent

    // ── Saytlar ──────────────────────────────
    data class ToggleSite(val url: String) :
        PolicySharedEvent

    data class SetSelectedSites(val sites: Set<String>) :
        PolicySharedEvent

    data class ToggleFeature(val key: String) : PolicySharedEvent
    data class SetSelectedFeatures(val features: Set<String>) : PolicySharedEvent

    // ── Tab ──────────────────────────────────
    data class SetAppWebTabIndex(val index: Int) :
        PolicySharedEvent

    // ── Limit dialog ─────────────────────────
    data object DismissAppLimitDialog :
        PolicySharedEvent

    data object DismissSiteLimitDialog :
        PolicySharedEvent

    data object DismissCategoryLimitDialog :
        PolicySharedEvent

    // ── Policy asosiy ────────────────────────
    data class SetPolicy(val policyItemUi: PolicyItemUi) :
        PolicySharedEvent

    data class SetPolicyTitle(val title: String) :
        PolicySharedEvent

    data class SetPolicyAction(val action: PolicyAction) :
        PolicySharedEvent

    /** `null` — muddatni olib tashlash ("Muddatsiz"). */
    data class SetExpiryOption(val option: ExpiryOption?) :
        PolicySharedEvent

    data class SetSubscriptionLimit(val limit: SubscriptionLimit?) :
        PolicySharedEvent

    // ── Snapshot ─────────────────────────────
    data object LockInitialSnapshot :
        PolicySharedEvent

    // ── Tozalash ─────────────────────────────
    data object ClearData :
        PolicySharedEvent

    data class UpsertTimeRule(val rule: TimeRuleUi) : PolicySharedEvent
    data class RemoveTimeRule(val id: Int) : PolicySharedEvent

    // ── Limit rule granular ──────────────────
    data class UpsertLimitRule(val rule: LimitRuleUi) : PolicySharedEvent
    data class RemoveLimitRule(val id: Int) : PolicySharedEvent

    data object DismissFeatureLimitDialog : PolicySharedEvent


}