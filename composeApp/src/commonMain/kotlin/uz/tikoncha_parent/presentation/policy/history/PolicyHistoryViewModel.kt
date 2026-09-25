package uz.tikoncha_parent.presentation.policy.history

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PolicyAuditEvent
import uz.tikoncha_parent.domain.model.policy.PolicyEventType
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository
import uz.tikoncha_parent.domain.use_case.policy.GetPolicyEventsUseCase
import kotlin.time.Instant

class PolicyHistoryViewModel(
    private val getPolicyEvents: GetPolicyEventsUseCase,
    private val policyRepository: PolicyRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(PolicyHistoryState())
    val state = _state.asStateFlow()

    private val myUserId: String = AppSettings.userId
    private val childId: String = AppSettings.selectedChild?.userId.orEmpty()

    /** Bir vaqtda bitta so'rov — refresh eskirgan sahifani yangi ro'yxatga qo'shib yubormasligi uchun. */
    private var loadJob: Job? = null

    /** Paket → ilova nomi. Tezkor blok yozuvlarida paket o'rniga nom ko'rsatiladi. */
    private var appNames: Map<String, String> = emptyMap()

    init {
        loadFirstPage(refreshing = false)
    }

    fun onEvent(event: PolicyHistoryEvent) {
        when (event) {
            PolicyHistoryEvent.Refresh -> loadFirstPage(refreshing = true)
            PolicyHistoryEvent.RetryInitial -> loadFirstPage(refreshing = false)
            PolicyHistoryEvent.LoadMore -> loadMore()
            PolicyHistoryEvent.RetryPagination -> {
                if (_state.value.hasNext) loadMore() else loadFirstPage(refreshing = true)
            }
        }
    }

    private fun loadFirstPage(refreshing: Boolean) {
        val s = _state.value
        if (s.isInitialLoading || s.isRefreshing) return

        loadJob?.cancel()
        loadJob = screenModelScope.launch {
            _state.update {
                it.copy(
                    isInitialLoading = !refreshing,
                    isRefreshing = refreshing,
                    isLoadingMore = false,
                    initialError = null,
                    paginationError = null,
                )
            }

            // Ilova nomlari tarix bilan parallel olinadi; yiqilsa paket nomi ko'rsatiladi.
            val appsJob = if (appNames.isEmpty() && childId.isNotBlank()) launch { loadAppNames() } else null
            val res = getPolicyEvents(childId, PolicyHistoryState.PAGE_SIZE, 0)
            appsJob?.join()

            when (res) {
                is Outcome.Success -> {
                    val page = res.data
                    val names = policyNames()
                    _state.update {
                        it.copy(
                            items = page.items.map { e -> e.toUi(names) }.distinctBy { item -> item.id },
                            total = if (page.items.isEmpty()) 0 else page.total,
                            nextOffset = page.items.size,
                            isInitialLoading = false,
                            isRefreshing = false,
                            hasLoadedOnce = true,
                        )
                    }
                }

                is Outcome.Failure -> _state.update {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        hasLoadedOnce = true,
                        // Ro'yxat bor bo'lsa saqlaymiz — xato ro'yxat ostida ko'rinadi.
                        initialError = if (it.items.isEmpty()) res else null,
                        paginationError = if (it.items.isEmpty()) null else res,
                    )
                }
            }
        }
    }

    private fun loadMore() {
        val s = _state.value
        if (s.isInitialLoading || s.isRefreshing || s.isLoadingMore || !s.hasNext) return

        loadJob = screenModelScope.launch {
            _state.update { it.copy(isLoadingMore = true, paginationError = null) }

            when (val res = getPolicyEvents(childId, PolicyHistoryState.PAGE_SIZE, s.nextOffset)) {
                is Outcome.Success -> {
                    val page = res.data
                    val names = policyNames()
                    _state.update {
                        it.copy(
                            // Sahifalar orasida yangi yozuv qo'shilsa offset siljiydi va takror keladi.
                            items = (it.items + page.items.map { e -> e.toUi(names) })
                                .distinctBy { item -> item.id },
                            // Bo'sh sahifa — server boshqa bermaydi, cheksiz so'rovni to'xtatamiz.
                            total = if (page.items.isEmpty()) it.nextOffset else page.total,
                            nextOffset = it.nextOffset + page.items.size,
                            isLoadingMore = false,
                        )
                    }
                }

                is Outcome.Failure -> _state.update {
                    it.copy(isLoadingMore = false, paginationError = res)
                }
            }
        }
    }

    /** Muvaffaqiyatsiz bo'lsa bo'sh qoladi — keyingi refresh'da qayta uriniladi. */
    private suspend fun loadAppNames() {
        val res = policyRepository.childApps(childId)
        if (res is Outcome.Success) {
            appNames = res.data.associate { app ->
                app.`package` to (app.name?.takeIf { it.isNotBlank() } ?: app.`package`)
            }
        }
    }

    /** Jadval id → nom. Tarix jadvallar ro'yxatidan ochiladi, shuning uchun kesh odatda to'la. */
    private fun policyNames(): Map<String, String> =
        policyRepository.cachedPolicies(childId).associate { it.id to it.name }

    private fun PolicyAuditEvent.toUi(names: Map<String, String>): PolicyHistoryItemUi {
        val zone = TimeZone.currentSystemDefault()
        val (nameFrom, nameTo) = diff["name"] ?: (null to null)
        val renamed = nameFrom != null && nameTo != null && nameFrom != nameTo
        val (pausedFrom, pausedTo) = diff["paused_until"] ?: (null to null)
        val isQuickBlock = event == PolicyEventType.QUICK_BLOCK_ADD || event == PolicyEventType.QUICK_BLOCK_REMOVE

        return PolicyHistoryItemUi(
            id = id,
            event = event,
            policyName = names[policyId] ?: nameTo ?: nameFrom,
            actor = when (actorUserId) {
                null -> HistoryActor.UNKNOWN
                myUserId -> HistoryActor.ME
                childId -> HistoryActor.CHILD
                else -> HistoryActor.OTHER_PARENT
            },
            createdAt = createdAt.toLocalDateTime(zone),
            pausedUntil = pausedTo
                ?.let { raw -> runCatching { Instant.parse(raw) }.getOrNull() }
                ?.toLocalDateTime(zone),
            renamedFrom = nameFrom.takeIf { renamed },
            renamedTo = nameTo.takeIf { renamed },
            // diff {"key": "com.app", "type": "APP"} — ilova nomi, sayt bo'lsa domen o'zi.
            quickBlockTarget = diff["key"]?.second
                ?.takeIf { isQuickBlock }
                ?.let { key -> appNames[key] ?: key },
            // Server davom ettirishni alohida event emas, UPDATED bilan yozadi.
            isResume = event == PolicyEventType.UPDATED && pausedFrom != null && pausedTo == null,
        )
    }
}