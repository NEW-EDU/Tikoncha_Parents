package uz.tikoncha_parent.presentation.policy.quick

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.domain.model.app_error.ErrorCause
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.QuickBlockTarget
import uz.tikoncha_parent.domain.use_case.policy.AddQuickBlockAppsUseCase
import uz.tikoncha_parent.domain.use_case.policy.ChildPaidStatusUseCase
import uz.tikoncha_parent.domain.use_case.policy.GetBlockableChildAppsUseCase
import uz.tikoncha_parent.domain.use_case.policy.ObserveQuickBlockSnapshotUseCase
import uz.tikoncha_parent.domain.use_case.policy.RefreshQuickBlocksUseCase
import uz.tikoncha_parent.domain.use_case.policy.RemoveQuickBlockUseCase
import uz.tikoncha_parent.domain.use_case.policy.UpdateOwnQuickBlockUseCase
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * "Tezkor blok" shabloni: shart yo'q — faqat doim yopiq turadigan ilovalar ro'yxati.
 * Statistikadagi qulf ham aynan shu ro'yxatga yozadi. Saqlash tugmasi yo'q: server
 * ro'yxatni faqat quick-block endpointlari orqali o'zgartiradi, har amal darhol ketadi.
 */
class QuickBlockViewModel(
    private val observeSnapshot: ObserveQuickBlockSnapshotUseCase,
    private val refreshQuickBlocks: RefreshQuickBlocksUseCase,
    private val updateOwn: UpdateOwnQuickBlockUseCase,
    private val addApps: AddQuickBlockAppsUseCase,
    private val removeQuickBlock: RemoveQuickBlockUseCase,
    private val getApps: GetBlockableChildAppsUseCase,
    private val childPaidStatus: ChildPaidStatusUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow(QuickBlockState())
    val state = _state.asStateFlow()

    private var observeJob: Job? = null

    init {
        screenModelScope.launch {
            while (isActive) {
                delay(MINUTE_MS)
                _state.update { it.copy(now = Clock.System.now()) }
            }
        }
    }

    fun onEvent(event: QuickBlockEvent) {
        when (event) {
            is QuickBlockEvent.Load -> load(event.childId)
            QuickBlockEvent.Resumed -> _state.value.childId.takeIf { it.isNotBlank() }?.let { refresh(it) }

            is QuickBlockEvent.EnabledToggled -> setEnabled(event.enabled)
            QuickBlockEvent.PauseClicked -> _state.update { it.copy(showPauseSheet = true) }
            QuickBlockEvent.PauseDismissed -> _state.update { it.copy(showPauseSheet = false) }
            is QuickBlockEvent.PauseSelected ->
                pause(event.option.until(Clock.System.now(), TimeZone.currentSystemDefault()))
            QuickBlockEvent.ResumeClicked -> pause(until = null)

            QuickBlockEvent.AddClicked -> _state.update {
                // Qo'shish — Plus (olib tashlash doim bepul). Tarif noma'lum bo'lsa server hal qiladi.
                if (it.quick.paid == false) it.copy(showPaywall = true) else it.copy(showAppsSheet = true)
            }
            is QuickBlockEvent.AppsAdded -> add(event.packages)
            QuickBlockEvent.SheetDismissed -> _state.update { it.copy(showAppsSheet = false) }
            is QuickBlockEvent.AppRemoved -> remove(event.packageName)

            QuickBlockEvent.PaywallDismissed -> _state.update { it.copy(showPaywall = false) }
            QuickBlockEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun load(childId: String) {
        if (childId.isBlank()) return
        if (_state.value.childId != childId) {
            _state.update { QuickBlockState(childId = childId) }
            observeJob?.cancel()
            observeJob = screenModelScope.launch {
                observeSnapshot(childId, AppSettings.userId).collect { snapshot ->
                    _state.update { it.copy(quick = snapshot.copy(paid = it.quick.paid), loaded = true, now = Clock.System.now()) }
                }
            }
            screenModelScope.launch {
                when (val res = getApps(childId)) {
                    is Outcome.Success -> _state.update { it.copy(childApps = res.data) }
                    is Outcome.Failure -> Unit     // nomlar o'rniga paket ko'rinadi; varaqda qayta urinadi
                }
            }
        }
        refresh(childId)
    }

    private fun refresh(childId: String) {
        _state.update { it.copy(quick = it.quick.copy(paid = childPaidStatus(childId))) }
        screenModelScope.launch {
            val res = refreshQuickBlocks(childId)
            if (res is Outcome.Failure && !_state.value.loaded) _state.update { it.copy(loaded = true, error = res) }
        }
        screenModelScope.launch {
            val paid = childPaidStatus.refresh(childId)
            _state.update { it.copy(quick = it.quick.copy(paid = paid)) }
        }
    }

    private fun setEnabled(enabled: Boolean) {
        val s = _state.value
        val own = s.own ?: return
        if (s.pendingEnabled != null) return
        _state.update { it.copy(pendingEnabled = enabled) }
        screenModelScope.launch {
            val res = updateOwn.setEnabled(s.childId, own, enabled)
            _state.update { it.copy(pendingEnabled = null, error = (res as? Outcome.Failure) ?: it.error) }
        }
    }

    private fun pause(until: Instant?) {
        val s = _state.value
        val own = s.own ?: return
        _state.update { it.copy(showPauseSheet = false, pausing = true) }
        screenModelScope.launch {
            val res = updateOwn.pause(s.childId, own, until)
            _state.update { it.copy(pausing = false, error = (res as? Outcome.Failure) ?: it.error) }
        }
    }

    private fun add(packages: Set<String>) {
        val childId = _state.value.childId
        _state.update { it.copy(showAppsSheet = false) }
        if (packages.isEmpty() || childId.isBlank()) return
        _state.update { it.copy(adding = true) }
        screenModelScope.launch {
            val res = addApps(childId, packages)
            _state.update {
                when {
                    res !is Outcome.Failure -> it.copy(adding = false)
                    // Lokal tarif eskirgan — server bepul dedi
                    res.cause is ErrorCause.PremiumRequired ->
                        it.copy(adding = false, showPaywall = true, quick = it.quick.copy(paid = false))
                    else -> it.copy(adding = false, error = res)
                }
            }
        }
    }

    private fun remove(packageName: String) {
        val childId = _state.value.childId
        if (childId.isBlank() || packageName in _state.value.removing) return
        _state.update { it.copy(removing = it.removing + packageName) }
        screenModelScope.launch {
            val res = removeQuickBlock(childId, QuickBlockTarget.app(packageName))
            _state.update {
                it.copy(removing = it.removing - packageName, error = (res as? Outcome.Failure) ?: it.error)
            }
        }
    }

    private companion object { const val MINUTE_MS = 60_000L }
}
