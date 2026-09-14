package uz.tikoncha_parent.presentation.policy.policy_list

import kotlin.time.Clock
import kotlin.time.Instant
import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.permission_status.PermissionIssue
import uz.tikoncha_parent.domain.model.policy.PolicyKind
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.presentation.ui_state.ResponseState

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
}