package uz.tikoncha_parent.presentation.task

import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.task.model.Task
import uz.tikoncha_parent.presentation.ui_state.ResponseState

enum class TaskFilterChip { IN_PROGRESS, DONE_BY_CHILD, OVERDUE }

data class TaskListState(
    // Child selector
    val selectedChild: UserInfo? = null,
    val userId: String? = null,
    val childrenList: List<UserInfo> = emptyList(),

    // Tabs
    val taskIndex: Int = 0,        // 0 = Mendan (PARENT), 1 = Farzandim (CHILD)

    // Filter chip (single-select)
    val activeChip: TaskFilterChip? = null,

    // Aktiv ro'yxat
    val taskList: List<Task> = emptyList(),
    val totalCount: Int = 0,

    // Pagination
    val hasMore: Boolean = true,
    val offset: Int = 0,
    val pageSize: Int = 20,

    // Loading
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isPaginating: Boolean = false,
    val isRefiltering: Boolean = false,
    /** Joriy (bola/tab/filtr) uchun ro'yxat kamida bir marta keldi (serverdan yoki keshdan) — bo'sh holat shundan keyingina. */
    val hasLoaded: Boolean = false,

    // Per-item optimistic UI
    val completingIds: Set<String> = emptySet(),
    val deletingIds: Set<String> = emptySet(),


    // Counters
    val activeTaskCount: Int = 0,
    val allChildrenActiveTaskCount: Int = 0,
    val allChildrenActiveTaskCountKnown: Boolean = false,

    // Response
    val listResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val error: Outcome.Failure? = null,

    // Toggles
    val showMineAll: Boolean = false,
    val showChildrenAll: Boolean = false,
) {
    /** Ikkala tabda ham selectedChild kerak — target = farzand. */
    val canFetch: Boolean
        get() = selectedChild != null

    val currentTargetUserId: String?
        get() = selectedChild?.userId
}