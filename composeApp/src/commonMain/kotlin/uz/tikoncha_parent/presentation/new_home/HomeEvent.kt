package uz.tikoncha_parent.presentation.new_home

import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.domain.model.UsagePeriod

sealed interface HomeEvent {
    data object GetChildren : HomeEvent
    data object ReloadUserInfo : HomeEvent
    data object PullRefresh : HomeEvent
    data object RefreshParentRequest : HomeEvent
    data object SyncSelectedChildFromSettings : HomeEvent
    data class OnChildSelected(val child: UserInfo) : HomeEvent
}