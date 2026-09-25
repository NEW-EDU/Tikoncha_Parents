package uz.tikoncha_parent.presentation.task

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.mapper.todo.toTask
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.todo.CreatedByRole
import uz.tikoncha_parent.domain.model.todo.TodoFilter
import uz.tikoncha_parent.domain.model.todo.TodosQuery
import uz.tikoncha_parent.domain.use_case.todo.CompleteTodoUseCase
import uz.tikoncha_parent.domain.use_case.todo.DeleteTodoUseCase
import uz.tikoncha_parent.domain.use_case.todo.GetTodosUseCase
import uz.tikoncha_parent.presentation.task.model.Task
import uz.tikoncha_parent.presentation.ui_state.ResponseState

class TaskListViewModel(
    private val getTodosUseCase: GetTodosUseCase,
    private val completeTodoUseCase: CompleteTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(TaskListState())
    val state = _state.asStateFlow()

    private val _effect = Channel<TaskListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var loadJob: Job? = null
    private var countJob: Job? = null

    init {
        _state.update {
            it.copy(
                selectedChild = AppSettings.selectedChild,
                userId = AppSettings.userId,
                childrenList = AppSettings.children
            )
        }
    }

    fun onEvent(event: TaskListEvent) {
        when (event) {
            TaskListEvent.LoadTasks -> {
                initialLoad()
            }

            TaskListEvent.OnRefresh -> {
                refresh()
            }

            TaskListEvent.OnLoadMore -> {
                loadMore()
            }

            TaskListEvent.OnRetry -> {
                firstPage()
            }

            TaskListEvent.LoadAllChildrenActiveTasks -> {
                syncChildren()
                loadAllChildrenActiveTasks()
            }

            is TaskListEvent.OnFilterChipToggled -> {
                toggleChip(event.chip)
            }

            is TaskListEvent.OnTaskSelected -> {
                changeTab(event.taskIndex)
            }

            is TaskListEvent.OnChildSelected -> {
                selectChild(event.child)
            }


            is TaskListEvent.OnCompletedTask -> {
                completeTask(event.task)
            }

            is TaskListEvent.OnDeleteTask -> {
                deleteTask(event.task)
            }

            TaskListEvent.ShowMineAll -> {
                _state.update {
                    it.copy(
                        showMineAll = !it.showMineAll
                    )
                }
            }

            TaskListEvent.ShowChildrenAll -> {
                _state.update {
                    it.copy(
                        showChildrenAll = !it.showChildrenAll
                    )
                }
            }

            TaskListEvent.SyncChildren -> {
                syncChildren()
            }
        }
    }

    private fun syncChildren() {
        val freshChildren = AppSettings.children
        val previousId = state.value.selectedChild?.userId

        // Joriy tanlovni saqlab, ma'lumotini yangilaymiz; bo'lmasa birinchisini olamiz
        val resolvedSelected = freshChildren.firstOrNull { it.userId == previousId }
            ?: AppSettings.selectedChild
            ?: freshChildren.firstOrNull()

        _state.update {
            it.copy(
                childrenList = freshChildren,
                selectedChild = resolvedSelected
            )
        }
    }

    private fun initialLoad() {
        val s = state.value
        if (!s.canFetch) {
            // Child tanlanmagan — yuklamaymiz
            _state.update {
                it.copy(
                    taskList = emptyList(),
                    totalCount = 0,
                    offset = 0,
                    hasMore = true,
                    isRefreshing = false,
                    isInitialLoading = false,
                    isRefiltering = false,
                    isPaginating = false,
                )
            }
            return
        }
        val hasData = s.taskList.isNotEmpty()
        _state.update {
            it.copy(
                isInitialLoading = !hasData,
                isRefiltering = hasData,
            )
        }
        firstPage()
    }


    // ---------------- tab / child / chip ----------------

    private fun changeTab(index: Int) {
        if (index == state.value.taskIndex) return

        loadJob?.cancel()
        val willFetch = state.value.selectedChild != null

        _state.update {
            it.copy(
                taskIndex = index,
                error = null,
                taskList = emptyList(),
                totalCount = 0,
                offset = 0,
                hasMore = true,
                isInitialLoading = willFetch,
                isRefiltering = false,
                isPaginating = false,
            )
        }
        if (willFetch) firstPage()
    }

    private fun selectChild(child: UserInfo) {
        if (child.userId == state.value.selectedChild?.userId) return
        AppSettings.selectedChild = child

        loadJob?.cancel()
        _state.update {
            it.copy(
                selectedChild = child,
                error = null,
                taskList = emptyList(),
                totalCount = 0,
                offset = 0,
                hasMore = true,
                isInitialLoading = true,
                isRefiltering = false,
                isPaginating = false,
            )
        }
        firstPage()
    }

    private fun toggleChip(chip: TaskFilterChip) {
        loadJob?.cancel()
        _state.update {
            val new = if (it.activeChip == chip) null else chip
            it.copy(
                activeChip = new,
                error = null,
                taskList = emptyList(),
                totalCount = 0,
                offset = 0,
                hasMore = true,
                isInitialLoading = it.canFetch,
                isRefiltering = false,
                isPaginating = false,
            )
        }
        if (state.value.canFetch) firstPage()
    }

    // ---------------- fetch ----------------

    private fun firstPage() {
        if (!state.value.canFetch) {
            _state.update {
                it.copy(
                    taskList = emptyList(),
                    totalCount = 0,
                    offset = 0,
                    hasMore = true,
                    isInitialLoading = false,
                    isRefiltering = false,
                    isPaginating = false,
                )
            }
            return
        }
        loadJob?.cancel()
        _state.update {
            it.copy(
                listResponseState = ResponseState.Loading,
                error = null,
                offset = 0,
                hasMore = true,
                isPaginating = false,
            )
        }
        loadJob = screenModelScope.launch { fetchPage(reset = true) }
    }

    private fun refresh() {
        if (!state.value.canFetch) return
        loadJob?.cancel()
        _state.update {
            it.copy(
                isRefreshing = true,
                error = null,
                offset = 0,
                hasMore = true,
                isPaginating = false,
            )
        }
        loadJob = screenModelScope.launch { fetchPage(reset = true) }
    }

    private fun loadMore() {
        val s = state.value
        if (s.isPaginating || s.isInitialLoading || s.isRefreshing || s.isRefiltering) return
        if (!s.hasMore || !s.canFetch) return

        _state.update { it.copy(isPaginating = true) }
        loadJob = screenModelScope.launch { fetchPage(reset = false) }
    }

    private suspend fun fetchPage(reset: Boolean) {
        val s = state.value
        val query = TodosQuery(
            targetUserId = s.currentTargetUserId,   // doimiy child
            filter = buildFilter(
                chip = s.activeChip,
                taskIndex = s.taskIndex,
                forHistory = false
            ),
            limit = s.pageSize,
            offset = if (reset) 0 else s.offset
        )

        when (val res = getTodosUseCase(query)) {
            is Outcome.Success -> {
                val page = res.data
                val mapped = page.items.map { it.toTask() }
                _state.update { current ->
                    val newList = if (reset) mapped else current.taskList + mapped
                    current.copy(
                        taskList = newList,
                        totalCount = page.total,
                        offset = newList.size,
                        hasMore = page.hasMore,
                        isInitialLoading = false,
                        isRefiltering = false,
                        isRefreshing = false,
                        isPaginating = false,
                        listResponseState = ResponseState.Idle,
                        error = null,
                        activeTaskCount = if (current.activeChip == null && current.taskIndex == 0)
                            page.total else current.activeTaskCount
                    )
                }
            }

            is Outcome.Failure -> {
                _state.update { current ->
                    current.copy(
                        isInitialLoading = false,
                        isRefiltering = false,
                        isRefreshing = false,
                        isPaginating = false,
                        hasMore = if (reset) current.hasMore else false,
                        listResponseState = ResponseState.Idle,
                        error = if (reset) res else current.error
                    )
                }
                if (!reset) sendEffect(TaskListEffect.ShowFailure(res))
            }
        }
    }

    // ---------------- complete (Tekshirildi) ----------------

    private fun completeTask(task: Task) {
        if (!task.isChildDone || task.isCompleted) return
        if (task.id in state.value.completingIds) return

        _state.update { it.copy(completingIds = it.completingIds + task.id) }

        screenModelScope.launch {
            when (val res = completeTodoUseCase(task.id)) {
                is Outcome.Success -> {
                    _state.update {
                        val newList = it.taskList.filterNot { t -> t.id == task.id }
                        it.copy(
                            taskList = newList,
                            offset = newList.size,
                            totalCount = (it.totalCount - 1).coerceAtLeast(0),
                            completingIds = it.completingIds - task.id
                        )
                    }
                    sendEffect(TaskListEffect.TaskMarkedAsCompleted)
                }

                is Outcome.Failure -> {
                    _state.update { it.copy(completingIds = it.completingIds - task.id) }
                    sendEffect(TaskListEffect.ShowFailure(res))
                }
            }
        }
    }

    // ---------------- delete ----------------

    private fun deleteTask(task: Task) {
        if (task.id in state.value.deletingIds) return
        _state.update { it.copy(deletingIds = it.deletingIds + task.id) }

        screenModelScope.launch {
            when (val res = deleteTodoUseCase(task.id)) {
                is Outcome.Success -> {
                    _state.update {
                        val newList = it.taskList.filterNot { t -> t.id == task.id }
                        it.copy(
                            taskList = newList,
                            offset = newList.size,
                            totalCount = (it.totalCount - 1).coerceAtLeast(0),
                            deletingIds = it.deletingIds - task.id
                        )
                    }
                    sendEffect(TaskListEffect.TaskDeleted)
                }

                is Outcome.Failure -> {
                    _state.update { it.copy(deletingIds = it.deletingIds - task.id) }
                    sendEffect(TaskListEffect.ShowFailure(res))
                }
            }
        }
    }

    // ---------------- counter ----------------

    private fun loadAllChildrenActiveTasks() {
        val children = state.value.childrenList
        if (children.isEmpty()) {
            _state.update { it.copy(allChildrenActiveTaskCount = 0) }
            return
        }
        countJob?.cancel()
        countJob = screenModelScope.launch {
            val results = children.map { child ->
                async {
                    getTodosUseCase(
                        TodosQuery(
                            targetUserId = child.userId,
                            filter = TodoFilter(
                                isCompleted = false,
                                createdByRole = CreatedByRole.PARENT
                            ),
                            limit = 1,
                            offset = 0
                        )
                    )
                }
            }.awaitAll()

            var total = 0
            for (res in results) {
                when (res) {
                    is Outcome.Success -> total += res.data.total
                    // Bittasi yiqilsa ham — kam sonni ko'rsatmaymiz.
                    // Eski qiymat state'da qoladi, "0 vazifa" degan yolg'on chiqmaydi.
                    is Outcome.Failure -> return@launch
                }
            }
            _state.update { it.copy(allChildrenActiveTaskCount = total) }
        }
    }

    // ---------------- helpers ----------------

    private fun buildFilter(
        chip: TaskFilterChip?,
        taskIndex: Int,
        forHistory: Boolean
    ): TodoFilter {
        // tab 0 = Mendan = PARENT, tab 1 = Farzandim = CHILD
        val role = if (taskIndex == 0) CreatedByRole.PARENT else CreatedByRole.CHILD

        if (forHistory) return TodoFilter(
            isCompleted = true,
            createdByRole = role
        )
        return when (chip) {
            TaskFilterChip.IN_PROGRESS -> TodoFilter(
                isCompleted = false,
                isChildDone = false,
                isExpired = false,
                createdByRole = role
            )

            TaskFilterChip.DONE_BY_CHILD -> TodoFilter(
                isCompleted = false,
                isChildDone = true,
                isExpired = null,
                createdByRole = role
            )

            TaskFilterChip.OVERDUE -> TodoFilter(
                isCompleted = false,
                isExpired = true,
                createdByRole = role
            )

            null -> TodoFilter(
                isCompleted = false,
                createdByRole = role
            )
        }
    }

    private fun sendEffect(effect: TaskListEffect) {
        screenModelScope.launch { _effect.send(effect) }
    }
}