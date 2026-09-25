package uz.tikoncha_parent.presentation.statistic

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.permission_status.PermissionStatusType
import uz.tikoncha_parent.domain.model.policy.QuickBlockSnapshot
import uz.tikoncha_parent.domain.repository.ChildRepository
import uz.tikoncha_parent.domain.repository.PermissionStatusRepository
import uz.tikoncha_parent.domain.use_case.app_usage.GetUsageHistoryUseCase
import uz.tikoncha_parent.domain.use_case.policy.ChildPaidStatusUseCase
import uz.tikoncha_parent.domain.use_case.policy.ObserveQuickBlockSnapshotUseCase
import uz.tikoncha_parent.domain.use_case.policy.RefreshQuickBlocksUseCase
import uz.tikoncha_parent.domain.use_case.policy.ToggleQuickBlockUseCase
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import kotlin.time.Clock

/**
 * Statistika. Hisob ([uz.tikoncha_parent.domain.model.app_usage.UsageHistory]) va tezkor blok
 * qarorlari ([QuickBlockSnapshot]) domen qatlamida — bu yerda faqat yuklash va UI modelga o'girish.
 */
class StatisticViewModel(
    private val childRepository: ChildRepository,
    private val permissionStatusRepository: PermissionStatusRepository,
    private val getUsageHistory: GetUsageHistoryUseCase,
    private val observeQuickBlockSnapshot: ObserveQuickBlockSnapshotUseCase,
    private val refreshQuickBlocks: RefreshQuickBlocksUseCase,
    private val toggleQuickBlock: ToggleQuickBlockUseCase,
    private val childPaidStatus: ChildPaidStatusUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow(StatisticState(today = today()))
    val state = _state.asStateFlow()

    private var childrenJob: Job? = null
    private var appUsageJob: Job? = null
    private var permissionJob: Job? = null
    private var recomputeJob: Job? = null
    private var quickBlockObserveJob: Job? = null
    private var observedQuickBlockChildId: String? = null

    init {
        // Pauza tugashi "hozir"ga bog'liq — daqiqada bir marta yangilanadi
        screenModelScope.launch {
            while (isActive) {
                delay(MINUTE_MS)
                _state.update { it.copy(now = Clock.System.now()) }
            }
        }
    }

    fun onEvent(event: StatisticEvent) {
        when (event) {
            StatisticEvent.GetChildren -> loadChildren()

            StatisticEvent.PullRefresh -> {
                _state.update { it.copy(isRefreshing = true) }
                refreshChildData()
            }

            StatisticEvent.Resumed -> {
                val childId = _state.value.selectedChild?.userId ?: return
                refreshPaid(childId)
                refreshQuickBlockList(childId)
            }

            is StatisticEvent.OnChildSelected -> selectChild(event.child)
            is StatisticEvent.ChangeMode -> changeMode(event.mode)
            is StatisticEvent.PageChanged -> selectPage(event.index)
            is StatisticEvent.BarClicked -> handleBarClick(event.bar)

            StatisticEvent.DismissUsageDetailsDialog ->
                _state.update { it.copy(showUsageDetailsDialog = false) }

            is StatisticEvent.ToggleQuickBlock -> toggle(event.packageName)

            StatisticEvent.DismissQuickBlockFailure ->
                _state.update { it.copy(quickBlockFailure = null, quickBlockPremiumFailure = null) }
        }
    }

    /* ---------------- CHILDREN ---------------- */
    private fun loadChildren() {
        childrenJob?.cancel()
        childrenJob = screenModelScope.launch {
            _state.update { it.copy(childrenResponseState = ResponseState.Loading) }

            when (val res = childRepository.children()) {
                is Outcome.Failure -> _state.update {
                    it.copy(
                        childrenResponseState = ResponseState.Error(failure = res),
                        childrenList = it.childrenList.ifEmpty { AppSettings.children },
                    )
                }

                is Outcome.Success -> {
                    if (AppSettings.selectedChild == null) AppSettings.selectedChild = res.data.firstOrNull()
                    _state.update {
                        it.copy(
                            childrenResponseState = ResponseState.Success(),
                            childrenList = res.data,
                            selectedChild = AppSettings.selectedChild,
                        )
                    }
                    refreshChildData()
                }
            }
        }
    }

    private fun selectChild(child: UserInfo) {
        Logger.d(TAG, "OnChildSelected childId=${child.userId}")
        if (child.userId == _state.value.selectedChild?.userId) return

        AppSettings.selectedChildId = child.userId
        AppSettings.selectedChild = child

        // Yangi bola — boshqa ma'lumot; eskisi bir lahza ham ko'rinmasin
        _state.update {
            it.copy(
                selectedChild = child,
                history = StatisticState().history,
                pages = emptyList(),
                bars = emptyBars(it.dateSelectionType),
                apps = emptyList(),
                quick = QuickBlockSnapshot(myUserId = AppSettings.userId),
                quickBlockInProgress = emptySet(),
            )
        }
        refreshChildData()
    }

    /** Tanlangan bola uchun hammasi: foydalanish, ruxsatlar, tezkor bloklar va tarif. */
    private fun refreshChildData() {
        val childId = _state.value.selectedChild?.userId
        if (childId.isNullOrBlank()) {
            quickBlockObserveJob?.cancel()
            observedQuickBlockChildId = null
            _state.update { it.copy(isRefreshing = false, quick = QuickBlockSnapshot()) }
            return
        }
        loadUsage(childId)
        loadPermissionStatus(childId)
        observeQuickBlocks(childId)
        refreshQuickBlockList(childId)
        refreshPaid(childId)
    }

    /* ---------------- APP USAGE ---------------- */
    private fun loadUsage(childId: String) {
        appUsageJob?.cancel()
        appUsageJob = screenModelScope.launch {
            val today = today()
            _state.update { it.copy(appUsageResponseState = ResponseState.Loading, today = today) }

            when (val res = getUsageHistory(childId, today)) {
                is Outcome.Failure -> _state.update {
                    it.copy(appUsageResponseState = ResponseState.Error(failure = res), isRefreshing = false)
                }

                is Outcome.Success -> {
                    _state.update {
                        it.copy(appUsageResponseState = ResponseState.Success(), history = res.data, isRefreshing = false)
                    }
                    rebuildPagesForCurrentMode()
                }
            }
        }
    }

    /* ---------------- QUICK BLOCK ---------------- */
    /** Bitta oqim: bloklar kim tomonidan, yoqilganmi, pauzadami — bir emissiyada (qator sakramaydi). */
    private fun observeQuickBlocks(childId: String) {
        if (observedQuickBlockChildId == childId && quickBlockObserveJob?.isActive == true) return
        quickBlockObserveJob?.cancel()
        observedQuickBlockChildId = childId
        quickBlockObserveJob = screenModelScope.launch {
            observeQuickBlockSnapshot(childId, AppSettings.userId).collect { snapshot ->
                // Tarif bu oqimda yo'q — oldingi qiymat saqlanadi
                _state.update { it.copy(quick = snapshot.copy(paid = it.quick.paid), now = Clock.System.now()) }
            }
        }
    }

    private fun refreshQuickBlockList(childId: String) {
        screenModelScope.launch {
            val res = refreshQuickBlocks(childId)
            if (res is Outcome.Failure) Logger.d(TAG, "quick-block refresh xato: ${res.cause}")
        }
    }

    /** Lokal keshdan darhol, keyin serverdan — sotib olgach qaytganda qulf darhol ishlasin. */
    private fun refreshPaid(childId: String) {
        _state.update { it.copy(quick = it.quick.copy(paid = childPaidStatus(childId))) }
        screenModelScope.launch {
            val paid = childPaidStatus.refresh(childId)
            if (_state.value.selectedChild?.userId == childId) {
                _state.update { it.copy(quick = it.quick.copy(paid = paid)) }
            }
        }
    }

    private fun toggle(packageName: String) {
        val childId = _state.value.selectedChild?.userId
        if (childId.isNullOrBlank() || packageName.isBlank()) return
        if (packageName in _state.value.quickBlockInProgress) return

        screenModelScope.launch {
            _state.update { it.copy(quickBlockInProgress = it.quickBlockInProgress + packageName) }
            val res = toggleQuickBlock(childId, packageName, _state.value.quick)
            _state.update { it.copy(quickBlockInProgress = it.quickBlockInProgress - packageName) }

            // Muvaffaqiyatda holat o'zi keladi: repozitoriy keshni javobdan yangilaydi → oqim.
            if (res is Outcome.Failure) {
                _state.update {
                    if (res.cause is ErrorCause.PremiumRequired) {
                        // Lokal tarif eskirgan — server bepul dedi
                        it.copy(quickBlockPremiumFailure = res, quick = it.quick.copy(paid = false))
                    } else {
                        it.copy(quickBlockFailure = res)
                    }
                }
            }
        }
    }

    /* ---------------- PERMISSION ---------------- */
    private fun loadPermissionStatus(childId: String) {
        permissionJob?.cancel()
        permissionJob = screenModelScope.launch {
            val res = permissionStatusRepository.permissionStatus(childId = childId, state = PermissionStatusType.STATISTICS)
            _state.update { it.copy(permissionIssueList = (res as? Outcome.Success)?.data.orEmpty()) }
        }
    }

    /* ---------------- MODE / PAGE ---------------- */
    private fun changeMode(newMode: DateSelectionType) {
        if (newMode == _state.value.dateSelectionType) return
        _state.update { it.copy(dateSelectionType = newMode) }
        rebuildPagesForCurrentMode()
    }

    private fun selectPage(index: Int) {
        val s = _state.value
        if (index !in s.pages.indices || index == s.selectedPageIndex) return
        _state.update { it.copy(selectedPageIndex = index) }
        recomputeBarsAndApps()
    }

    private fun rebuildPagesForCurrentMode() {
        val s = _state.value
        val today = s.today ?: return
        val pages = when (s.dateSelectionType) {
            DateSelectionType.WEEK -> buildWeeklyPages(s.history, today)
            DateSelectionType.DAY -> buildDailyPages(s.history, today)
        }
        _state.update { it.copy(pages = pages, selectedPageIndex = pages.lastIndex.coerceAtLeast(0)) }
        recomputeBarsAndApps()
    }

    private fun recomputeBarsAndApps() {
        val s = _state.value
        val page = s.selectedPage
        if (page == null) {
            _state.update { it.copy(bars = emptyBars(s.dateSelectionType), apps = emptyList()) }
            return
        }

        recomputeJob?.cancel()
        recomputeJob = screenModelScope.launch(Dispatchers.Default) {
            val bars = when (s.dateSelectionType) {
                DateSelectionType.WEEK -> buildWeeklyBars(s.history, page)
                DateSelectionType.DAY -> buildDailyBars(s.history, page)
            }
            val apps = buildStatApps(s.history, page)
            _state.update { it.copy(bars = bars, apps = apps) }
        }
    }

    /* ---------------- BAR CLICK ---------------- */
    private fun handleBarClick(bar: ChartBarUi) {
        if (bar.totalMillis <= 0L) return
        val s = _state.value
        val page = s.selectedPage ?: return
        val details = when (s.dateSelectionType) {
            DateSelectionType.WEEK -> buildWeeklyBarDetails(s.history, page, bar)
            DateSelectionType.DAY -> buildDailyBarDetails(s.history, page, bar)
        }
        _state.update { it.copy(usageDetails = details, showUsageDetailsDialog = true) }
    }

    private fun today(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private companion object {
        const val TAG = "StatisticViewModel"
        const val MINUTE_MS = 60_000L
    }
}
