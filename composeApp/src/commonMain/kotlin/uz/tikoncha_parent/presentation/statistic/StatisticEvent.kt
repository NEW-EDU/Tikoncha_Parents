package uz.tikoncha_parent.presentation.statistic

import uz.tikoncha_parent.domain.model.UserInfo

sealed interface StatisticEvent {
    data object GetChildren : StatisticEvent
    data object PullRefresh : StatisticEvent

    /** Ilova qayta ko'rindi — tarif va tezkor bloklar qayta o'qiladi (sotib olgach darhol ishlasin). */
    data object Resumed : StatisticEvent

    data class OnChildSelected(val child: UserInfo) : StatisticEvent

    data class ChangeMode(val mode: DateSelectionType) : StatisticEvent
    data class PageChanged(val index: Int) : StatisticEvent

    data class BarClicked(val bar: ChartBarUi) : StatisticEvent
    data object DismissUsageDetailsDialog : StatisticEvent

    data class ToggleQuickBlock(val packageName: String) : StatisticEvent
    data object DismissQuickBlockFailure : StatisticEvent
}
