package uz.tikoncha_parent.presentation.policy.history

import uz.tikoncha_parent.domain.model.app_error.Outcome

data class PolicyHistoryState(
    val items: List<PolicyHistoryItemUi> = emptyList(),
    val total: Int = 0,
    val nextOffset: Int = 0,
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasLoadedOnce: Boolean = false,
    val initialError: Outcome.Failure? = null,
    val paginationError: Outcome.Failure? = null,
) {
    val hasNext: Boolean get() = nextOffset < total

    val showEmpty: Boolean
        get() = items.isEmpty() &&
                hasLoadedOnce &&
                !isInitialLoading &&
                !isRefreshing &&
                initialError == null

    val showInitialError: Boolean
        get() = initialError != null && items.isEmpty()

    val showEndOfList: Boolean
        get() = items.isNotEmpty() && !hasNext && paginationError == null && !isLoadingMore

    companion object {
        const val PAGE_SIZE = 50
        /** Oxiriga shuncha yozuv qolganda keyingi sahifa so'raladi. */
        const val PREFETCH_THRESHOLD = 5
    }
}