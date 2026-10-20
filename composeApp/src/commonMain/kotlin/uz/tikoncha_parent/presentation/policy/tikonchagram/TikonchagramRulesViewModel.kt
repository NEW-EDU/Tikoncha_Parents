package uz.tikoncha_parent.presentation.policy.tikonchagram

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.tikonchagram.TgPreset
import uz.tikoncha_parent.domain.model.tikonchagram.TgRules
import uz.tikoncha_parent.domain.repository.TikonchagramRepository

data class TikonchagramRulesState(
    val childId: String = "",
    val loaded: Boolean = false,
    /** Serverdan bir marta bo'lsa ham o'qildi — shundan keyingina tahrirlash mumkin. */
    val ready: Boolean = false,
    val rules: TgRules = TgRules.STRICT,
    val saving: Boolean = false,
    val error: Outcome.Failure? = null,
)

sealed interface TikonchagramRulesEvent {
    data class Init(val childId: String) : TikonchagramRulesEvent
    data class Changed(val rules: TgRules) : TikonchagramRulesEvent
    data class PresetSelected(val preset: TgPreset) : TikonchagramRulesEvent
    data object Retry : TikonchagramRulesEvent
    data object ErrorDismissed : TikonchagramRulesEvent
}

/**
 * Tikonchagram qoidalari: har o'zgarish darhol ko'rinadi va qisqa kutishdan keyin serverga
 * to'liq yoziladi (ketma-ket bosishlar bitta so'rovga yig'iladi). Xato bo'lsa — serverdagi holat qaytadi.
 */
class TikonchagramRulesViewModel(
    private val repository: TikonchagramRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(TikonchagramRulesState())
    val state = _state.asStateFlow()

    private var started = false
    private var saveJob: Job? = null

    fun onEvent(e: TikonchagramRulesEvent) {
        when (e) {
            is TikonchagramRulesEvent.Init -> start(e.childId)
            is TikonchagramRulesEvent.Changed -> change(e.rules)
            is TikonchagramRulesEvent.PresetSelected -> change(_state.value.rules.withPreset(e.preset))
            TikonchagramRulesEvent.Retry -> {
                _state.update { it.copy(loaded = false) }
                screenModelScope.launch { load() }
            }
            TikonchagramRulesEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun start(childId: String) {
        if (started) return
        started = true
        _state.update { it.copy(childId = childId) }
        screenModelScope.launch { load() }
    }

    private suspend fun load() {
        when (val r = repository.rules(_state.value.childId)) {
            is Outcome.Success -> _state.update { it.copy(loaded = true, ready = true, rules = r.data.rules) }
            is Outcome.Failure -> _state.update { it.copy(loaded = true, error = r) }
        }
    }

    private fun change(rules: TgRules) {
        val s = _state.value
        if (!s.ready || rules == s.rules) return
        _state.update { it.copy(rules = rules) }
        saveJob?.cancel()
        saveJob = screenModelScope.launch {
            delay(SAVE_DEBOUNCE_MS)
            _state.update { it.copy(saving = true) }
            when (val r = repository.save(s.childId, _state.value.rules)) {
                is Outcome.Success -> _state.update { it.copy(saving = false) }
                is Outcome.Failure -> {
                    _state.update { it.copy(saving = false, error = r) }
                    load()
                }
            }
        }
    }

    private companion object {
        const val SAVE_DEBOUNCE_MS = 600L
    }
}
