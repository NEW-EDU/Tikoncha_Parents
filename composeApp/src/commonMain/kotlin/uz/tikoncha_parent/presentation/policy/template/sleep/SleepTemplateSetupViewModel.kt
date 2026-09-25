package uz.tikoncha_parent.presentation.policy.template.sleep

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.uyqu_vaqti_rejasi
import uz.tikoncha_parent.domain.model.WeekDay
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.Patch
import uz.tikoncha_parent.domain.model.policy.PolicyConditions
import uz.tikoncha_parent.domain.model.policy.PolicyDraft
import uz.tikoncha_parent.domain.model.policy.PolicyPatch
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.model.policy.PolicyTargets
import uz.tikoncha_parent.domain.use_case.policy.CreatePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.DeletePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.UpdatePolicyUseCase
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedState
import uz.tikoncha_parent.presentation.policy.time_rule.TimeRuleUi
import uz.tikoncha_parent.presentation.policy.toCondition
import uz.tikoncha_parent.presentation.ui_state.ResponseState

class SleepTemplateSetupViewModel(
    private val createPolicy: CreatePolicyUseCase,
    private val updatePolicy: UpdatePolicyUseCase,
    private val deletePolicy: DeletePolicyUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow(SleepTemplateSetupState())
    val state = _state.asStateFlow()

    private val _effect = Channel<SleepTemplateSetupEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: SleepTemplateSetupEvent) {
        when (event) {
            is SleepTemplateSetupEvent.Init -> initState(event)
            is SleepTemplateSetupEvent.SetStartTime ->
                _state.update { it.copy(startTime = event.time) }
            is SleepTemplateSetupEvent.SetEndTime ->
                _state.update { it.copy(endTime = event.time) }
            is SleepTemplateSetupEvent.SetTimeRange ->
                _state.update { it.copy(startTime = event.start, endTime = event.end) }
            is SleepTemplateSetupEvent.Save -> save(event.sharedState)
            is SleepTemplateSetupEvent.Delete -> delete(event.policyId)
            SleepTemplateSetupEvent.ResetResponseState ->
                _state.update {
                    it.copy(
                        createState = ResponseState.Idle,
                        updateState = ResponseState.Idle,
                        deleteState = ResponseState.Idle,
                    )
                }
        }
    }

    private fun initState(event: SleepTemplateSetupEvent.Init) {
        if (_state.value.isInitialized) return

        val editing = event.sharedState.selectedPolicy

        if (editing != null) {
            val firstTimeRule = editing.timeRule.firstOrNull()
            _state.update {
                it.copy(
                    startTime = firstTimeRule?.startTime ?: it.startTime,
                    endTime = firstTimeRule?.endTime ?: it.endTime,
                    editingPolicy = editing,
                    isInitialized = true,
                )
            }
        } else {
            _state.update { it.copy(isInitialized = true) }
        }
    }

    private fun save(shared: PolicySharedState) {
        val s = _state.value

        if (s.intervalMinutes <= 0) {
            _effect.trySend(SleepTemplateSetupEffect.NoTimeIntervalSelectionToast)
            return
        }
        if (shared.selectedPkgs.isEmpty() &&
            shared.selectedCategories.isEmpty() &&
            shared.selectedSites.isEmpty()
        ) {
            _effect.trySend(SleepTemplateSetupEffect.NoAppWebSelectedToast)
            return
        }

        val timeRule = TimeRuleUi(
            id = 1,
            startTime = s.startTime,
            endTime = s.endTime,
            reverse = false,
            allDay = false,
            weekDays = WeekDay.entries.toSet(),
        )

        if (s.isEditMode) requestUpdate(shared, timeRule) else requestCreate(shared, timeRule)
    }

    private fun targetsOf(shared: PolicySharedState) = PolicyTargets(
        packages = shared.selectedPkgs.toList(),
        categories = shared.selectedCategories.toList(),
        sites = shared.selectedSites.toList(),
        features = shared.selectedFeatures.toList(),
        iosSelectionIds = shared.iosSelectionIds,
        packs = shared.packs,
    )

    private fun requestCreate(shared: PolicySharedState, timeRule: TimeRuleUi) {
        screenModelScope.launch {
            _state.update { it.copy(createState = ResponseState.Loading) }

            // Bo'sh nom serverda 422 beradi — shablon nomini tildan olamiz.
            val name = getString(Res.string.uyqu_vaqti_rejasi)

            val draft = PolicyDraft(
                name = name,
                // ALLOW qattiq yozilmaydi — ekran qaysi rejimni tanlasa o'sha ketadi.
                action = shared.policyAction,
                preset = PolicyPreset.SLEEP,
                targets = targetsOf(shared),
                conditions = PolicyConditions(time = listOf(timeRule.toCondition())),
            )

            when (val r = createPolicy(shared.selectedChild?.userId.orEmpty(), draft)) {
                is Outcome.Failure -> _state.update { it.copy(createState = ResponseState.Error(failure = r)) }
                is Outcome.Success -> _state.update { it.copy(createState = ResponseState.Success()) }
            }
        }
    }

    private fun requestUpdate(shared: PolicySharedState, timeRule: TimeRuleUi) {
        val policyId = _state.value.editingPolicy?.policyId.orEmpty()

        screenModelScope.launch {
            _state.update { it.copy(updateState = ResponseState.Loading) }

            val name = getString(Res.string.uyqu_vaqti_rejasi)

            val patch = PolicyPatch(
                name = Patch.Value(name),
                action = Patch.Value(shared.policyAction),
                targets = Patch.Value(targetsOf(shared)),
                conditions = Patch.Value(PolicyConditions(time = listOf(timeRule.toCondition()))),
            )

            when (val r = updatePolicy(policyId, patch)) {
                is Outcome.Failure -> _state.update { it.copy(updateState = ResponseState.Error(failure = r)) }
                is Outcome.Success -> _state.update { it.copy(updateState = ResponseState.Success()) }
            }
        }
    }

    private fun delete(policyId: String) {
        screenModelScope.launch {
            _state.update { it.copy(deleteState = ResponseState.Loading) }

            when (val r = deletePolicy(policyId)) {
                is Outcome.Failure -> _state.update { it.copy(deleteState = ResponseState.Error(failure = r)) }
                is Outcome.Success -> _state.update { it.copy(deleteState = ResponseState.Success()) }
            }
        }
    }
}