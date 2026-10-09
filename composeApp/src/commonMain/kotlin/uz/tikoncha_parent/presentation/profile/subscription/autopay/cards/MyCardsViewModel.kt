package uz.tikoncha_parent.presentation.profile.subscription.autopay.cards

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.autopay.PayCard
import uz.tikoncha_parent.domain.repository.AutopayRepository

data class MyCardsState(
    val loading: Boolean = true,
    val cards: List<PayCard> = emptyList(),
    /** ⋮ bosilgan karta — amallar varag'i ochiq. */
    val actionsFor: PayCard? = null,
    val confirmRemove: PayCard? = null,
    val busy: Boolean = false,
    val error: Outcome.Failure? = null,
)

sealed interface MyCardsEvent {
    data object Load : MyCardsEvent
    data class OpenActions(val card: PayCard) : MyCardsEvent
    data object DismissActions : MyCardsEvent
    data class MakePrimary(val card: PayCard) : MyCardsEvent
    data class AskRemove(val card: PayCard) : MyCardsEvent
    /** Karta hodisaning o'zida: CustomDialog tugmasi avval onDismiss'ni chaqiradi (confirmRemove tozalanadi). */
    data class ConfirmRemove(val card: PayCard) : MyCardsEvent
    data object DismissRemove : MyCardsEvent
    data object DismissError : MyCardsEvent
}

class MyCardsViewModel(
    private val autopay: AutopayRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(MyCardsState())
    val state = _state.asStateFlow()

    fun onEvent(event: MyCardsEvent) {
        when (event) {
            MyCardsEvent.Load -> load()
            is MyCardsEvent.OpenActions -> _state.update { it.copy(actionsFor = event.card) }
            MyCardsEvent.DismissActions -> _state.update { it.copy(actionsFor = null) }
            is MyCardsEvent.MakePrimary -> primary(event.card)
            is MyCardsEvent.AskRemove -> _state.update { it.copy(actionsFor = null, confirmRemove = event.card) }
            is MyCardsEvent.ConfirmRemove -> remove(event.card)
            MyCardsEvent.DismissRemove -> _state.update { it.copy(confirmRemove = null) }
            MyCardsEvent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun load() {
        screenModelScope.launch {
            when (val r = autopay.cards()) {
                is Outcome.Success -> _state.update { it.copy(loading = false, cards = r.data) }
                is Outcome.Failure -> _state.update { it.copy(loading = false, error = r) }
            }
        }
    }

    private fun primary(card: PayCard) {
        if (_state.value.busy) return
        _state.update { it.copy(actionsFor = null, busy = true) }
        screenModelScope.launch {
            when (val r = autopay.makePrimary(card.id)) {
                is Outcome.Success -> _state.update { it.copy(busy = false, cards = r.data) }
                is Outcome.Failure -> _state.update { it.copy(busy = false, error = r) }
            }
        }
    }

    private fun remove(card: PayCard) {
        if (_state.value.busy) return
        _state.update { it.copy(confirmRemove = null, busy = true) }
        screenModelScope.launch {
            when (val r = autopay.removeCard(card.id)) {
                // Asosiy karta o'chsa server keyingisini asosiy qiladi — ro'yxatni qayta olamiz
                is Outcome.Success -> when (val fresh = autopay.cards()) {
                    is Outcome.Success -> _state.update { it.copy(busy = false, cards = fresh.data) }
                    is Outcome.Failure -> _state.update { s -> s.copy(busy = false, cards = s.cards.filterNot { it.id == card.id }) }
                }
                is Outcome.Failure -> _state.update { it.copy(busy = false, error = r) }
            }
        }
    }
}
