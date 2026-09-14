package uz.tikoncha_parent.presentation.policy.policy_list

import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.permission_status.PermissionIssue
import uz.tikoncha_parent.domain.model.policy.PolicyKind
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.model.policy.QuickBlockEntry
import uz.tikoncha_parent.presentation.policy.app_site_selection.AppSelectionUi
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import kotlin.time.Clock
import kotlin.time.Instant

data class PolicyState(
    val policyResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val policies: List<PolicyItemUi> = emptyList(),
    val selectedChild: UserInfo? = null,
    val subscriptionLimit: SubscriptionLimit = SubscriptionLimit(),
    val isInitialLoadDone: Boolean = false,
    val permissionIssueList: List<PermissionIssue> = emptyList(),
    val childrenList: List<UserInfo> = emptyList(),
    val childrenResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val selectedTypeIndex: Int = 0,
    val showPolicyTutorialCard: Boolean = false,
    val myUserId: String = "",
    val now: Instant = Clock.System.now(),
    /** Server javobini kutayotgan jadval id lari — tugma bloklanadi. */
    val actionInProgress: Set<String> = emptySet(),
    /** Pauza menyusi ochiq bo'lgan jadval id si. */
    val pauseSheetFor: String? = null,
    val quickBlocks: List<QuickBlockEntry> = emptyList(),
    /** Paket → nom va ikonka (bola qurilmasidagi ilovalar). */
    val childApps: Map<String, AppSelectionUi> = emptyMap(),
) {
    /** Tezkor blok va himoya paketlari "Jadvallar" ro'yxatiga kirmaydi. */
    val standardPolicies: List<PolicyItemUi>
        get() = policies.filter { it.kind == PolicyKind.STANDARD && it.preset != PolicyPreset.PROTECTION }

    /** 0 = Siz, 1 = Farzandingiz, 2 = Maktab / Umumiy. */
    val filteredPolicies: List<PolicyItemUi>
        get() = standardPolicies.filter {
            when (selectedTypeIndex) {
                0 -> it.policyType == PolicyType.PARENT_CHILD
                1 -> it.policyType == PolicyType.STUDENT
                else -> it.policyType == PolicyType.SCHOOL || it.policyType == PolicyType.ALL
            }
        }

    /** Server `count_standard_active` bilan bir xil hisob. */
    val activeStandardCount: Int
        get() = standardPolicies.count {
            it.isActive && (it.policyType == PolicyType.PARENT_CHILD || it.policyType == PolicyType.STUDENT)
        }

    val canCreatePolicy: Boolean get() = activeStandardCount < subscriptionLimit.policyCount

    val hasDeviceIssue: Boolean get() = permissionIssueList.isNotEmpty()
    /** Ilovasi bor tezkor bloklar — siznikilar birinchi. */
    val visibleQuickBlocks: List<QuickBlockEntry>
        get() = quickBlocks
            .filter { it.targets.packages.isNotEmpty() }
            .sortedByDescending { it.isMine(myUserId) }

    /** Yoqilgan himoya paketlari nomlari — "Himoya" qatori ostida ko'rsatiladi. */
    val enabledProtectionNames: List<String>
        get() = policies
            .filter { it.preset == PolicyPreset.PROTECTION && it.isActive }
            .map { it.policyName }
            .distinct()

    fun isQuickBlockBusy(packageName: String): Boolean = quickBlockKey(packageName) in actionInProgress

    companion object {
        /** `actionInProgress` da jadval id lari bilan to'qnashmasligi uchun prefiks. */
        fun quickBlockKey(packageName: String) = "qb:$packageName"
    }
}