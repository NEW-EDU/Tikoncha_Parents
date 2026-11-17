package uz.tikoncha_parent.presentation.policy.special

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.safety.SafetySwitch
import uz.tikoncha_parent.domain.model.safety.SafetySwitches
import uz.tikoncha_parent.domain.repository.SafetyRepository
import uz.tikoncha_parent.domain.use_case.policy.ChildPaidStatusUseCase
import uz.tikoncha_parent.presentation.policy.components.PolicyScreenId
import uz.tikoncha_parent.presentation.policy.components.PolicyToast
import uz.tikoncha_parent.presentation.policy.components.PolicyToasts

/**
 * Maxsus himoya — ota-ona farzandi uçun yoqadi (`PUT /safety/settings`). Yoqilgani farzand telefonida qulflangan
 * (u öçira olmaydi), xavf bölsa ota-onaga faqat toifa va ilova nomi keladi. PLUS himoyalar PLUS'siz yoqilmaydi.
 */
data class SpecialProtectionState(
    val childId: String = "",
    val loaded: Boolean = false,
    val switches: SafetySwitches = SafetySwitches(),
    val busy: SafetySwitch? = null,
    val paid: Boolean? = null,
    val showPayWall: Boolean = false,
    /** Holatni yuklab bölmadi — switch'lar örniga "Qayta uriniş" (standart holatlar haqiqiydek körinmasin). */
    val loadError: Outcome.Failure? = null,
    val error: Outcome.Failure? = null,
) {
    fun isOn(s: SafetySwitch) = when (s) {
        SafetySwitch.DANGER_ZONES -> switches.dangerZones
        SafetySwitch.IMAGE -> switches.image
        SafetySwitch.CONVERSATION -> switches.conversation
        SafetySwitch.INCOGNITO -> switches.incognito
    }
}

class SpecialProtectionViewModel(
    private val repository: SafetyRepository,
    private val paidStatus: ChildPaidStatusUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow(SpecialProtectionState())
    val state = _state.asStateFlow()
    private var started = false

    fun start(childId: String) {
        if (started) return
        started = true
        _state.update { it.copy(childId = childId, paid = paidStatus(childId)) }
        screenModelScope.launch { load() }
        screenModelScope.launch {
            val paid = paidStatus.refresh(childId)
            _state.update { it.copy(paid = paid ?: it.paid) }
        }
    }

    private suspend fun load() {
        when (val r = repository.switches(_state.value.childId)) {
            is Outcome.Success -> _state.update { it.copy(loaded = true, loadError = null, switches = r.data) }
            is Outcome.Failure -> _state.update { it.copy(loaded = true, loadError = r) }
        }
    }

    fun retry() {
        if (!_state.value.loaded) return
        _state.update { it.copy(loaded = false, loadError = null) }
        screenModelScope.launch { load() }
    }

    fun toggle(switch: SafetySwitch, on: Boolean) {
        val s = _state.value
        if (s.busy != null || !s.loaded || s.loadError != null) return
        if (on && switch.plusOnly && s.paid == false) {
            _state.update { it.copy(showPayWall = true) }
            return
        }
        screenModelScope.launch {
            _state.update { it.copy(busy = switch) }
            when (val r = repository.setSwitch(s.childId, switch, on)) {
                is Outcome.Success -> {
                    _state.update { it.copy(busy = null, switches = r.data) }
                    PolicyToasts.post(PolicyToast.Saved, PolicyScreenId.SPECIAL)
                }
                is Outcome.Failure -> _state.update { it.copy(busy = null, error = r) }
            }
        }
    }

    fun dismissPayWall() = _state.update { it.copy(showPayWall = false) }
    fun dismissError() = _state.update { it.copy(error = null) }
}
