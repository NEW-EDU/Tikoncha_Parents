package uz.tikoncha_parent.presentation.profile.subscription.autopay.method

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.domain.model.app_error.getOrNull
import uz.tikoncha_parent.domain.repository.AutopayRepository

class PaymentMethodViewModel(
    private val autopay: AutopayRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(PaymentMethodState())
    val state = _state.asStateFlow()

    private val _effect = Channel<PaymentMethodEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var started = false

    fun onEvent(event: PaymentMethodEvent) {
        when (event) {
            is PaymentMethodEvent.Init -> init(event)
            is PaymentMethodEvent.Select -> _state.update { it.copy(selected = event.method) }
            PaymentMethodEvent.Continue -> onContinue()
        }
    }

    private fun init(e: PaymentMethodEvent.Init) {
        if (started) return
        started = true
        _state.update {
            it.copy(childId = e.childId, planId = e.planId, period = e.period, cardAmount = e.cardAmount, clickAmount = e.clickAmount)
        }
        val childId = e.childId
        if (childId.isNullOrBlank()) {
            // Ro'yxatdan o'tmagan bola — avto-to'lov farzand hisobiga bog'lanadi, shuning uchun faqat Click
            _state.update { it.copy(loading = false, cardAvailable = false, selected = PayMethod.CLICK) }
            return
        }
        screenModelScope.launch {
            val info = async { autopay.state(childId).getOrNull() }
            val cards = async { autopay.cards().getOrNull() }
            val available = info.await()?.available == true
            // Server javob bermasa yoki karta to'lovi yopiq — faqat Click (eski yo'l) qoladi
            _state.update {
                it.copy(
                    loading = false,
                    cardAvailable = available,
                    hasSavedCards = !cards.await().isNullOrEmpty(),
                    selected = if (available) PayMethod.CARD else PayMethod.CLICK,
                )
            }
        }
    }

    private fun onContinue() {
        val s = _state.value
        if (s.loading) return
        val effect = when {
            s.selected == PayMethod.CLICK || !s.cardAvailable -> PaymentMethodEffect.OpenClick(s.planId, s.period, s.clickAmount)
            s.hasSavedCards -> PaymentMethodEffect.OpenConfirm(s.planId, s.period, s.cardAmount)
            else -> PaymentMethodEffect.OpenAddCard(s.planId, s.period, s.cardAmount)
        }
        _effect.trySend(effect)
    }
}
