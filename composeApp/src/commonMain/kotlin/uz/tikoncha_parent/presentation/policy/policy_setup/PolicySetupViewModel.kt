package uz.tikoncha_parent.presentation.policy.policy_setup

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.policy.PolicyPreset
import uz.tikoncha_parent.domain.use_case.policy.CreatePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.DeletePolicyUseCase
import uz.tikoncha_parent.domain.use_case.policy.UpdatePolicyUseCase
import uz.tikoncha_parent.presentation.policy.shared.PolicySharedState
import uz.tikoncha_parent.presentation.policy.toDraft
import uz.tikoncha_parent.presentation.policy.toPatch
import uz.tikoncha_parent.presentation.ui_state.ResponseState

class PolicySetupViewModel(
    private val createPolicy: CreatePolicyUseCase,
    private val updatePolicy: UpdatePolicyUseCase,
    private val deletePolicy: DeletePolicyUseCase,
) : ScreenModel {

    private val _state = MutableStateFlow(PolicySetupState())
    val state = _state.asStateFlow()

    private val _effect = Channel<PolicySetupEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: PolicySetupEvent) {
        when (event) {
            is PolicySetupEvent.SavePolicy -> {
                val s = event.sharedState

                if (s.policyTitle.isEmpty()) {
                    _effect.trySend(PolicySetupEffect.NoTitleToast)
                    return
                }
                if (s.timeList.isEmpty() && s.limitList.isEmpty() && s.locationRule == null) {
                    _effect.trySend(PolicySetupEffect.NoRuleSelectedToast)
                    return
                }
                if (s.selectedCategories.isEmpty() &&
                    s.selectedPkgs.isEmpty() &&
                    s.selectedSites.isEmpty() &&
                    s.selectedFeatures.isEmpty()
                ) {
                    _effect.trySend(PolicySetupEffect.NoAppWebSelectedToast)
                    return
                }

                if (s.isEditMode) requestUpdate(s) else requestCreate(s)
            }

            is PolicySetupEvent.DeletePolicy -> requestDelete(event.policyId)

            PolicySetupEvent.ResetResponseState -> _state.update { PolicySetupState() }
        }
    }

    private fun requestCreate(shared: PolicySharedState) {
        screenModelScope.launch {
            _state.update { it.copy(createState = ResponseState.Loading) }

            val childId = shared.selectedChild?.userId.orEmpty()
            val draft = shared.toDraft().let { draft ->
                // Faqat limit bor, vaqt/lokatsiya yo'q — bu "Ilova taymeri".
                if (draft.preset == null &&
                    shared.limitList.isNotEmpty() &&
                    shared.timeList.isEmpty() &&
                    shared.locationRule == null
                ) draft.copy(preset = PolicyPreset.APP_LIMIT) else draft
            }

            when (val res = createPolicy(childId, draft)) {
                is Outcome.Failure -> _state.update { it.copy(createState = ResponseState.Error(failure = res)) }
                is Outcome.Success -> _state.update { it.copy(createState = ResponseState.Success()) }
            }
        }
    }

    private fun requestUpdate(shared: PolicySharedState) {
        screenModelScope.launch {
            val policyId = shared.selectedPolicy?.policyId.orEmpty()
            val patch = shared.toPatch()

            // Bo'sh PATCH serverga yuborilmaydi. "Tahrirlandi" deyish ham noto'g'ri —
            // ekranda qolib, "Hech narsa o'zgarmadi" xabari ko'rsatiladi.
            if (patch.isEmpty) {
                _effect.trySend(PolicySetupEffect.NothingChangedToast)
                return@launch
            }

            _state.update { it.copy(updateState = ResponseState.Loading) }

            when (val res = updatePolicy(policyId, patch)) {
                is Outcome.Failure -> _state.update { it.copy(updateState = ResponseState.Error(failure = res)) }
                is Outcome.Success -> _state.update { it.copy(updateState = ResponseState.Success()) }
            }
        }
    }

    private fun requestDelete(policyId: String) {
        screenModelScope.launch {
            _state.update { it.copy(deleteState = ResponseState.Loading) }

            when (val res = deletePolicy(policyId)) {
                is Outcome.Failure -> _state.update { it.copy(deleteState = ResponseState.Error(failure = res)) }
                is Outcome.Success -> _state.update { it.copy(deleteState = ResponseState.Success()) }
            }
        }
    }
}