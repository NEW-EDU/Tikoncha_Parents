package uz.tikoncha_parent.presentation.profile.coin_purchase

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.remote.model.subscription.PromoCodeValidationRequest
import uz.tikoncha_parent.domain.model.PaymentStatus
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.subscription.PurchaseCoinRequest
import uz.tikoncha_parent.domain.repository.PaymentRepository

class CoinPurchaseViewModel (
    private val paymentRepository: PaymentRepository
): ScreenModel {

    private val _state = MutableStateFlow(CoinPurchaseState())
    val state = _state.asStateFlow()


    private val _effect = MutableSharedFlow<CoinPurchaseEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    fun onEvent(event: CoinPurchaseEvent){
        when(event){
            is CoinPurchaseEvent.ApplyPromoCode -> {
                _state.update {
                    it.copy(
                        promoCode = event.promoCode.filterNot { c -> c.isWhitespace() }.uppercase()
                    )
                }
                validatePromoCode()
            }
            is CoinPurchaseEvent.SetupData -> {
                _state.update {
                    it.copy(
                        coins = event.coins,
                        totalPrice = event.totalPrice,
                        discountPrice = event.discountPrice
                    )
                }
            }

            CoinPurchaseEvent.StartPayment -> {
                requestPayment()
            }
        }
    }

    private var promoCodeJob: Job? = null

    private fun validatePromoCode(){
        promoCodeJob?.cancel()
        promoCodeJob = screenModelScope.launch {
            _state.update {
                it.copy(
                    promoCodeLoading = true,
                )
            }
            val request = PromoCodeValidationRequest(
                code = _state.value.promoCode,
                amount = _state.value.totalPrice - _state.value.discountPrice,
                plan_duration = null
            )
            when (val res = paymentRepository.promoCodeValidation(request)) {
                is Outcome.Failure -> {
                    _state.update {
                        it.copy(
                            promoCodeLoading = false,
                            promoActivated = false,
                        )
                    }
                    _effect.tryEmit(CoinPurchaseEffect.ShowPromoCodeErrorToast(res))
                }

                is Outcome.Success -> {
                    _state.update {
                        it.copy(
                            promoActivated = true,
                            promoCodeLoading = false,
                            promoCodeDiscountPrice = res.data.savings,
                        )
                    }
                    _effect.tryEmit(CoinPurchaseEffect.ShowPromoCodeSuccessToast)
                }
            }
        }
    }

    private fun requestPayment(){
        screenModelScope.launch {
            _state.update {
                it.copy(
                    paymentLoading = true,
                    paymentActive = true
                )
            }
            val request = PurchaseCoinRequest(
                coins = _state.value.coins
            )
            when (val res = paymentRepository.purchaseCoin(request)) {
                is Outcome.Failure -> {
                    _effect.tryEmit(CoinPurchaseEffect.PaymentFailed(res))
                    _state.update {
                        it.copy(
                            paymentLoading = false,
                            paymentActive = false
                        )
                    }
                }

                is Outcome.Success -> {
                    _state.update {
                        it.copy(
                            paymentLoading = false,
                            merchantTransId = res.data.merchant_trans_id,
                            serviceId = res.data.service_id
                        )
                    }

                    _effect.tryEmit(
                        CoinPurchaseEffect.OpenClickPayment(
                            serviceId = res.data.service_id,
                            merchantId = _state.value.merchantId,
                            amount = _state.value.finalPrice,
                            transactionId = res.data.merchant_trans_id
                        )
                    )
                    requestPaymentStatus()
                }
            }
        }
    }

    var statusJob: Job? = null
    private fun requestPaymentStatus(){
        statusJob?.cancel()

        val merchantTransId = _state.value.merchantTransId
        if (merchantTransId.isBlank()) return

        statusJob = screenModelScope.launch {

            _state.update {
                it.copy(
                    paymentStatus = PaymentStatus.PENDING
                )
            }

            repeat(120) {
                when (val res = paymentRepository.paymentStatus(_state.value.merchantTransId)) {
                    is Outcome.Failure -> {
                        _state.update { it.copy(paymentStatusError = res) }
                    }

                    is Outcome.Success -> {
                        val status = res.data

                        _state.update { it.copy(paymentStatus = status) }

                        if (status == PaymentStatus.COMPLETED) {
                            _effect.tryEmit(CoinPurchaseEffect.PaymentSuccess)
                        }

                        if (status == PaymentStatus.COMPLETED || status == PaymentStatus.FAILED) {
                            _state.update { it.copy(paymentActive = false) }
                            return@launch
                        }
                    }
                }
                delay(1000)
            }
        }
    }
}