package uz.tikoncha_parent.presentation.profile.subscription.payment

import uz.tikoncha_parent.platform.isIos
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tikoncha_parents.composeapp.generated.resources.Res
import tikoncha_parents.composeapp.generated.resources.tolov_amalga_oshmadi
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.remote.model.SubscriptionPaymentRequest
import uz.tikoncha_parent.data.remote.model.subscription.PromoCodeValidationRequest
import uz.tikoncha_parent.domain.model.PaymentStatus
import uz.tikoncha_parent.domain.model.PurchaseResult
import uz.tikoncha_parent.domain.model.SubscriptionDuration
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.PaymentRepository
import uz.tikoncha_parent.domain.service.PaymentService
import uz.tikoncha_parent.domain.use_case.payment.PurchaseIApPremiumUseCase
import uz.tikoncha_parent.presentation.ui_state.ResponseState

class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val purchaseIApPremiumUseCase: PurchaseIApPremiumUseCase,
    private val paymentService: PaymentService
): ScreenModel {

    private val _state = MutableStateFlow(PaymentState())
    val state = _state.asStateFlow()

    init {
        // Apple ko'rigi uchun raqam (+99811…): iPhone'da faqat App Store — u oldindan tanlanadi
        val appStoreOnly = isIos() && AppSettings.isTestAccount
        _state.update {
            it.copy(
                isTestAccount = AppSettings.isTestAccount,
                selectedPaymentType = if (appStoreOnly) PaymentType.AppStore else it.selectedPaymentType,
            )
        }
    }

    fun onEvent(event: PaymentEvent){
        when(event){
            PaymentEvent.Pay -> {
                if (AppSettings.selectedChild == null) {
                    _state.update {
                        it.copy(
                            showSubscribeChildSheet = true
                        )
                    }
                    return
                } else {
                    when (state.value.selectedPaymentType) {
                        PaymentType.Click -> {
                            requestPayment()
                        }

                        PaymentType.AppStore -> {
                            requestApplyPay()
                        }

                        null -> {

                        }
                    }
                }
            }

            PaymentEvent.ResetPaymentResponse -> {
                _state.update {
                    it.copy(
                        paymentResponseState = ResponseState.Idle,
                        applePaymentResponseState = ResponseState.Idle
                    )
                }
            }
            is PaymentEvent.SetAmount -> {
                _state.update {
                    it.copy(
                        originalAmount = event.amount,
                        finalAmount = event.amount
                    )
                }

            }

            is PaymentEvent.SetSubscriptionDuration -> {
                _state.update {
                    it.copy(
                        subscriptionDuration = event.duration
                    )
                }
            }

            is PaymentEvent.SetPlanId -> {
                _state.update {
                    it.copy(
                        planId = event.planId
                    )
                }
            }

            is PaymentEvent.SetPaymentType -> {
                _state.update {
                    it.copy(
                        selectedPaymentType = event.type
                    )
                }
            }

            is PaymentEvent.OnPromoCode -> {
                _state.update {
                    it.copy(
                        promoCode = event.promoCode.filterNot { c -> c.isWhitespace() }.uppercase()
                    )
                }
            }

            PaymentEvent.ValidatePromoCode -> {
                validatePromoCode()
            }
            PaymentEvent.ClearPromoCodeResponse -> {
                _state.update {
                    it.copy(
                        promoCodeResponseState = ResponseState.Idle
                    )
                }
            }

            PaymentEvent.DismissChildSelectionSheet -> {
                _state.update {
                    it.copy(
                        showSubscribeChildSheet = false
                    )
                }
            }

            is PaymentEvent.PayWithChildPhone -> {
                when (state.value.selectedPaymentType) {
                    PaymentType.Click -> {
                        requestPayment(phone = "+998${event.phone}")
                    }

                    PaymentType.AppStore -> {
                        requestApplyPay()
                    }

                    null -> {

                    }
                }
            }
        }
    }

    private fun requestApplyPay(){
        screenModelScope.launch {
            val productId = if (state.value.subscriptionDuration == SubscriptionDuration.MONTHLY){
                "tikoncha.parent.monthly.v2"
            }
            else{
                "tikoncha.parent.yearly.v2"
            }

            when(val result = purchaseIApPremiumUseCase.invoke(productId)){
                PurchaseResult.Cancelled -> {
                    _state.update {
                        it.copy(
                            applePaymentResponseState = ResponseState.Error(
                                res = Res.string.tolov_amalga_oshmadi
                            )
                        )
                    }
                }
                is PurchaseResult.Error -> {
                    _state.update {
                        it.copy(
                            applePaymentResponseState = ResponseState.Error(
                                message = result.message
                            )
                        )
                    }
                }
                PurchaseResult.Pending -> {
                    _state.update {
                        it.copy(
                            applePaymentResponseState = ResponseState.Loading
                        )
                    }
                }
                PurchaseResult.Success -> {

                    AppSettings.setUserSubscription(
                        phone = AppSettings.selectedChild?.phoneNumber?:"",
                        isSubscribed = true
                    )

                    _state.update {
                        it.copy(
                            applePaymentResponseState = ResponseState.Success()
                        )
                    }
                }
            }
        }
    }
    private fun requestPayment(phone: String? = null){
        screenModelScope.launch {
            _state.update {
                it.copy(
                    paymentResponseState = ResponseState.Loading
                )
            }
            val request = SubscriptionPaymentRequest(
                plan_id = _state.value.planId,
                plan_duration = _state.value.subscriptionDuration.name,
                child_user_id = AppSettings.selectedChild?.userId,
                child_phone = phone,
                promocode_code = _state.value.promoCode
            )
            when (val res = paymentRepository.subscriptionPayment(request)) {
                is Outcome.Failure -> {
                    _state.update {
                        it.copy(paymentResponseState = ResponseState.Error(failure = res))
                    }
                }

                is Outcome.Success -> {
                    _state.update {
                        it.copy(
                            paymentResponseState = ResponseState.Success(),
                            merchantTransId = res.data.merchant_trans_id,
                            serviceId = res.data.service_id,
                            finalAmount = res.data.amount,
                            showSubscribeChildSheet = false
                        )
                    }

                    openClickPayment(
                        serviceId = _state.value.serviceId.toString(),
                        merchantId = _state.value.merchantId.toString(),
                        amount = _state.value.finalAmount.toString(),
                        transactionId = _state.value.merchantTransId
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
                            requestSubscriptionLimit()
                        }

                        if (status == PaymentStatus.COMPLETED || status == PaymentStatus.FAILED) {
                            return@launch
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    private fun requestSubscriptionLimit(){
        screenModelScope.launch {
            paymentRepository.syncSubscriptionLimits()
        }
    }
    private fun openClickPayment(
        serviceId: String,
        merchantId: String,
        amount: String,
        transactionId: String
    ) {


        paymentService.openClickPayment(
            serviceId = serviceId,
            merchantId = merchantId,
            amount = amount,
            transactionId = transactionId

        )
    }


    private var promoCodeJob: Job? = null
    private fun validatePromoCode(){
        promoCodeJob?.cancel()
        promoCodeJob = screenModelScope.launch {
            _state.update {
                it.copy(
                    promoCodeResponseState = ResponseState.Loading
                )
            }
            val request = PromoCodeValidationRequest(
                code = _state.value.promoCode,
                amount = _state.value.originalAmount,
                plan_duration = _state.value.subscriptionDuration.name
            )

            when (val res = paymentRepository.promoCodeValidation(request)) {
                is Outcome.Failure -> {
                    _state.update {
                        it.copy(
                            promoCodeResponseState = ResponseState.Error(failure = res),
                            promoActivated = false
                        )
                    }
                }

                is Outcome.Success -> {
                    _state.update {
                        it.copy(
                            promoActivated = true,
                            promoCodeResponseState = ResponseState.Success(),
                            originalAmount = res.data.original_amount,
                            discountAmount = res.data.discounted_amount,
                            discountPercentage = res.data.discount_percentage,
                            discountSaving = res.data.savings,
                            finalAmount = res.data.discounted_amount
                        )
                    }
                }
            }
        }
    }


}

