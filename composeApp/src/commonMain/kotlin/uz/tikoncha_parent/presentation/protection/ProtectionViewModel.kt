package uz.tikoncha_parent.presentation.protection

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.protection.AccountRequestAction
import uz.tikoncha_parent.domain.model.protection.AccountRequestStatus
import uz.tikoncha_parent.domain.model.protection.ChildMode
import uz.tikoncha_parent.domain.model.protection.ChildPermission
import uz.tikoncha_parent.domain.model.protection.ChildRequest
import uz.tikoncha_parent.domain.model.protection.ProtectionStatus
import uz.tikoncha_parent.domain.model.protection.StrictMethod
import uz.tikoncha_parent.domain.repository.ChildRepository
import uz.tikoncha_parent.domain.repository.ProtectionRepository
import uz.tikoncha_parent.presentation.ui_state.ResponseState
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ProtectionViewModel(
    private val protectionRepository: ProtectionRepository,
    private val childRepository: ChildRepository
) : ScreenModel {

    private val _state = MutableStateFlow(ProtectionState())
    val state = _state.asStateFlow()

    // ── Countdown alohida flow: har soniya yangilansa ham faqat
    //    bitta Text recompose bo'ladi, butun ekran emas ──
    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds = _remainingSeconds.asStateFlow()

    private var currentChildId: String? = null
    private var countdownJob: Job? = null
    private var childrenJob: Job? = null


    fun onEvent(event: ProtectionEvent) {
        when (event) {
            is ProtectionEvent.LoadStatus -> loadStatus(event.childId, silent = false)
            is ProtectionEvent.Refresh -> loadStatus(event.childId, silent = true)

            ProtectionEvent.ToggleCodeVisibility ->
                _state.update { it.copy(isCodeVisible = !it.isCodeVisible) }

            is ProtectionEvent.ApproveStrictRequest ->
                decideStrictRequest(event.requestId, approve = true)

            is ProtectionEvent.RejectStrictRequest ->
                decideStrictRequest(event.requestId, approve = false)

            is ProtectionEvent.AllowAccountRequest ->
                decideAccountRequest(event.action, allow = true)

            is ProtectionEvent.DenyAccountRequest ->
                decideAccountRequest(event.action, allow = false)

            ProtectionEvent.ActionErrorDismissed ->
                _state.update { it.copy(actionResponseState = ResponseState.Idle) }

            is ProtectionEvent.OnChildSelected -> {
                _state.update { it.copy(selectedChild = event.child) }
                AppSettings.selectedChildId = event.child.userId
                AppSettings.selectedChild = event.child
            }

            ProtectionEvent.GetChildren -> {
                loadChildren()
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
    // ─────────────────────── Status yuklash ───────────────────────
    private fun loadStatus(childId: String, silent: Boolean) {
        currentChildId = childId
        screenModelScope.launch(Dispatchers.IO) {
            if (silent) {
                _state.update { it.copy(isRefreshing = true) }
            } else {
                _state.update { it.copy(responseState = ResponseState.Loading) }
            }

            when (val res = protectionRepository.protectionStatus(childId)) {
                is Outcome.Failure -> _state.update {
                    it.copy(
                        isRefreshing = false,
                        responseState = ResponseState.Error(failure = res),
                    )
                }
                is Outcome.Success -> applyStatus(res.data)
            }
        }
    }

    private fun applyStatus(data: ProtectionStatus) {
        val mode = ChildMode.from(data.currentMode)
        val lastSync = parseInstant(data.lastSyncAt)

        _state.update {
            it.copy(
                responseState = ResponseState.Success(),
                isRefreshing = false,
                mode = mode,
                strictMethod = if (mode == ChildMode.STRICT) StrictMethod.from(data.strictMethod) else null,
                unlockData = data.unlockData,
                isCodeVisible = false,
                lastSyncAt = lastSync,
                isOnline = isOnline(lastSync),
                policiesUpToDate = data.policiesUpToDate,
                policiesSyncedAt = parseInstant(data.policiesSyncedAt),
                enabledPermissions = data.enabledKeys.mapNotNull { map -> ChildPermission.from(map) }.toSet(),
                disabledPermissions = data.disabledKeys.mapNotNull { map -> ChildPermission.from(map) }.toSet(),
                strictDisableRequest = data.strictDisableRequest,
                logoutRequest = data.logoutRequest,
                deleteRequest = data.deleteRequest,
            )
        }
        restartCountdown(data.strictDisableRequest)
    }

    // ─────────────── Qalqon o'chirish so'rovi (strict) ───────────────

    private fun decideStrictRequest(requestId: String, approve: Boolean) {
        if (_state.value.actionInProgressId != null) return

        screenModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(actionInProgressId = requestId) }

            val res = if (approve) {
                protectionRepository.approveStrictDisableRequest(requestId)
            } else {
                protectionRepository.rejectStrictDisableRequest(requestId)
            }

            when (res) {
                is Outcome.Failure -> _state.update {
                    it.copy(
                        actionInProgressId = null,
                        actionResponseState = ResponseState.Error(failure = res),
                    )
                }
                is Outcome.Success -> {
                    countdownJob?.cancel()
                    _remainingSeconds.value = 0
                    _state.update {
                        it.copy(
                            actionInProgressId = null,
                            strictDisableRequest = res.data
                        )
                    }
                }
            }
        }
    }

    // ───────── Hisobdan chiqish / ilovani o'chirish (account) ─────────

    private fun decideAccountRequest(action: AccountRequestAction, allow: Boolean) {
        if (_state.value.actionInProgressId != null) return

        val request = when (action) {
            AccountRequestAction.LOGOUT -> _state.value.logoutRequest
            AccountRequestAction.DELETE -> _state.value.deleteRequest
            AccountRequestAction.UNKNOWN -> null
        } ?: return
        val requestId = request.id ?: return

        screenModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(actionInProgressId = requestId) }

            val status = if (allow) AccountRequestStatus.ACCESS else AccountRequestStatus.DENY
            when (val res = protectionRepository.updateAccountRequestStatus(requestId, status)) {
                is Outcome.Failure -> _state.update {
                    it.copy(
                        actionInProgressId = null,
                        actionResponseState = ResponseState.Error(failure = res),
                    )
                }
                is Outcome.Success -> {
                    // Karta yo'qolmaydi — yangilangan status (access/deny) bilan qoladi.
                    // Keyingi refresh'da API qaytarmasa tabiiy o'chadi.
                    val updated = request.copy(status = status.value)
                    _state.update {
                        when (action) {
                            AccountRequestAction.LOGOUT ->
                                it.copy(actionInProgressId = null, logoutRequest = updated)
                            else ->
                                it.copy(actionInProgressId = null, deleteRequest = updated)
                        }
                    }
                }
            }
        }
    }

    // ─────────────────────── Countdown ───────────────────────

    private fun restartCountdown(request: ChildRequest?) {
        countdownJob?.cancel()

        val isPending = request?.status.equals("pending", ignoreCase = true)
        val expiresAt = parseInstant(request?.expiresAt)
        if (!isPending || expiresAt == null) {
            _remainingSeconds.value = 0
            return
        }

        countdownJob = screenModelScope.launch {
            while (true) {
                val remaining = (expiresAt - kotlin.time.Clock.System.now()).inWholeSeconds.toInt()
                if (remaining <= 0) {
                    _remainingSeconds.value = 0
                    currentChildId?.let { loadStatus(it, silent = true) }
                    return@launch
                }
                _remainingSeconds.value = remaining
                delay(1_000)
            }
        }
    }

    // ─────────────────────── Helpers ───────────────────────

    private fun parseInstant(iso: String?): Instant? {
        if (iso.isNullOrBlank()) return null
        runCatching { return Instant.parse(iso) }
        return runCatching {
            LocalDateTime.parse(iso).toInstant(TimeZone.UTC)
        }.getOrNull()
    }

    private fun isOnline(lastSync: Instant?): Boolean =
        lastSync != null && (kotlin.time.Clock.System.now() - lastSync) <= ONLINE_THRESHOLD

    override fun onDispose() {
        countdownJob?.cancel()
        super.onDispose()
    }

    companion object {
        private val ONLINE_THRESHOLD = 5.minutes
    }
}