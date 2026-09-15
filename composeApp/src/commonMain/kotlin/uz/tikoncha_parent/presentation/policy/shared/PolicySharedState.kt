package uz.tikoncha_parent.presentation.policy.shared

import uz.tikoncha_parent.domain.model.LocationRule
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.SubscriptionType
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.policy.LaunchLimit
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.model.policy.WifiCondition
import uz.tikoncha_parent.presentation.policy.limit_rule.LimitRuleUi
import uz.tikoncha_parent.presentation.policy.policy_list.PolicyItemUi
import uz.tikoncha_parent.presentation.policy.policy_setup.ExpiryOption
import uz.tikoncha_parent.presentation.policy.policy_setup.PolicyDraftSnapshot
import uz.tikoncha_parent.presentation.policy.time_rule.TimeRuleUi
import kotlin.time.Instant

data class PolicySharedState(
    val selectedChild: UserInfo? = null,

    val timeList: List<TimeRuleUi> = emptyList(),
    val limitList: List<LimitRuleUi> = emptyList(),
    val locationRule: LocationRule? = null,

    val selectedPkgs: Set<String> = emptySet(),
    val selectedCategories: Set<String> = emptySet(),
    val selectedSites: Set<String> = emptySet(),
    val selectedFeatures: Set<String> = emptySet(),

    val appWebTabIndex: Int = 0,

    val policyTitle: String = "",
    val policyAction: PolicyAction = PolicyAction.DENY,
    val selectedPolicy: PolicyItemUi? = null,

    val canUpdate: Boolean = true,
    val subscriptionLimitEntity: SubscriptionLimit? = null,
    val showAppLimitDialog: Boolean = false,
    val showSiteLimitDialog: Boolean = false,
    val showCategoryLimitDialog: Boolean = false,
    val showFeatureLimitDialog: Boolean = false,

    val initialDraftSnapshot: PolicyDraftSnapshot? = null,
    val canUpdateInitialDraftSnapshot: Boolean = true,


    // ── v2: tahrirda yo'qolmasligi kerak bo'lgan qismlar ──
    val preset: PolicyPreset? = null,
    val isActive: Boolean = true,
    val pausedUntil: Instant? = null,
    val expiresAt: Instant? = null,
    /** Shu sessiyada tanlangan tayyor variant — aniq vaqt saqlash paytida hisoblanadi. */
    val expiryOption: ExpiryOption? = null,
    /** WiFi ekrani hali "soon" — faqat saqlanadi. */
    val wifiList: List<WifiCondition> = emptyList(),
    /** 2+ lokatsiya — UI bittasini ko'rsatadi, qolgani saqlanadi. */
    val extraLocations: List<LocationRule> = emptyList(),
    val launchLimits: List<LaunchLimit> = emptyList(),
    /** iOS ilova tanlovi — Parents UI'da yo'q. */
    val iosSelectionIds: List<String> = emptyList(),
    val packs: List<String> = emptyList(),
) {
    val isEditable: Boolean
        get() = selectedPolicy == null || selectedPolicy.canEdit

    val isEditMode: Boolean
        get() = selectedPolicy != null

    val selectedAppCount: Int
        get() = selectedPkgs.size + selectedFeatures.size

    val selectedCategoryCount: Int get() = selectedCategories.size
    val selectedSiteCount: Int get() = selectedSites.size
    val selectedFeatureCount: Int get() = selectedFeatures.size

    val showAddRuleButton: Boolean
        get() = (limitList.isEmpty() || timeList.isEmpty() || locationRule == null) && canUpdate

    val canSavePolicy: Boolean
        get() {
            val hasRule = timeList.isNotEmpty() || limitList.isNotEmpty() || locationRule != null
            val hasResource =
                selectedPkgs.isNotEmpty() ||
                        selectedCategories.isNotEmpty() ||
                        selectedSites.isNotEmpty() ||
                        selectedFeatures.isNotEmpty() ||
                        iosSelectionIds.isNotEmpty() ||
                        packs.isNotEmpty()
            return hasRule && hasResource && policyTitle.isNotBlank()
        }

    /** Faqat individual selectedPkgs ni tekshiradi. Coverage uchun [isAppCoveredByCategory] ishlat. */
    fun isAppSelected(pkg: String, category: String?): Boolean {
        if (selectedPkgs.any { it.equals(pkg, ignoreCase = true) }) return true
        if (category != null && selectedCategories.any {
                it.equals(category, ignoreCase = true)
            }) return true
        return false
    }

    /** YANGI: app kategoriya orqali "covered" bo'lganmi? */
    fun isAppCoveredByCategory(category: String?): Boolean {
        if (category == null) return false
        return selectedCategories.any { it.equals(category, ignoreCase = true) }
    }

    fun isCategorySelected(categoryName: String): Boolean =
        selectedCategories.any { it.equals(categoryName, ignoreCase = true) }

    fun isSiteSelected(url: String): Boolean =
        selectedSites.any { it.equals(url, ignoreCase = true) }

    fun isFeatureSelected(key: String): Boolean =
        selectedFeatures.any { it.equals(key, ignoreCase = true) }

    fun toSnapshot(): PolicyDraftSnapshot = PolicyDraftSnapshot(
        title = policyTitle,
        action = policyAction,
        timeList = timeList,
        limitList = limitList,
        locationRule = locationRule,
        packages = selectedPkgs.toList(),
        categories = selectedCategories.toList(),
        sites = selectedSites.toList(),
        features = selectedFeatures.toList(),
        expiresAt = expiresAt,
        expiryOption = expiryOption,
    )

    val hasChanges: Boolean
        get() {
            val initial = initialDraftSnapshot ?: return false
            return initial != toSnapshot()
        }

    val canAddApp: Boolean
        get() = subscriptionLimitEntity?.appCount?.let { selectedAppCount < it } ?: true

    val canAddSite: Boolean
        get() = subscriptionLimitEntity?.appCount?.let { selectedSiteCount < it } ?: true

    val canSelectCategory: Boolean
        get() = subscriptionLimitEntity?.subscriptionType != SubscriptionType.FREE

    val canSelectFeature: Boolean
        get() = subscriptionLimitEntity?.subscriptionType != SubscriptionType.FREE

    val canSaveAppWebSelection: Boolean
        get() = selectedPkgs.isNotEmpty() ||
                selectedSites.isNotEmpty() ||
                selectedCategories.isNotEmpty() ||
                selectedFeatures.isNotEmpty()
}