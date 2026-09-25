package uz.tikoncha_parent.presentation.statistic

import uz.tikoncha_parent.domain.model.UserInfo

sealed interface StatisticEvent {
    data object Init : StatisticEvent
    data object GetChildren : StatisticEvent
    data object RefreshChild : StatisticEvent
    data object PullRefresh : StatisticEvent
    data object GetAppUsage : StatisticEvent
    data object RefreshSubscriptionLimit : StatisticEvent

    data class OnChildSelected(val child: UserInfo) : StatisticEvent

    data class ChangeMode(val mode: DateSelectionType) : StatisticEvent
    data class PageChanged(val index: Int) : StatisticEvent

    data class BarClicked(val bar: ChartBarUi) : StatisticEvent
    data object DismissUsageDetailsDialog : StatisticEvent

    data class ToggleQuickBlock(val packageName: String) : StatisticEvent
    data object DismissQuickBlockFailure : StatisticEvent

    data class GrantBonusTime(
        val packageName: String,
        val policyName: String,
        val minutes: Int,
    ) : StatisticEvent

    data object ClearAll : StatisticEvent
}