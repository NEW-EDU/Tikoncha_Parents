@file:OptIn(ExperimentalTime::class)

package uz.tikoncha_parent.presentation.statistic

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import uz.tikoncha_parent.core.FeatureFlags
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.permission_status.PermissionStatusType
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.repository.ChildRepository
import uz.tikoncha_parent.domain.repository.PaymentRepository
import uz.tikoncha_parent.domain.repository.PermissionStatusRepository
import uz.tikoncha_parent.domain.use_case.policy.AddQuickBlockUseCase
import uz.tikoncha_parent.domain.use_case.policy.GrantBonusTimeUseCase
import uz.tikoncha_parent.domain.use_case.policy.ObserveQuickBlocksUseCase
import uz.tikoncha_parent.domain.use_case.policy.RefreshQuickBlocksUseCase
import uz.tikoncha_parent.domain.use_case.policy.RemoveQuickBlockUseCase
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class StatisticViewModel(
    private val paymentRepository: PaymentRepository,
    private val childRepository: ChildRepository,
    private val permissionStatusRepository: PermissionStatusRepository,
    private val observeQuickBlocks: ObserveQuickBlocksUseCase,
    private val refreshQuickBlocks: RefreshQuickBlocksUseCase,
    private val addQuickBlock: AddQuickBlockUseCase,
    private val removeQuickBlock: RemoveQuickBlockUseCase,
    private val grantBonusTime: GrantBonusTimeUseCase,
) : ScreenModel {

    private val TAG = "StatisticViewModel"

    private val _state = MutableStateFlow(StatisticState())
    val state = _state.asStateFlow()

    private var childrenJob: Job? = null
    private var appUsageJob: Job? = null
    private var permissionJob: Job? = null
    private var recomputeJob: Job? = null
    private var quickBlockObserveJob: Job? = null
    private var observedQuickBlockChildId: String? = null
    private val bonusInProgress = mutableSetOf<String>()

    init {
        Logger.d(TAG, "INIT")
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        _state.update { it.copy(today = today, myUserId = AppSettings.userId) }
    }

    fun onEvent(event: StatisticEvent) {
        when (event) {
            StatisticEvent.Init                          -> { /* Screen LaunchedEffect dan kirsa */ loadChildren() }
            StatisticEvent.GetChildren                   -> loadChildren()
            StatisticEvent.GetAppUsage                   -> loadAppUsages()
            StatisticEvent.RefreshSubscriptionLimit      -> refreshSubscriptionLimit()

            StatisticEvent.RefreshChild -> {
                val limit = AppSettings.subscriptionLimitList
                    .find { it.childId == AppSettings.selectedChild?.userId }
                    ?: SubscriptionLimit()
                _state.update {
                    it.copy(
                        selectedChild = AppSettings.selectedChild,
                        subscriptionLimit = limit,
                        showBlur = shouldShowBlur(limit)
                    )
                }
                loadQuickBlocks()
            }

            is StatisticEvent.OnChildSelected            -> selectChild(event.child)
            is StatisticEvent.ChangeMode                 -> changeMode(event.mode)
            is StatisticEvent.PageChanged                -> selectPage(event.index)
            is StatisticEvent.BarClicked                 -> handleBarClick(event.bar)

            StatisticEvent.DismissUsageDetailsDialog ->
                _state.update { it.copy(showUsageDetailsDialog = false) }

            is StatisticEvent.ToggleQuickBlock -> toggleQuickBlock(event.packageName)

            StatisticEvent.DismissQuickBlockFailure -> {
                _state.update { it.copy(quickBlockFailure = null, quickBlockPremiumFailure = null) }
            }


            is StatisticEvent.GrantBonusTime -> {
                grantBonus(event.packageName, event.policyName, event.minutes)
            }

            StatisticEvent.ClearAll -> {
                quickBlockObserveJob?.cancel()
                observedQuickBlockChildId = null
                _state.update { StatisticState(today = it.today, myUserId = it.myUserId) }
            }
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
                    val list = res.data
                    _state.update {
                        it.copy(
                            childrenResponseState = ResponseState.Success(),
                            childrenList = list,
                            selectedChild = AppSettings.selectedChild
                        )
                    }
                    if (AppSettings.selectedChild == null) {
                        AppSettings.selectedChild = list.firstOrNull()
                        _state.update { it.copy(selectedChild = AppSettings.selectedChild) }
                    }
                    loadAppUsages()
                    loadPermissionStatus()
                    loadQuickBlocks()
                }
            }
        }
    }

    private fun selectChild(child: uz.tikoncha_parent.domain.model.UserInfo) {
        Logger.d(TAG, "OnChildSelected childId=${child.userId}")
        if (child.userId == _state.value.selectedChild?.userId) return

        AppSettings.selectedChildId = child.userId
        AppSettings.selectedChild = child

        val limit = AppSettings.subscriptionLimitList
            .find { it.childId == child.userId } ?: SubscriptionLimit()

        _state.update {
            it.copy(
                selectedChild = child,
                subscriptionLimit = limit,
                showBlur = shouldShowBlur(limit),
                // tozalash — yangi child boshqa data'ga ega
                appUsageList = emptyList(),
                pages = emptyList(),
                bars = emptyBars(it.dateSelectionType),
                topApps = emptyList(),
                quickBlocks = emptyList(),
            )
        }
        loadAppUsages()
        loadPermissionStatus()
        loadQuickBlocks()
    }

    /* ---------------- APP USAGE ---------------- */
    private fun loadAppUsages() {
        val childId = state.value.selectedChild?.userId
        if (childId.isNullOrEmpty()) return

        appUsageJob?.cancel()
        appUsageJob = screenModelScope.launch {
            _state.update { it.copy(appUsageResponseState = ResponseState.Loading) }

            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val from = today.minus(15, DateTimeUnit.DAY)

            when (val res = childRepository.appUsages(childId, from = from, to = today)) {
                is Outcome.Failure -> _state.update {
                    it.copy(appUsageResponseState = ResponseState.Error(failure = res))
                }
                is Outcome.Success -> {
                    _state.update {
                        it.copy(
                            appUsageResponseState = ResponseState.Success(),
                            appUsageList = res.data
                        )
                    }
                    rebuildPagesForCurrentMode()
                }
            }
        }
    }

    /* ---------------- QUICK BLOCK ---------------- */
    /** Keshni kuzatadi va serverdan yangilaydi. Bola o'zgarmagan bo'lsa kuzatuv qayta boshlanmaydi. */
    private fun loadQuickBlocks() {
        val childId = _state.value.selectedChild?.userId
        if (childId.isNullOrBlank()) {
            quickBlockObserveJob?.cancel()
            observedQuickBlockChildId = null
            _state.update { it.copy(quickBlocks = emptyList()) }
            return
        }

        if (observedQuickBlockChildId != childId || quickBlockObserveJob?.isActive != true) {
            quickBlockObserveJob?.cancel()
            observedQuickBlockChildId = childId
            quickBlockObserveJob = screenModelScope.launch {
                observeQuickBlocks(childId).collect { list ->
                    _state.update { it.copy(quickBlocks = list) }
                }
            }
        }

        screenModelScope.launch {
            val res = refreshQuickBlocks(childId)
            if (res is Outcome.Failure) Logger.d(TAG, "quick-block refresh xato: ${res.cause}")
        }
    }

    private fun toggleQuickBlock(packageName: String) {
        val s = _state.value
        val childId = s.selectedChild?.userId
        if (childId.isNullOrBlank() || packageName.isBlank()) return
        if (packageName in s.quickBlockInProgress) return

        val mine = s.blockedByMe(packageName)
        // Faqat boshqa ota-ona yoki bola bloklagan — o'zgartirmaymiz, UI xabar ko'rsatadi.
        if (!mine && s.blockedByOthers(packageName)) return

        screenModelScope.launch {
            _state.update { it.copy(quickBlockInProgress = it.quickBlockInProgress + packageName) }

            val target = QuickBlockTarget.app(packageName)
            val res = if (mine) removeQuickBlock(childId, target) else addQuickBlock(childId, target)

            _state.update { it.copy(quickBlockInProgress = it.quickBlockInProgress - packageName) }

            // Muvaffaqiyatda holat o'zi keladi: repozitoriy refresh() qiladi → kuzatuv oqimi yangilanadi.
            if (res is Outcome.Failure) {
                _state.update {
                    if (res.cause is ErrorCause.PremiumRequired) it.copy(quickBlockPremiumFailure = res)
                    else it.copy(quickBlockFailure = res)
                }
            }
        }
    }


    /* ---------------- BONUS VAQT (FeatureFlags.BONUS_TIME) ---------------- */
    private fun grantBonus(packageName: String, policyName: String, minutes: Int) {
        if (!FeatureFlags.BONUS_TIME) return
        val childId = _state.value.selectedChild?.userId
        if (childId.isNullOrBlank() || packageName.isBlank()) return
        if (!bonusInProgress.add(packageName)) return

        screenModelScope.launch {
            val res = grantBonusTime(childId, listOf(packageName), minutes, policyName)
            bonusInProgress.remove(packageName)

            // Muvaffaqiyatda jadval keshga tushadi va "Cheklovlar" da muddati bilan ko'rinadi.
            // Xato dialoglari tezkor blok bilan umumiy — ikkalasi ham ilova qatoridagi amal.
            if (res is Outcome.Failure) {
                _state.update {
                    if (res.cause is ErrorCause.PremiumRequired) it.copy(quickBlockPremiumFailure = res)
                    else it.copy(quickBlockFailure = res)
                }
            }
        }
    }

    /* ---------------- PERMISSION ---------------- */
    private fun loadPermissionStatus() {
        val childId = _state.value.selectedChild?.userId ?: return
        permissionJob?.cancel()
        permissionJob = screenModelScope.launch {
            when (val res = permissionStatusRepository.permissionStatus(
                childId = childId,
                state = PermissionStatusType.STATISTICS,
            )) {
                is Outcome.Success -> _state.update { it.copy(permissionIssueList = res.data) }
                is Outcome.Failure -> _state.update { it.copy(permissionIssueList = emptyList()) }
            }
        }
    }

    /* ---------------- SUBSCRIPTION ---------------- */
    private fun refreshSubscriptionLimit() {
        screenModelScope.launch {
            paymentRepository.syncSubscriptionLimits()
            // useCase AppSettings.subscriptionLimitList ni yangilaydi (ehtimol).
            // Shundan keyin tanlangan child'ga mos limitni state ga ko'chirib qo'yamiz:
            val limit = AppSettings.subscriptionLimitList
                .find { it.childId == _state.value.selectedChild?.userId }
                ?: SubscriptionLimit()
            _state.update {
                it.copy(
                    subscriptionLimit = limit,
                    showBlur = shouldShowBlur(limit)
                )
            }
        }
    }

    /**
     * SubscriptionLimit modelingizdagi maydonlarga qarab moslang.
     * Masalan: `limit.exceeded`, `limit.remaining <= 0`, va h.k.
     */
    private fun shouldShowBlur(limit: SubscriptionLimit?): Boolean {
        if (limit == null) return false
        // TODO: SubscriptionLimit ichida qaysi maydon "exceeded" ni bildirsa, shu yerga yozing
        // return limit.isExceeded
        return false
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
        recomputeBarsAndTopApps()
    }

    private fun rebuildPagesForCurrentMode() {
        val s = _state.value
        val today = s.today ?: return
        val pages = when (s.dateSelectionType) {
            DateSelectionType.WEEK -> buildWeeklyPages(s.appUsageList, today)
            DateSelectionType.DAY  -> buildDailyPages(s.appUsageList, today)
        }
        _state.update {
            it.copy(
                pages = pages,
                selectedPageIndex = pages.lastIndex.coerceAtLeast(0)
            )
        }
        recomputeBarsAndTopApps()
    }

    private fun recomputeBarsAndTopApps() {
        val s = _state.value
        val page = s.selectedPage
        if (page == null) {
            _state.update { it.copy(bars = emptyBars(s.dateSelectionType), topApps = emptyList()) }
            return
        }

        recomputeJob?.cancel()
        recomputeJob = screenModelScope.launch(Dispatchers.Default) {
            val bars = when (s.dateSelectionType) {
                DateSelectionType.WEEK -> buildWeeklyBars(s.appUsageList, page)
                DateSelectionType.DAY  -> buildDailyBars(s.appUsageList, page)
            }
            val tops = buildTopApps(s.appUsageList, page)
            _state.update { it.copy(bars = bars, topApps = tops) }
        }
    }

    /* ---------------- BAR CLICK ---------------- */
    private fun handleBarClick(bar: ChartBarUi) {
        if (bar.totalMillis <= 0L) return
        if (_state.value.showBlur) return        // blur ostida click ishlamaydi

        val s = _state.value
        val page = s.selectedPage ?: return
        val details = when (s.dateSelectionType) {
            DateSelectionType.WEEK -> buildWeeklyBarDetails(s.appUsageList, page, bar)
            DateSelectionType.DAY  -> buildDailyBarDetails(s.appUsageList, page, bar)
        }
        _state.update { it.copy(usageDetails = details, showUsageDetailsDialog = true) }
    }
}