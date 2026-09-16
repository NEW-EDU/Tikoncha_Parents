@file:OptIn(ExperimentalTime::class)

package uz.tikoncha_parent.presentation.new_home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.PolicyType
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

    private val hasAppUsageLoaded = MutableStateFlow<String?>("")
    private val hasPolicyLoaded = MutableStateFlow<String?>("")
    private val hasTaskLoaded = MutableStateFlow<String?>("")

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private var childrenJob: Job? = null
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
                _state.update { it.copy(selectedChild = event.child) }
                AppSettings.selectedChildId = event.child.userId
                AppSettings.selectedChild = event.child
                loadAll()
            }

            HomeEvent.GetChildren -> loadChildren()
            HomeEvent.ReloadUserInfo -> reloadUserInfo()

            HomeEvent.PullRefresh -> {
                _state.update { it.copy(isRefreshing = true) }
                reloadUserInfo()
                loadChildren()
            }

            HomeEvent.RefreshParentRequest -> {
                loadProtectionStatus()
            }

            HomeEvent.SyncSelectedChildFromSettings -> {
                Logger.d(TAG, "SyncSelectedChildFromSettings = ${AppSettings.selectedChild}")
                _state.update {
                    it.copy(
                        childrenList = AppSettings.children,
                        selectedChild = AppSettings.selectedChild,
                    )
                }
                loadAll()
            }
        }
    }

    private fun loadAll() {
        val selectedChildId = _state.value.selectedChild?.userId
        if (hasTaskLoaded.value != selectedChildId) {
            loadTasks()
        }
        observePolicyCount()
        if (hasPolicyLoaded.value != selectedChildId) {
            loadPolicies()
        }
        if (hasAppUsageLoaded.value != selectedChildId) {
            loadTodayUsage()
        }
        loadProtectionStatus()
    }

    private fun loadProtectionStatus() {
        val childId = _state.value.selectedChild?.userId
        if (childId.isNullOrEmpty()) {
            protectionJob?.cancel()
            protectionChildId = null
            _state.update {
                it.copy(
                    protectionPendingRequestCount = 0,
                    protectionPermissionOffCount = 0,
                )
            }
            return
        }

        if (protectionJob?.isActive == true && protectionChildId == childId) return

        protectionJob?.cancel()
        protectionChildId = childId
        protectionJob = screenModelScope.launch {
            when (val res = protectionRepository.protectionStatus(childId)) {
                is Outcome.Success -> _state.update {
                    it.copy(
                        protectionPendingRequestCount = res.data.pendingRequestCount(),
                        protectionPermissionOffCount = res.data.missingRequiredPermissionCount(),
                    )
                }

                is Outcome.Failure -> Unit
            }
        }
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

    private fun loadChildren() {
        childrenJob?.cancel()
        childrenJob = screenModelScope.launch {
            _state.update { it.copy(childrenResponseState = ResponseState.Loading) }

            when (val res = childRepository.children()) {
                is Outcome.Failure -> _state.update {
                    it.copy(
                        childrenResponseState = ResponseState.Error(failure = res),
                        // Server javob bermadi — lokal keshdan ko'rsatamiz.
                        // Aks holda farzand bor bo'lsa ham "Farzand qo'shilmagan" chiqadi.
                        childrenList = it.childrenList.ifEmpty { AppSettings.children },
                        selectedChild = it.selectedChild ?: AppSettings.selectedChild,
                        isRefreshing = false,
                    )
                }

                is Outcome.Success -> {
                    val children = res.data
                    val previousChildId = _state.value.selectedChild?.userId

                    AppSettings.syncSelectedChildWith(children)
                    if (children.isEmpty()) {
                        AppSettings.selectedChild = null
                        AppSettings.selectedChildId = ""
                    }
                    _state.update {
                        it.copy(
                            childrenResponseState = ResponseState.Success(),
                            childrenList = AppSettings.children,
                            selectedChild = AppSettings.selectedChild,
                            isRefreshing = false,
                        )
                    }

                    // Serverdan kelgan farzand keshdagi bilan bir xil bo'lsa,
                    // SyncSelectedChildFromSettings allaqachon yuklagan — takrorlamaymiz.
                    if (AppSettings.selectedChild?.userId != previousChildId) {
                        loadAll()
                    }
                }
            }
        }
    }


    private fun loadTasks() = screenModelScope.launch {
        val selectedId = state.value.selectedChild?.userId ?: return@launch

        val query = TodosQuery(
            targetUserId = selectedId,
            filter = TodoFilter(),
            limit = 500,
            offset = 0,
        )

        when (val res = getTodosUseCase(query)) {
            is Outcome.Success -> {
                _state.update {
                    it.copy(activeTaskCount = res.data.items.count { todo -> !todo.isCompleted })
                }
                hasTaskLoaded.value = _state.value.selectedChild?.userId
            }

            is Outcome.Failure -> Unit
        }
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
    private fun loadPolicies() = screenModelScope.launch {
        val selectedChildId = _state.value.selectedChild?.userId ?: return@launch
        if (policyRepository.refreshPolicies(selectedChildId) is Outcome.Success) {
            hasPolicyLoaded.value = selectedChildId
        }
    }

    private fun loadTodayUsage() {
        val childId = _state.value.selectedChild?.userId
        if (childId.isNullOrEmpty()) return
        todayUsageJob?.cancel()
        todayUsageJob = screenModelScope.launch {
            when (val res = todayUsageUseCase.invoke(childId)) {
                is Outcome.Success -> {
                    _state.update { it.copy(todayUsage = res.data) }
                    hasAppUsageLoaded.value = _state.value.selectedChild?.userId
                }

                is Outcome.Failure -> Unit
            }
        }
    }
}