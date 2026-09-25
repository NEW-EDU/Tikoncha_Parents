package uz.tikoncha_parent.presentation.policy.history

sealed interface PolicyHistoryEvent {
    /** Pull-to-refresh — birinchi sahifadan qayta. */
    data object Refresh : PolicyHistoryEvent

    /** Ro'yxat oxiriga yetganda keyingi sahifa. */
    data object LoadMore : PolicyHistoryEvent

    /** To'liq ekran xatosidan keyin. */
    data object RetryInitial : PolicyHistoryEvent

    /** Ro'yxat ostidagi xato kartasidan keyin. */
    data object RetryPagination : PolicyHistoryEvent
}