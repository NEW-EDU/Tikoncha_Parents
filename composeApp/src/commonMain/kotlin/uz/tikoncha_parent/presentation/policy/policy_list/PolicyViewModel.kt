package uz.tikoncha_parent.presentation.policy.policy_list

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.permission_status.PermissionStatusType
import uz.tikoncha_parent.domain.model.policy.Policy
import uz.tikoncha_parent.domain.repository.ChildRepository
import uz.tikoncha_parent.domain.repository.PermissionStatusRepository
import uz.tikoncha_parent.domain.use_case.policy.ObservePoliciesUseCase
import uz.tikoncha_parent.domain.use_case.policy.PausePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.RefreshPoliciesUseCase
import uz.tikoncha_parent.domain.use_case.policy.TogglePolicyUseCase
import uz.tikoncha_parent.presentation.policy.toItemUi
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import uz.tikoncha_parent.data.mapper.toAppSelectionUi
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.repository.policy.PolicyRepository
import uz.tikoncha_parent.domain.use_case.policy.ObserveQuickBlocksUseCase
import uz.tikoncha_parent.domain.use_case.policy.RefreshQuickBlocksUseCase
import uz.tikoncha_parent.domain.use_case.policy.RemoveQuickBlockUseCase

class PolicyViewModel(
    private val observePolicies: ObservePoliciesUseCase,
    private val refreshPolicies: RefreshPoliciesUseCase,
    private val togglePolicy: TogglePolicyUseCase,
    private val pausePolicy: PausePolicyUseCase,
    private val permissionStatusRepository: PermissionStatusRepository,
    private val childRepository: ChildRepository,
    private val observeQuickBlocks: ObserveQuickBlocksUseCase,
    private val refreshQuickBlocks: RefreshQuickBlocksUseCase,
    private val removeQuickBlock: RemoveQuickBlockUseCase,
    private val policyRepository: PolicyRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(PolicyState())
    val state = _state.asStateFlow()

    private val _effect = Channel<PolicyListEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /** Domain ro'yxati saqlanadi — Tick da yorliqlarni qayta hisoblash uchun kerak. */
    private var domainPolicies: List<Policy> = emptyList()

    private var observeJob: Job? = null
    private var policyJob: Job? = null
    private var permissionJob: Job? = null
    private var childrenJob: Job? = null
    private var quickBlockObserveJob: Job? = null
    private var childAppsJob: Job? = null
    private var childAppsLoadedFor: String? = null

    init {
        _state.update {
            it.copy(
                selectedChild = AppSettings.selectedChild,
                showPolicyTutorialCard = AppSettings.showPolicyTutorial,
                myUserId = AppSettings.userId,
            )
        }

        observeSelectedChild()
        loadPermissionStatus()
        startTicker()
    }

    fun onEvent(event: PolicyEvent) {
        when (event) {
            PolicyEvent.RefreshPolicies -> {
                getSubscriptionLimit()
                loadPermissionStatus()
                getPolicies()
            }

            PolicyEvent.GetChildren -> loadChildren()

            is PolicyEvent.OnChildSelected -> {
                _state.update {
                    it.copy(selectedChild = event.child, quickBlocks = emptyList(), childApps = emptyMap())
                }
                AppSettings.selectedChildId = event.child.userId
                AppSettings.selectedChild = event.child
                childAppsJob?.cancel()
                childAppsLoadedFor = null
                observeSelectedChild()
                getSubscriptionLimit()
                getPolicies()
            }

            is PolicyEvent.OnTypeSelected ->
                _state.update { it.copy(selectedTypeIndex = event.index) }

            is PolicyEvent.TogglePolicy -> toggle(event.policyId, event.enabled)

            is PolicyEvent.OpenPauseSheet ->
                _state.update { it.copy(pauseSheetFor = event.policyId) }

            is PolicyEvent.PausePolicy -> pause(event.policyId, event.option)

            is PolicyEvent.ResumePolicy -> pause(event.policyId, option = null)

            is PolicyEvent.RemoveQuickBlock -> removeQuickBlockFor(event.packageName)

            PolicyEvent.Tick -> remapPolicies()
        }
    }

    // ── Kesh kuzatuvi ─────────────────────────────────────────
    private fun observeSelectedChild() {
        observeJob?.cancel()
        quickBlockObserveJob?.cancel()
        val childId = _state.value.selectedChild?.userId

        if (childId.isNullOrBlank()) {
            domainPolicies = emptyList()
            _state.update { it.copy(policies = emptyList(), quickBlocks = emptyList(), childApps = emptyMap()) }
            return
        }

        observeJob = screenModelScope.launch {
            observePolicies(childId).collect { list ->
                domainPolicies = list
                remapPolicies()
            }
        }

        quickBlockObserveJob = screenModelScope.launch {
            observeQuickBlocks(childId).collect { list ->
                _state.update { it.copy(quickBlocks = list) }
                // Nomlar faqat ko'rsatadigan blok bo'lsagina kerak — ortiqcha so'rov yo'q.
                if (list.any { entry -> entry.targets.packages.isNotEmpty() }) loadChildApps(childId)
            }
        }
    }

    /** Domain → UI. `now` o'zgarganda holat yorliqlari ham qayta hisoblanadi. */
    private fun remapPolicies(now: Instant = Clock.System.now()) {
        _state.update { current ->
            current.copy(
                policies = domainPolicies
                    .map { it.toItemUi(current.myUserId, now) }
                    .sortedByDescending { it.policyType.order },
                now = now,
            )
        }
    }

    /** Pauza tugaganini o'zi sezishi uchun — daqiqada bir marta. */
    private fun startTicker() {
        screenModelScope.launch {
            while (true) {
                delay(TICK_INTERVAL_MS)
                remapPolicies()
            }
        }
    }

    // ── Amallar ───────────────────────────────────────────────
    private fun toggle(policyId: String, enabled: Boolean) {
        if (policyId.isBlank()) return
        screenModelScope.launch {
            markInProgress(policyId, true)
            val res = togglePolicy(policyId, enabled)
            markInProgress(policyId, false)
            if (res is Outcome.Failure) emitFailure(res)
        }
    }

    /** [option] `null` — pauzani bekor qilish. */
    private fun pause(policyId: String, option: PauseOption?) {
        if (policyId.isBlank()) return
        screenModelScope.launch {
            _state.update { it.copy(pauseSheetFor = null) }
            markInProgress(policyId, true)

            val until = option?.until(Clock.System.now(), TimeZone.currentSystemDefault())
            val res = pausePolicy(policyId, until)

            markInProgress(policyId, false)
            if (res is Outcome.Failure) emitFailure(res)
        }
    }

    private fun markInProgress(policyId: String, busy: Boolean) {
        _state.update {
            it.copy(
                actionInProgress =
                    if (busy) it.actionInProgress + policyId else it.actionInProgress - policyId
            )
        }
    }

    private fun emitFailure(failure: Outcome.Failure) {
        val cause = failure.cause
        if (cause is ErrorCause.PremiumRequired) {
            _effect.trySend(PolicyListEffect.ShowPremium(cause.feature, failure))
        } else {
            _effect.trySend(PolicyListEffect.ShowError(failure))
        }
    }

    // ── Yuklashlar (o'zgarmadi) ───────────────────────────────
    private fun getPolicies() {
        policyJob?.cancel()
        policyJob = screenModelScope.launch {
            val childId = _state.value.selectedChild?.userId
            if (childId.isNullOrBlank()) return@launch
            launch { refreshQuickBlocks(childId) }

            if (!_state.value.isInitialLoadDone) {
                _state.update { it.copy(policyResponseState = ResponseState.Loading) }
            }

            when (val res = refreshPolicies(childId)) {
                is Outcome.Failure -> _state.update {
                    it.copy(
                        policyResponseState = ResponseState.Error(failure = res),
                        isInitialLoadDone = true,
                    )
                }

                is Outcome.Success -> _state.update {
                    it.copy(
                        policyResponseState = ResponseState.Success(),
                        isInitialLoadDone = true,
                    )
                }
            }
        }
    }

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
                    val children = res.data
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
                        )
                    }
                }
            }
        }
    }

    private fun getSubscriptionLimit() {
        screenModelScope.launch { refreshSubscriptionLimit() }
    }

    private fun refreshSubscriptionLimit() {
        _state.update { current ->
            val childId = current.selectedChild?.userId
            val limit = AppSettings.subscriptionLimitList.find { it.childId == childId }
                ?: SubscriptionLimit()
            current.copy(subscriptionLimit = limit)
        }
    }

    private fun loadPermissionStatus() {
        permissionJob?.cancel()
        permissionJob = screenModelScope.launch {
            when (val res = permissionStatusRepository.permissionStatus(
                childId = _state.value.selectedChild?.userId ?: "",
                state = PermissionStatusType.POLICY,
            )) {
                is Outcome.Success -> _state.update { it.copy(permissionIssueList = res.data) }
                is Outcome.Failure -> _state.update { it.copy(permissionIssueList = emptyList()) }
            }
        }
    }

    private fun removeQuickBlockFor(packageName: String) {
        val childId = _state.value.selectedChild?.userId
        if (childId.isNullOrBlank() || packageName.isBlank()) return

        val key = PolicyState.quickBlockKey(packageName)
        if (key in _state.value.actionInProgress) return

        screenModelScope.launch {
            markInProgress(key, true)
            val res = removeQuickBlock(childId, QuickBlockTarget.app(packageName))
            markInProgress(key, false)
            // ABSENT ham muvaffaqiyat; ro'yxat repozitoriy refresh() orqali o'zi yangilanadi.
            if (res is Outcome.Failure) emitFailure(res)
        }
    }

    /** Bir bola uchun bir marta; xato bo'lsa kartada paket nomi ko'rinadi. */
    private fun loadChildApps(childId: String) {
        if (childAppsLoadedFor == childId || childAppsJob?.isActive == true) return
        childAppsJob = screenModelScope.launch {
            val res = policyRepository.childApps(childId)
            if (res is Outcome.Success) {
                childAppsLoadedFor = childId
                _state.update { st ->
                    st.copy(childApps = res.data.map { it.toAppSelectionUi() }.associateBy { it.packageName })
                }
            }
        }
    }

    private companion object { const val TICK_INTERVAL_MS = 60_000L }
}