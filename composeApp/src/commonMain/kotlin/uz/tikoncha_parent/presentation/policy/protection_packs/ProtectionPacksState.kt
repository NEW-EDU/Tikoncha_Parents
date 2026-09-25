package uz.tikoncha_parent.presentation.policy.protection_packs

import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.ui_state.ResponseState

data class ProtectionPacksState(
    val responseState: ResponseState<Nothing> = ResponseState.Idle,
    val isRefreshing: Boolean = false,
    val selectedChild: UserInfo? = null,
    val childrenList: List<UserInfo> = emptyList(),
    val packs: List<ProtectionPackUi> = emptyList(),
    val premiumFailure: Outcome.Failure? = null,
    val isPaid: Boolean = false,
)