package uz.tikoncha_parent.presentation.profile.subscription.subscription_payment

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.mapper.toSubscriptionPlanUi
import uz.tikoncha_parent.domain.model.SubscriptionLimit
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.PaymentRepository
import uz.tikoncha_parent.presentation.ui_state.ResponseState


class SubscriptionPaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val region: uz.tikoncha_parent.domain.repository.PaymentRegionRepository,
) : ScreenModel {

    companion object {
        private var cachedSubscription: SubscriptionUi? = null
    }

    private val _state =
        MutableStateFlow(SubscriptionPaymentState(subscriptionUi = cachedSubscription))
    val state = _state.asStateFlow()
    var limitJob: Job? = null

    init {
        requestSubscriptionPlans(showLoading = cachedSubscription == null)
        _state.update {
            it.copy(
                selectedChild = AppSettings.selectedChild,
                children = AppSettings.children
            )
        }
        getCurrentLimit()
        _state.update { it.copy(localPayments = region.localPayments.value) }
        screenModelScope.launch { _state.update { it.copy(localPayments = region.refresh()) } }
    }


    fun onEvent(event: SubscriptionPaymentEvent) {
        when (event) {
            SubscriptionPaymentEvent.ResetResponseState -> {
                _state.update {
                    it.copy(
                        subscriptionPlanState = ResponseState.Idle
                    )
                }
            }

            is SubscriptionPaymentEvent.SetSelectedChild -> {
                _state.update {
                    it.copy(
                        selectedChild = event.child
                    )
                }
                AppSettings.selectedChild = event.child
                getCurrentLimit()
            }
        }
    }


    private fun requestSubscriptionPlans(showLoading: Boolean = true) {
        screenModelScope.launch {
            if (showLoading) {
                _state.update { it.copy(subscriptionPlanState = ResponseState.Loading) }
            }

            when (val res = paymentRepository.subscriptionPlans()) {
                is Outcome.Failure -> {
                    if (showLoading) {
                        _state.update {
                            it.copy(subscriptionPlanState = ResponseState.Error(failure = res))
                        }
                    }
                }

                is Outcome.Success -> {
                    val data = res.data.firstOrNull()?.toSubscriptionPlanUi()
                    cachedSubscription = data
                    _state.update {
                        it.copy(
                            subscriptionUi = data,
                            subscriptionPlanState = ResponseState.Success()
                        )
                    }
                }
            }
        }
    }

    private fun getCurrentLimit() {
        limitJob?.cancel()
        limitJob = screenModelScope.launch {
            paymentRepository.syncSubscriptionLimits()
            _state.update { innerState ->
                val limit = AppSettings.subscriptionLimitList
                    .find { it.childId == state.value.selectedChild?.userId }
                    ?: SubscriptionLimit()
                innerState.copy(currentPlan = limit.subscriptionType)
            }
        }
    }
}