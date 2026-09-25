@file:OptIn(ExperimentalTime::class)

package uz.tikoncha_parent.presentation.new_home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.HourMinute
import uz.tikoncha_parent.domain.model.PolicyType
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.protection.missingRequiredPermissionCount
import uz.tikoncha_parent.domain.model.protection.pendingRequestCount
import uz.tikoncha_parent.domain.model.todo.TodoFilter
import uz.tikoncha_parent.domain.model.todo.TodosQuery
import uz.tikoncha_parent.domain.repository.ChildRepository
import uz.tikoncha_parent.domain.repository.DeviceRepository
import uz.tikoncha_parent.domain.repository.PaymentRepository
import uz.tikoncha_parent.domain.repository.ProtectionRepository
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository
import uz.tikoncha_parent.domain.use_case.app_usage.TodayUsageUseCase
import uz.tikoncha_parent.domain.use_case.policy.ObservePoliciesUseCase
import uz.tikoncha_parent.domain.use_case.todo.GetTodosUseCase
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import kotlin.time.ExperimentalTime

class HomeViewModel(
    private val childRepository: ChildRepository,
    private val deviceRepository: DeviceRepository,
    private val paymentRepository: PaymentRepository,
    private val getTodosUseCase: GetTodosUseCase,
    private val policyRepository: PolicyRepository,
    private val todayUsageUseCase: TodayUsageUseCase,
    private val protectionRepository: ProtectionRepository,
    private val observePoliciesUseCase: ObservePoliciesUseCase,
) : ScreenModel {

    private val TAG = "HomeViewModel"
    private val hasLoaded = MutableStateFlow(false)

    /** Jadvallar keshda turadi — tarmoqdan farzand uchun bir marta yoki pull-to-refresh'da olinadi. */
    private val hasPolicyLoaded = MutableStateFlow<String?>("")

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private var childrenJob: Job? = null
    private var taskJob: Job? = null
    private var todayUsageJob: Job? = null
    private var policyObserveJob: Job? = null
    private var observedPolicyChildId: String? = null
    private var protectionJob: Job? = null
    private var protectionChildId: String? = null

    init {
        loadOnce()
    }

    fun loadOnce() {
        val setOk = hasLoaded.compareAndSet(expect = false, update = true)
        if (!setOk) return
        sendDeviceInfo()
        getSubscriptionLimit()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnChildSelected -> {
                AppSettings.selectedChildId = event.child.userId
                AppSettings.selectedChild = event.child
                applyChildren(_state.value.childrenList, event.child)
                loadAll()
            }

            HomeEvent.GetChildren -> loadChildren()
            HomeEvent.ReloadUserInfo -> reloadUserInfo()

            HomeEvent.PullRefresh -> {
                if (_state.value.isRefreshing) return
                _state.update { it.copy(isRefreshing = true) }
                reloadUserInfo()
                loadChildren(refreshAll = true)
            }

            HomeEvent.RefreshParentRequest -> {
                loadProtectionStatus()
            }

            HomeEvent.SyncSelectedChildFromSettings -> {
                Logger.d(TAG, "SyncSelectedChildFromSettings = ${AppSettings.selectedChild}")
                applyChildren(AppSettings.children, AppSettings.selectedChild)
                loadAll()
            }
        }
    }

    /**
     * Tanlangan farzand almashsa, oldingi farzandning raqamlari yangi ism ostida
     * (yoki farzand qolmaganda) ko'rinib qolmasligi uchun kartalar nolga qaytadi.
     */
    private fun applyChildren(children: List<UserInfo>, selectedChild: UserInfo?) {
        _state.update {
            val updated = it.copy(childrenList = children, selectedChild = selectedChild)
            if (it.selectedChild?.userId == selectedChild?.userId) {
                updated
            } else {
                updated.copy(
                    activeTaskCount = 0,
                    parentPolicyCount = 0,
                    todayUsage = HourMinute(0, 0),
                    topApps = emptyList(),
                    topAppsFromRecentDays = false,
                    protectionPendingRequestCount = 0,
                    protectionPermissionOffCount = 0,
                    protectionLoaded = false,
                )
            }
        }
    }

    /**
     * Vazifalar soni, bugungi vaqt va himoya holati har safar yangilanadi —
     * ular boshqa ekranlarda yoki farzand qurilmasida o'zgaradi.
     */
    private fun loadAll(refreshPolicies: Boolean = false): List<Job> {
        observePolicyCount()
        val protection = loadProtectionStatus()

        val childId = _state.value.selectedChild?.userId
        if (childId.isNullOrBlank()) return emptyList()

        return listOfNotNull(
            loadTasks(childId),
            loadTodayUsage(childId),
            if (refreshPolicies || hasPolicyLoaded.value != childId) loadPolicies(childId) else null,
            protection,
        )
    }

    private fun loadProtectionStatus(): Job? {
        val childId = _state.value.selectedChild?.userId
        if (childId.isNullOrEmpty()) {
            protectionJob?.cancel()
            protectionChildId = null
            _state.update {
                it.copy(
                    protectionPendingRequestCount = 0,
                    protectionPermissionOffCount = 0,
                    protectionLoaded = false,
                )
            }
            return null
        }

        if (protectionJob?.isActive == true && protectionChildId == childId) return protectionJob

        protectionJob?.cancel()
        protectionChildId = childId
        return screenModelScope.launch {
            when (val res = protectionRepository.protectionStatus(childId)) {
                is Outcome.Success -> _state.update {
                    it.copy(
                        protectionPendingRequestCount = res.data.pendingRequestCount(),
                        protectionPermissionOffCount = res.data.missingRequiredPermissionCount(),
                        protectionLoaded = true,
                    )
                }

                is Outcome.Failure -> Unit
            }
        }.also { protectionJob = it }
    }

    private fun reloadUserInfo() {
        _state.update {
            it.copy(
                showTikonchaTutorialCard = AppSettings.showTikonchaTutorial,
                userName = AppSettings.userInfo?.name ?: "",
                userImageUrl = AppSettings.userInfo?.avatarUrl ?: AppSettings.profileImageUrl
            )
        }
    }

    private fun sendDeviceInfo() = screenModelScope.launch {
        deviceRepository.registerDevice(AppSettings.fcmToken)
    }

    private fun getSubscriptionLimit() = screenModelScope.launch {
        paymentRepository.syncSubscriptionLimits()
    }

    /**
     * @param refreshAll pull-to-refresh: farzand o'zgarmagan bo'lsa ham barcha kartalar
     * qayta yuklanadi, indikator esa hammasi tugagach yopiladi.
     */
    private fun loadChildren(refreshAll: Boolean = false): Job {
        childrenJob?.cancel()
        return screenModelScope.launch {
            try {
                _state.update { it.copy(childrenResponseState = ResponseState.Loading) }

                val res = childRepository.children()
                val previousChildId = _state.value.selectedChild?.userId

                when (res) {
                    is Outcome.Failure -> _state.update {
                        it.copy(
                            childrenResponseState = ResponseState.Error(failure = res),
                            // Server javob bermadi — lokal keshdan ko'rsatamiz.
                            // Aks holda farzand bor bo'lsa ham "Farzand qo'shilmagan" chiqadi.
                            childrenList = it.childrenList.ifEmpty { AppSettings.children },
                            selectedChild = it.selectedChild ?: AppSettings.selectedChild,
                        )
                    }

                    is Outcome.Success -> {
                        val children = res.data
                        AppSettings.syncSelectedChildWith(children)
                        if (children.isEmpty()) {
                            AppSettings.selectedChild = null
                            AppSettings.selectedChildId = ""
                        }
                        _state.update { it.copy(childrenResponseState = ResponseState.Success()) }
                        applyChildren(AppSettings.children, AppSettings.selectedChild)
                    }
                }

                // Farzand o'zgarmagan bo'lsa SyncSelectedChildFromSettings allaqachon yuklagan —
                // oddiy kirishda takrorlamaymiz, pull-to-refresh'da esa majburan yangilaymiz.
                val childChanged = _state.value.selectedChild?.userId != previousChildId
                if (refreshAll || childChanged) {
                    loadAll(refreshPolicies = refreshAll).joinAll()
                }
            } finally {
                if (refreshAll) _state.update { it.copy(isRefreshing = false) }
            }
        }.also { childrenJob = it }
    }

    private fun loadTasks(childId: String): Job {
        taskJob?.cancel()
        return screenModelScope.launch {
            val query = TodosQuery(
                targetUserId = childId,
                filter = TodoFilter(),
                limit = 500,
                offset = 0,
            )

            val res = getTodosUseCase(query)
            // Javob kelguncha boshqa farzand tanlangan bo'lsa — eski natijani yozmaymiz.
            if (res !is Outcome.Success || _state.value.selectedChild?.userId != childId) return@launch

            _state.update {
                it.copy(activeTaskCount = res.data.items.count { todo -> !todo.isCompleted })
            }
        }.also { taskJob = it }
    }

    /** Keshni kuzatadi — jadval tahrirlanganda raqam o'zi yangilanadi. */
    private fun observePolicyCount() {
        val childId = _state.value.selectedChild?.userId

        if (childId.isNullOrBlank()) {
            policyObserveJob?.cancel()
            observedPolicyChildId = null
            _state.update { it.copy(parentPolicyCount = 0) }
            return
        }

        if (observedPolicyChildId == childId && policyObserveJob?.isActive == true) return

        policyObserveJob?.cancel()
        observedPolicyChildId = childId
        policyObserveJob = screenModelScope.launch {
            observePoliciesUseCase(childId).collect { policies ->
                val count = policies
                    .filter { it.scope == PolicyType.PARENT_CHILD && it.isStandard }
                    .flatMap { it.targets.packages }
                    .toSet().size

                _state.update { it.copy(parentPolicyCount = count) }
            }
        }
    }

    /** Faqat tarmoqdan yangilaydi — hisob observePolicyCount() da. */
    private fun loadPolicies(childId: String): Job = screenModelScope.launch {
        if (policyRepository.refreshPolicies(childId) is Outcome.Success) {
            hasPolicyLoaded.value = childId
        }
    }

    private fun loadTodayUsage(childId: String): Job {
        todayUsageJob?.cancel()
        return screenModelScope.launch {
            val res = todayUsageUseCase(childId)
            // "Bugun" har chaqiruvda qayta hisoblanadi — kun almashganda kechagi raqam qolmaydi.
            if (res !is Outcome.Success || _state.value.selectedChild?.userId != childId) return@launch

            // Bugun ishlatilgan bo'lsa — bugungilar; bo'lmasa oxirgi kunlarniki (UI xira chizadi)
            val useRecent = res.data.topApps.isEmpty() && res.data.recentTopApps.isNotEmpty()
            _state.update {
                it.copy(
                    todayUsage = res.data.total,
                    topApps = if (useRecent) res.data.recentTopApps else res.data.topApps,
                    topAppsFromRecentDays = useRecent,
                )
            }
        }.also { todayUsageJob = it }
    }
}