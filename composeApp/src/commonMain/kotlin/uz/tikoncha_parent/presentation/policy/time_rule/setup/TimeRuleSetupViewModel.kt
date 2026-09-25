package uz.tikoncha_parent.presentation.policy.time_rule.setup

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import uz.tikoncha_parent.presentation.policy.common.buildWeekdayChips
import uz.tikoncha_parent.presentation.policy.common.occupiedTimeDays
import uz.tikoncha_parent.presentation.policy.common.selectedDays
import uz.tikoncha_parent.presentation.policy.common.toggleDay
import uz.tikoncha_parent.presentation.policy.time_rule.TimeRuleUi
import uz.tikoncha_parent.presentation.policy.time_rule.setup.TimeRuleSetupState.Companion.DEFAULT_END_TIME
import uz.tikoncha_parent.presentation.policy.time_rule.setup.TimeRuleSetupState.Companion.DEFAULT_START_TIME

class TimeRuleSetupViewModel : ScreenModel {

    private val _state = MutableStateFlow(TimeRuleSetupState())
    val state = _state.asStateFlow()

    private val _effect = Channel<TimeRuleSetupEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: TimeRuleSetupEvent) {
        when (event) {
            is TimeRuleSetupEvent.Init -> handleInit(event)
            is TimeRuleSetupEvent.SelectDay -> _state.update {
                it.copy(weekDays = it.weekDays.toggleDay(event.day))
            }
            is TimeRuleSetupEvent.SetStartTime -> _state.update { it.copy(startTime = event.time) }
            is TimeRuleSetupEvent.SetEndTime -> _state.update { it.copy(endTime = event.time) }
            is TimeRuleSetupEvent.SetTimeRange -> _state.update {
                it.copy(startTime = event.start, endTime = event.end)
            }
            is TimeRuleSetupEvent.SetAllDay -> {
                _state.update {
                    it.copy(
                        reverse = false,
                        allDay = event.allDay,
                        startTime = if (event.allDay) LocalTime(0,0) else DEFAULT_START_TIME,
                        endTime = if (event.allDay) LocalTime(23,59) else DEFAULT_END_TIME
                    )
                }
            }
            is TimeRuleSetupEvent.SetReverse -> _state.update { it.copy(reverse = event.outside) }
            TimeRuleSetupEvent.Save -> save()
        }
    }

    private fun handleInit(event: TimeRuleSetupEvent.Init) {
        if (_state.value.isInitialized) return

        val editing = event.editingRule
        val occupied = occupiedTimeDays(
            rules = event.allTimeRules,
            excludeId = editing?.id,
        )

        _state.update {
            TimeRuleSetupState(
                editingId = editing?.id,
                weekDays = buildWeekdayChips(
                    otherRuleDays = occupied,
                    currentSelected = editing?.weekDays.orEmpty(),
                ),
                startTime = editing?.startTime ?: LocalTime(8, 0),
                endTime = editing?.endTime ?: LocalTime(12, 0),
                allDay = editing?.allDay ?: false,
                reverse = editing?.reverse ?: false,
                isInitialized = true,
            )
        }
    }

    private fun save() {
        val s = _state.value

        if (!s.hasAnyDaySelected){
            _effect.trySend(TimeRuleSetupEffect.NoDaySelectionToast)
            return
        }
        if (!s.allDay && s.intervalMinutes <= 0){
            _effect.trySend(TimeRuleSetupEffect.NoTimeIntervalSelectionToast)
            return
        }


        val rule = buildRule(s)
        screenModelScope.launch {
            _effect.send(TimeRuleSetupEffect.Saved(rule))
        }
    }

    private fun buildRule(s: TimeRuleSetupState): TimeRuleUi {
        val selected = s.weekDays.selectedDays()
        return TimeRuleUi(
            // id = 0 → create signal. Edit bo'lsa mavjud id.
            // Yakuniy id PolicySharedModel.upsertTimeRule ichida beriladi.
            id = s.editingId ?: 0,
            startTime = s.startTime,
            endTime = s.endTime,
            reverse = s.reverse,
            allDay = s.allDay,
            weekDays = selected,
        )
    }

    /**
     * Create rejimda yangi id hosil qiladi.
     * NOTE: mavjud rulelardan max+1 qilish Init event ichida bilmaganimiz uchun
     * hozircha timestamp asosida. Agar siz id ni shared tarafda upsert paytida
     * hal qilishni istasangiz, TimeRuleUi.id ni Int? qilib aytishingiz kerak.
     */

    private fun LocalTime.hhmm(): String {
        val hh = hour.toString().padStart(2, '0')
        val mm = minute.toString().padStart(2, '0')
        return "$hh:$mm"
    }
}