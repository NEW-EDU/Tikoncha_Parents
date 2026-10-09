package uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.remote.model.subscription.PromoCodeValidationRequest
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.getOrNull
import uz.tikoncha_parent.domain.model.autopay.AutopayInfo
import uz.tikoncha_parent.domain.model.autopay.ChargeState
import uz.tikoncha_parent.domain.model.autopay.ChargeStatus
import uz.tikoncha_parent.domain.repository.AutopayRepository
import uz.tikoncha_parent.domain.repository.PaymentRepository

/**
 * Rozilik va birinchi to'lov (farzand uchun). Pul faqat shu yerda, "To'lash" bosilganda yechiladi.
 * Natija noma'lum bo'lsa (`PENDING`) qayta to'lash YO'Q — holat so'raladi.
 */
class AutopayConfirmViewModel(
    private val autopay: AutopayRepository,
    private val payments: PaymentRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(AutopayConfirmState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AutopayConfirmEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var started = false

    fun onEvent(event: AutopayConfirmEvent) {
        when (event) {
            is AutopayConfirmEvent.Init -> init(event)
            AutopayConfirmEvent.Resumed -> if (started && !_state.value.loading) reloadCards()
            AutopayConfirmEvent.OpenCardPicker -> _state.update { it.copy(showCardPicker = true) }
            is AutopayConfirmEvent.PickCard -> _state.update { it.copy(selectedCardId = event.cardId, showCardPicker = false) }
            AutopayConfirmEvent.AddNewCard -> {
                _state.update { it.copy(showCardPicker = false) }
                val s = _state.value
                _effect.trySend(AutopayConfirmEffect.OpenAddCard(s.planId, s.period, s.amount))
            }
            AutopayConfirmEvent.DismissCardPicker -> _state.update { it.copy(showCardPicker = false) }
            AutopayConfirmEvent.OpenPromo -> _state.update { it.copy(showPromoInput = true, promoError = null) }
            is AutopayConfirmEvent.PromoEntered -> checkPromo(event.code)
            AutopayConfirmEvent.DismissPromo -> _state.update { it.copy(showPromoInput = false, promoError = null) }
            AutopayConfirmEvent.RemovePromo -> _state.update { it.copy(promocode = "", promoPercent = 0, showPromoInput = false, promoError = null) }
            is AutopayConfirmEvent.ConsentChanged -> _state.update { it.copy(consent = event.value) }
            AutopayConfirmEvent.Pay -> pay()
            AutopayConfirmEvent.DismissDeclined -> _state.update { it.copy(declinedText = null) }
            AutopayConfirmEvent.DismissError -> _state.update { it.copy(error = null) }
            AutopayConfirmEvent.DismissTimeout -> {
                _state.update { it.copy(checkTimedOut = false) }
                _effect.trySend(AutopayConfirmEffect.CloseToSubscription)
            }
        }
    }

    private fun init(e: AutopayConfirmEvent.Init) {
        if (started) return
        started = true
        _state.update { it.copy(childId = e.childId, planId = e.planId, period = e.period, amount = e.amount, selectedCardId = e.cardId) }
        screenModelScope.launch {
            val cards = async { autopay.cards() }
            val info = async { autopay.state(e.childId).getOrNull() }
            val list = cards.await()
            _state.update { s ->
                val all = list.getOrNull().orEmpty()
                s.copy(
                    loading = false,
                    cards = all,
                    // Asosiy karta oldindan tanlanadi
                    selectedCardId = s.selectedCardId?.takeIf { id -> all.any { it.id == id } }
                        ?: (all.firstOrNull { it.isPrimary } ?: all.firstOrNull())?.id,
                    paidUntil = info.await()?.paidUntil,
                    error = list as? Outcome.Failure,
                )
            }
        }
    }

    /** Kod serverda tekshiriladi (summa, tarif, davr bilan); faqat tasdiqlangani qo'llanadi. */
    private fun checkPromo(raw: String) {
        val code = raw.filterNot { it.isWhitespace() }.uppercase()
        val s = _state.value
        if (code.isEmpty() || s.promoChecking) return
        _state.update { it.copy(promoChecking = true, promoError = null) }
        screenModelScope.launch {
            val r = payments.promoCodeValidation(
                PromoCodeValidationRequest(code = code, amount = s.amount, plan_duration = s.period.wire, plan_id = s.planId.ifBlank { null })
            )
            _state.update {
                when (r) {
                    is Outcome.Success -> it.copy(
                        promoChecking = false, showPromoInput = false,
                        promocode = r.data.code.uppercase(), promoPercent = r.data.discount_percentage.coerceIn(0, 100),
                    )
                    is Outcome.Failure -> it.copy(promoChecking = false, promoError = r)
                }
            }
        }
    }

    /** Yangi karta qo'shib qaytganda — ro'yxat yangilanadi, yangi karta tanlanadi. */
    private fun reloadCards() {
        screenModelScope.launch {
            val before = _state.value.cards.map { it.id }.toSet()
            val list = autopay.cards().getOrNull() ?: return@launch
            val fresh = list.firstOrNull { it.id !in before }
            _state.update { s ->
                s.copy(
                    cards = list,
                    selectedCardId = fresh?.id ?: s.selectedCardId?.takeIf { id -> list.any { it.id == id } }
                        ?: (list.firstOrNull { it.isPrimary } ?: list.firstOrNull())?.id,
                )
            }
        }
    }

    private fun pay() {
        val s = _state.value
        val card = s.selectedCard ?: return
        if (!s.canPay) return
        _state.update { it.copy(submitting = true, error = null, declinedText = null) }
        screenModelScope.launch {
            when (val r = autopay.enable(s.childId, s.planId, s.period, card.id, consent = true, promocode = s.promocode.ifBlank { null })) {
                is Outcome.Failure -> _state.update { it.copy(submitting = false, error = r) }
                is Outcome.Success -> when (r.data.status) {
                    ChargeStatus.PAID, ChargeStatus.SCHEDULED -> finish(r.data.autopay, scheduled = r.data.status == ChargeStatus.SCHEDULED)
                    ChargeStatus.PENDING -> {
                        val tx = r.data.transactionId
                        if (tx == null) {
                            _state.update { it.copy(submitting = false, checkTimedOut = true) }
                        } else {
                            _state.update { it.copy(submitting = false, checking = true) }
                            poll(tx)
                        }
                    }
                    ChargeStatus.FAILED, ChargeStatus.SKIPPED ->
                        _state.update { it.copy(submitting = false, declinedText = r.data.errorText.orEmpty()) }
                }
            }
        }
    }

    /** Har 5 s, ko'pi bilan ~2 daqiqa. `pay` qayta chaqirilmaydi — faqat holat so'raladi. */
    private suspend fun poll(transactionId: String) {
        repeat(POLL_TIMES) {
            delay(POLL_MS)
            when (autopay.chargeState(transactionId).getOrNull()) {
                ChargeState.COMPLETED -> {
                    val info = autopay.state(_state.value.childId).getOrNull()
                    _state.update { it.copy(checking = false) }
                    if (info != null) finish(info, scheduled = false) else _effect.send(AutopayConfirmEffect.CloseToSubscription)
                    return
                }
                ChargeState.CANCELLED -> {
                    val info = autopay.state(_state.value.childId).getOrNull()
                    _state.update { it.copy(checking = false, declinedText = info?.lastErrorText.orEmpty()) }
                    return
                }
                else -> Unit
            }
        }
        _state.update { it.copy(checking = false, checkTimedOut = true) }
    }

    private suspend fun finish(info: AutopayInfo, scheduled: Boolean) {
        runCatching { payments.syncSubscriptionLimits() }   // ilova farzandning PLUS holatini darhol bilsin
        val s = _state.value
        _state.update { it.copy(submitting = false, checking = false) }
        _effect.send(
            AutopayConfirmEffect.Done(
                scheduled = scheduled,
                amount = s.payAmount,
                paidUntil = info.paidUntil,
                nextChargeAt = info.nextChargeAt,
                card = info.card ?: s.selectedCard,
            )
        )
    }

    private companion object {
        const val POLL_TIMES = 24
        const val POLL_MS = 5_000L
    }
}
