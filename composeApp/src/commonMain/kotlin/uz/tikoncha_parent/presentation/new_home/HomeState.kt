package uz.tikoncha_parent.presentation.new_home

import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.ui_state.ResponseState

data class HomeState(
    val childrenResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val isRefreshing: Boolean = false,
    val childrenList: List<UserInfo> = emptyList(),
    val selectedChild: UserInfo? = null,

    val userName: String = "",
    val userImageUrl: String? = null,
    val showTikonchaTutorialCard: Boolean = false,

    val activeTaskCount: Int = 0,
    val parentPolicyCount: Int = 0,

    /* ----- HomeScreen card uchun bugungi usage ----- */
    val todayUsage: HourMinute = HourMinute(0, 0),

    val protectionPendingRequestCount: Int = 0,
    val protectionPermissionOffCount: Int = 0,
)