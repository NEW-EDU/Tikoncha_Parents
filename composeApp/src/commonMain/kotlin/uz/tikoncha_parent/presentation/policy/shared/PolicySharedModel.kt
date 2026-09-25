package uz.tikoncha_parent.presentation.policy.shared

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uz.tikoncha_parent.domain.model.policy.PolicyAction
import uz.tikoncha_parent.presentation.policy.app_site_selection.AppFeatures
import uz.tikoncha_parent.presentation.policy.limit_rule.LimitRuleUi
import uz.tikoncha_parent.presentation.policy.time_rule.TimeRuleUi

class PolicySharedModel() : ViewModel() {

    private val _state = MutableStateFlow(PolicySharedState())
    val state = _state.asStateFlow()

    fun onEvent(event: PolicySharedEvent) {
        when (event) {
            // ── Qoidalar ──────────────────────
            is PolicySharedEvent.SetTimeRule ->
                _state.update { it.copy(timeList = event.list) }
            is PolicySharedEvent.SetLimitRule ->
                _state.update { it.copy(limitList = event.list) }
            is PolicySharedEvent.SetLocationRule ->
                _state.update { it.copy(locationRule = event.locationRule) }

            // ── Ilovalar ──────────────────────
            is PolicySharedEvent.ToggleApp -> toggleApp(event.packageName, event.category)
            is PolicySharedEvent.SetSelectedPkgs ->
                _state.update { it.copy(selectedPkgs = event.pkgs) }

            // ── Kategoriyalar ─────────────────
            // MUHIM: event.appPackages ham o'tkaziladi
            is PolicySharedEvent.ToggleCategory ->
                toggleCategory(event.categoryName, event.appPackages)
            is PolicySharedEvent.SetSelectedCategories ->
                _state.update { it.copy(selectedCategories = event.categories) }

            // ── Saytlar ───────────────────────
            is PolicySharedEvent.ToggleSite -> toggleSite(event.url)
            is PolicySharedEvent.SetSelectedSites ->
                _state.update { it.copy(selectedSites = event.sites) }

            is PolicySharedEvent.ToggleFeature -> toggleFeature(event.key)
            is PolicySharedEvent.SetSelectedFeatures ->
                _state.update { it.copy(selectedFeatures = event.features) }

            PolicySharedEvent.DismissFeatureLimitDialog ->
                _state.update { it.copy(showFeatureLimitDialog = false) }

            is PolicySharedEvent.SetAppWebTabIndex ->
                _state.update { it.copy(appWebTabIndex = event.index) }

            PolicySharedEvent.DismissAppLimitDialog ->
                _state.update { it.copy(showAppLimitDialog = false) }
            PolicySharedEvent.DismissSiteLimitDialog ->
                _state.update { it.copy(showSiteLimitDialog = false) }
            PolicySharedEvent.DismissCategoryLimitDialog ->
                _state.update { it.copy(showCategoryLimitDialog = false) }

            is PolicySharedEvent.SetPolicy -> {
                val policy = event.policyItemUi
                _state.update {
                    it.copy(
                        timeList = policy.timeRule,
                        limitList = policy.limitRule,
                        locationRule = policy.locationRule,
                        selectedPkgs = policy.targets.packages.toSet(),
                        selectedCategories = policy.targets.categories.toSet(),
                        selectedSites = policy.targets.sites.toSet(),
                        selectedFeatures = policy.targets.features.toSet(),
                        policyTitle = policy.policyName,
                        policyAction = policy.action,
                        selectedPolicy = policy,
                        canUpdate = policy.canEdit,
                        // ── passthrough ──
                        preset = policy.preset,
                        isActive = policy.isActive,
                        pausedUntil = policy.pausedUntil,
                        expiresAt = policy.expiresAt,
                        expiryOption = null,
                        wifiList = policy.wifiRule,
                        extraLocations = policy.extraLocations,
                        launchLimits = policy.launchLimits,
                        iosSelectionIds = policy.targets.iosSelectionIds,
                        packs = policy.targets.packs,
                    )
                }
                _state.update { s -> s.copy(initialDraftSnapshot = s.toSnapshot()) }
            }

            is PolicySharedEvent.SetPolicyTitle ->
                _state.update { it.copy(policyTitle = event.title) }
            is PolicySharedEvent.SetPolicyAction ->
                _state.update {
                    // Muddat faqat qora ro'yxat uchun (§5.7) — oq ro'yxatga O'TGANDA olib tashlanadi.
                    if (event.action == PolicyAction.ALLOW && it.policyAction != PolicyAction.ALLOW) {
                        it.copy(policyAction = event.action, expiryOption = null, expiresAt = null)
                    } else {
                        it.copy(policyAction = event.action)
                    }
                }

            is PolicySharedEvent.SetExpiryOption ->
                // Yangi variant eski aniq vaqt o'rnini egallaydi; null — muddatsiz.
                _state.update { it.copy(expiryOption = event.option, expiresAt = null) }
            is PolicySharedEvent.SetSubscriptionLimit ->
                _state.update { it.copy(subscriptionLimitEntity = event.limit) }

            PolicySharedEvent.LockInitialSnapshot -> {
                _state.update { s ->
                    if (s.canUpdateInitialDraftSnapshot) {
                        s.copy(
                            initialDraftSnapshot = s.toSnapshot(),
                            canUpdateInitialDraftSnapshot = false,
                        )
                    } else s
                }
            }

            PolicySharedEvent.ClearData ->
                _state.value = PolicySharedState()

            is PolicySharedEvent.SetSelectedChild ->
                _state.update { it.copy(selectedChild = event.child) }

            is PolicySharedEvent.UpsertTimeRule -> upsertTimeRule(event.rule)
            is PolicySharedEvent.RemoveTimeRule -> removeTimeRule(event.id)
            is PolicySharedEvent.UpsertLimitRule -> upsertLimitRule(event.rule)
            is PolicySharedEvent.RemoveLimitRule -> removeLimitRule(event.id)
        }
    }

    // ═════════════════════════════════════════
    // Toggle helpers
    // ═════════════════════════════════════════

    private fun toggleApp(packageName: String, category: String?) {
        val s = _state.value

        // Covered bo'lsa hech narsa qilmaymiz (UI toast ko'rsatadi)
        if (s.isAppCoveredByCategory(category)) return

        val existing = s.selectedPkgs.firstOrNull { it.equals(packageName, ignoreCase = true) }
        if (existing != null) {
            _state.update { it.copy(selectedPkgs = it.selectedPkgs - existing) }
        } else {
            if (!s.canAddApp) {
                _state.update { it.copy(showAppLimitDialog = true) }
                return
            }

            val featuresToAutoAdd: List<String> = if (s.canSelectFeature) {
                val maxApps = s.subscriptionLimitEntity?.appCount ?: Int.MAX_VALUE
                val slotsLeft = (maxApps - (s.selectedAppCount + 1)).coerceAtLeast(0)
                AppFeatures.featuresFor(packageName)
                    .filter { feat ->
                        s.selectedFeatures.none { it.equals(feat.key, ignoreCase = true) }
                    }
                    .take(slotsLeft)
                    .map { it.key }
            } else emptyList()

            _state.update {
                it.copy(
                    selectedPkgs = it.selectedPkgs + packageName,
                    selectedFeatures = it.selectedFeatures + featuresToAutoAdd,
                )
            }
        }
    }

    /**
     * Kategoriya toggle:
     *  - ON bo'lganda: shu kategoriyadagi individual selectedPkgs va selectedFeatures'larni tozalaydi.
     *  - OFF bo'lganda: faqat selectedCategories dan olib tashlaydi.
     */
    private fun toggleCategory(categoryName: String, appPackages: List<String>) {
        val s = _state.value
        val existing = s.selectedCategories.firstOrNull {
            it.equals(categoryName, ignoreCase = true)
        }

        if (existing != null) {
            // OFF
            _state.update { it.copy(selectedCategories = it.selectedCategories - existing) }
        } else {
            // ON
            if (!s.canSelectCategory) {
                _state.update { it.copy(showCategoryLimitDialog = true) }
                return
            }

            // 1) Shu kategoriya app'lariga tegishli individual selectedPkgs
            val pkgsToRemove = s.selectedPkgs.filter { pkg ->
                appPackages.any { it.equals(pkg, ignoreCase = true) }
            }.toSet()

            // 2) Shu kategoriya app'lariga tegishli individual selectedFeatures
            val featuresToRemove = s.selectedFeatures.filter { featureKey ->
                val parentPkg = AppFeatures.parentPackageOf(featureKey)
                parentPkg != null && appPackages.any { it.equals(parentPkg, ignoreCase = true) }
            }.toSet()

            _state.update {
                it.copy(
                    selectedCategories = it.selectedCategories + categoryName,
                    selectedPkgs = it.selectedPkgs - pkgsToRemove,
                    selectedFeatures = it.selectedFeatures - featuresToRemove,
                )
            }
        }
    }

    private fun toggleSite(url: String) {
        val s = _state.value
        val normalized = url.trim()
        val existing = s.selectedSites.firstOrNull { it.equals(normalized, ignoreCase = true) }

        if (existing != null) {
            _state.update { it.copy(selectedSites = it.selectedSites - existing) }
        } else {
            if (!s.canAddSite) {
                _state.update { it.copy(showSiteLimitDialog = true) }
                return
            }
            _state.update { it.copy(selectedSites = it.selectedSites + normalized) }
        }
    }

    private fun toggleFeature(key: String) {
        val s = _state.value
        val existing = s.selectedFeatures.firstOrNull { it.equals(key, ignoreCase = true) }

        if (existing != null) {
            _state.update { it.copy(selectedFeatures = it.selectedFeatures - existing) }
        } else {
            if (!s.canSelectFeature) {
                _state.update { it.copy(showFeatureLimitDialog = true) }
                return
            }
            if (!s.canAddApp) {
                _state.update { it.copy(showAppLimitDialog = true) }
                return
            }
            _state.update { it.copy(selectedFeatures = it.selectedFeatures + key) }
        }
    }

    // ═════════════════════════════════════════
    // Rule upsert/remove
    // ═════════════════════════════════════════

    private fun upsertTimeRule(rule: TimeRuleUi) {
        _state.update { s ->
            if (rule.id != 0) {
                val idx = s.timeList.indexOfFirst { it.id == rule.id }
                if (idx >= 0) {
                    val newList = s.timeList.toMutableList().apply { this[idx] = rule }
                    return@update s.copy(timeList = newList)
                }
            }
            val newId = (s.timeList.maxOfOrNull { it.id } ?: 0) + 1
            s.copy(timeList = s.timeList + rule.copy(id = newId))
        }
    }

    private fun removeTimeRule(id: Int) {
        _state.update { s -> s.copy(timeList = s.timeList.filterNot { it.id == id }) }
    }

    private fun upsertLimitRule(rule: LimitRuleUi) {
        _state.update { s ->
            if (rule.id != 0) {
                val idx = s.limitList.indexOfFirst { it.id == rule.id }
                if (idx >= 0) {
                    val newList = s.limitList.toMutableList().apply { this[idx] = rule }
                    return@update s.copy(limitList = newList)
                }
            }
            val newId = (s.limitList.maxOfOrNull { it.id } ?: 0) + 1
            s.copy(limitList = s.limitList + rule.copy(id = newId))
        }
    }

    private fun removeLimitRule(id: Int) {
        _state.update { s -> s.copy(limitList = s.limitList.filterNot { it.id == id }) }
    }
}