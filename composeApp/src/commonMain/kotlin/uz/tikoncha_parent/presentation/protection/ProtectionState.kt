package uz.tikoncha_parent.presentation.protection

import kotlinx.datetime.Instant
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.protection.AccountRequest
import uz.tikoncha_parent.domain.model.protection.ChildMode
import uz.tikoncha_parent.domain.model.protection.ChildPermission
import uz.tikoncha_parent.domain.model.protection.ChildRequest
import uz.tikoncha_parent.domain.model.protection.StrictMethod
import uz.tikoncha_parent.presentation.ui_state.ResponseState

data class ProtectionState(
    val responseState: ResponseState<Any> = ResponseState.Idle,
    val isRefreshing: Boolean = false,

    // hero karta
    val mode: ChildMode = ChildMode.UNKNOWN,
    val strictMethod: StrictMethod? = null,
    val unlockData: String? = null,
    val isCodeVisible: Boolean = false,
    val lastSyncAt: Instant? = null,
    val isOnline: Boolean = false,
    /** null — farzand ilovasi bu ma'lumotni yubormaydi (eski versiya) */
    val policiesUpToDate: Boolean? = null,
    val policiesSyncedAt: Instant? = null,

    // ruxsatlar
    val enabledPermissions: Set<ChildPermission> = emptySet(),
    val disabledPermissions: Set<ChildPermission> = emptySet(),

    // farzand so'rovlari
    val strictDisableRequest: ChildRequest? = null,
    val logoutRequest: AccountRequest? = null,
    val deleteRequest: AccountRequest? = null,
    // remainingSeconds ← O'CHIRILDI (endi alohida flow)
    val actionInProgressId: String? = null,
    val actionResponseState: ResponseState<Any> = ResponseState.Idle,

    val childrenList: List<UserInfo> = emptyList(),
    val selectedChild: UserInfo? = null,
    val childrenResponseState: ResponseState<Nothing> = ResponseState.Idle,
) {
    private val reportedPermissions: Set<ChildPermission>
        get() = enabledPermissions + disabledPermissions

    val requiredPermissions: List<ChildPermission>
        get() = if (mode == ChildMode.UNKNOWN) emptyList()
        else ChildPermission.entries.filter {
            it.minMode.ordinal <= mode.ordinal && it in reportedPermissions
        }

    val higherTierPermissions: List<ChildPermission>
        get() = ChildPermission.entries.filter {
            (mode == ChildMode.UNKNOWN || it.minMode.ordinal > mode.ordinal) &&
                    it in reportedPermissions
        }

    val missingRequiredPermissions: List<ChildPermission>
        get() = requiredPermissions.filter { it in disabledPermissions }

    val grantedRequiredCount: Int
        get() = requiredPermissions.count { it in enabledPermissions }

    val isStrictRequestPending: Boolean
        get() = strictDisableRequest?.status.equals("pending", ignoreCase = true)

    val isLogoutRequestPending: Boolean
        get() = logoutRequest?.status.equals("process", ignoreCase = true)

    val isDeleteRequestPending: Boolean
        get() = deleteRequest?.status.equals("process", ignoreCase = true)

    val pendingRequestCount: Int
        get() = listOf(isStrictRequestPending, isLogoutRequestPending, isDeleteRequestPending)
            .count { it }

    val showRequestsCard: Boolean
        get() = strictDisableRequest != null || logoutRequest != null || deleteRequest != null
}