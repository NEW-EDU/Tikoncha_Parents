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
    private val paymentRepository: PaymentRepository,
    private val autopay: uz.tikoncha_parent.domain.repository.AutopayRepository,
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
                val s = _state.value
                when {
                    s.method == CoinPayMethod.CARD && s.cardAvailable ->
                        if (s.card == null) _effect.tryEmit(CoinPurchaseEffect.OpenAddCard) else payByCard()
                    else -> requestPayment()
                }
            }

            is CoinPurchaseEvent.SelectMethod -> _state.update { it.copy(method = event.method) }

            CoinPurchaseEvent.Resumed -> if (!_state.value.paymentActive) loadCards()
        }
    }

    /** Kartalar: ro'yxat kelsa — karta orqali tölov ochiq (server yopiq bölsa tölovda aniq xato chiqadi).
     *  Karta bor (yoki yangi qöşilgan) bölsa — u oldindan tanlanadi. */
    private fun loadCards() {
        screenModelScope.launch {
            val r = autopay.cards()
            val cards = (r as? Outcome.Success)?.data.orEmpty()
            _state.update {
                it.copy(
                    cardAvailable = r is Outcome.Success,
                    cards = cards,
                    method = if (cards.isNotEmpty() && (it.cards.isEmpty() || it.method == CoinPayMethod.CARD)) CoinPayMethod.CARD else it.method,
                )
            }
        }
    }

    /** Saqlangan kartadan bir marta yeçiladi. Natija nomaʼlum bölsa — qayta tölanmaydi, holat sörab turiladi. */
    private fun payByCard() {
        val s = _state.value
        if (s.paymentActive) return
        _state.update { it.copy(paymentLoading = true, paymentActive = true, paymentStatus = PaymentStatus.PENDING) }
        screenModelScope.launch {
            when (val r = autopay.buyCoins(s.coins, s.card?.id, s.promoCode.takeIf { s.promoActivated })) {
                is Outcome.Failure -> {
                    _state.update { it.copy(paymentLoading = false, paymentActive = false, paymentStatus = PaymentStatus.START) }
                    _effect.tryEmit(CoinPurchaseEffect.PaymentFailed(r))
                }
                is Outcome.Success -> when (r.data.status) {
                    "paid" -> {
                        _state.update { it.copy(paymentLoading = false, paymentActive = false, paymentStatus = PaymentStatus.COMPLETED) }
                        _effect.tryEmit(CoinPurchaseEffect.PaymentSuccess)
                    }
                    "failed" -> {
                        _state.update { it.copy(paymentLoading = false, paymentActive = false, paymentStatus = PaymentStatus.START) }
                        _effect.tryEmit(CoinPurchaseEffect.CardPaymentFailed(r.data.errorText))
                    }
                    else -> {
                        _state.update { it.copy(paymentLoading = false) }
                        r.data.transactionId?.let { pollCardCharge(it) }
                    }
                }
            }
        }
    }

    private fun pollCardCharge(transactionId: String) {
        statusJob?.cancel()
        statusJob = screenModelScope.launch {
            repeat(60) {
                delay(3000)
                when ((autopay.chargeState(transactionId) as? Outcome.Success)?.data) {
                    uz.tikoncha_parent.domain.model.autopay.ChargeState.COMPLETED -> {
                        _state.update { it.copy(paymentActive = false, paymentStatus = PaymentStatus.COMPLETED) }
                        _effect.tryEmit(CoinPurchaseEffect.PaymentSuccess)
                        return@launch
                    }
                    uz.tikoncha_parent.domain.model.autopay.ChargeState.CANCELLED -> {
                        _state.update { it.copy(paymentActive = false, paymentStatus = PaymentStatus.FAILED) }
                        return@launch
                    }
                    else -> Unit
                }
            }
            // Aniqlanmadi: tölov qayta yuborilmaydi — server özi aniqlaydi
            _state.update { it.copy(paymentActive = false) }
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