package uz.tikoncha_parent.presentation.profile.subscription.subscription_info

import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.repository.ChildRepository
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.tikoncha_parent.data.local.AppSettings
import uz.tikoncha_parent.data.mapper.toSubscriptionPlanUi
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.app_error.getOrNull
import uz.tikoncha_parent.domain.model.autopay.ChargeStatus
import uz.tikoncha_parent.domain.model.autopay.PlanPeriod
import uz.tikoncha_parent.domain.model.subscription.PlanDuration
import uz.tikoncha_parent.domain.model.subscription.PlanType
import uz.tikoncha_parent.domain.repository.AutopayRepository
import uz.tikoncha_parent.domain.repository.PaymentRepository
import uz.tikoncha_parent.presentation.ui_state.ResponseState

class SubscriptionViewModel(
    private val paymentRepository: PaymentRepository,
    private val autopay: AutopayRepository,
    private val childRepository: ChildRepository,
) : ScreenModel {

    private val _state = MutableStateFlow(SubscriptionState())
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    private val _effect = Channel<SubscriptionEffect>(Channel.BUFFERED)
    val effect: Flow<SubscriptionEffect> = _effect.receiveAsFlow()

    init {
        _state.update {
            it.copy(
                selectedChildId = AppSettings.selectedChild?.userId,
                selectedChild = AppSettings.selectedChild,
                children = AppSettings.children,
            )
        }
        onEvent(SubscriptionEvent.Load)
        loadChildren()
    }

    fun onEvent(event: SubscriptionEvent) {
        when (event) {
            SubscriptionEvent.Load ->{
                val first = _state.value.children.firstOrNull()
                when {
                    !_state.value.selectedChildId.isNullOrBlank() -> { loadStatus(); loadAutopay() }
                    // Tanlanmagan, lekin farzand bor — birinchisi (Statistika'dagidek)
                    first != null -> selectChild(first)
                    // Farzand umuman yo'q — eski yo'l: tarif (telefon raqami bo'yicha sotish)
                    else -> _effect.trySend(SubscriptionEffect.NavigateToPayment)
                }
            }

            is SubscriptionEvent.SelectChild -> selectChild(event.child)
            SubscriptionEvent.OpenPaywall -> _effect.trySend(SubscriptionEffect.OpenPaywall)

            SubscriptionEvent.ResetResponseState -> {
                _state.update { it.copy(subscriptionStatusState = ResponseState.Idle) }
            }

            SubscriptionEvent.OnCardClick -> {
                screenModelScope.launch {
                    _effect.trySend(SubscriptionEffect.NavigateToInfoMode)
                }
            }

            SubscriptionEvent.OnErrorDismissed -> {
                _state.update { it.copy(subscriptionStatusState = ResponseState.Idle) }
                screenModelScope.launch {
                    _effect.trySend(SubscriptionEffect.PopBack)
                }
            }

            SubscriptionEvent.OnBackClick -> {
                screenModelScope.launch { _effect.trySend(SubscriptionEffect.PopBack) }
            }

            // Ekranga qaytganda (to'lov / karta almashtirishdan keyin) — jim yangilanadi
            SubscriptionEvent.Resumed -> if (_state.value.subscription != null) { refreshStatusSilently(); loadAutopay(); loadChildren() }
            is SubscriptionEvent.AutopaySwitch ->
                if (event.on) enableAutopay() else _state.update { it.copy(confirmDisable = true) }
            SubscriptionEvent.ConfirmDisable -> disable()
            SubscriptionEvent.DismissDisable -> _state.update { it.copy(confirmDisable = false) }
            SubscriptionEvent.RetryCharge -> retry()
            SubscriptionEvent.OpenCardPicker -> openCardPicker()
            is SubscriptionEvent.PickCard -> pickCard(event.cardId)
            SubscriptionEvent.DismissCardPicker -> _state.update { it.copy(showCardPicker = false) }
            SubscriptionEvent.AddCard -> {
                _state.update { it.copy(showCardPicker = false) }
                _effect.trySend(SubscriptionEffect.OpenAddCard)
            }
            SubscriptionEvent.OpenCards -> _effect.trySend(SubscriptionEffect.OpenCards)
            SubscriptionEvent.OpenHistory -> _effect.trySend(SubscriptionEffect.OpenHistory)
            SubscriptionEvent.AutopayErrorDismissed -> _state.update { it.copy(autopayError = null, retryDeclinedText = null) }
        }
    }

    private fun loadStatus() {
        screenModelScope.launch {
            _state.update { it.copy(subscriptionStatusState = ResponseState.Loading) }

            when (val res = paymentRepository.getSubscriptionStatus(_state.value.selectedChildId)) {
                is Outcome.Failure -> {
                    _state.update {
                        it.copy(subscriptionStatusState = ResponseState.Error(failure = res))
                    }
                }

                is Outcome.Success -> {
                    _state.update {
                        it.copy(
                            subscription = res.data,
                            subscriptionStatusState = ResponseState.Success(Unit)
                        )
                    }
                    // PLUS yo'q — tarif ekraniga o'zi o'tmaydi: tanlagich joyida qoladi, ekranda "… uchun obuna bo'lish"
                    if (res.data.planType == PlanType.FREE || res.data.isExpired) loadFromPrice()
                }
            }
        }
    }

    /** Sarlavhadagi tanlagich uchun yangi ro'yxat (PLUS belgilari to'lovdan keyin ham to'g'ri bo'lsin). Xato — kesh qoladi. */
    private fun loadChildren() {
        screenModelScope.launch {
            val list = childRepository.children().getOrNull() ?: return@launch
            val current = _state.value.selectedChildId
            _state.update { s ->
                s.copy(children = list, selectedChild = list.firstOrNull { it.userId == current } ?: s.selectedChild)
            }
            if (current.isNullOrBlank()) list.firstOrNull()?.let { selectChild(it) }
        }
    }

    /** Statistika'dagi kabi: tanlov butun ilova uchun saqlanadi. Eski farzandning ma'lumoti bir lahza ham ko'rinmasin. */
    private fun selectChild(child: UserInfo) {
        if (child.userId == _state.value.selectedChildId && _state.value.subscription != null) return
        AppSettings.selectedChildId = child.userId
        AppSettings.selectedChild = child
        _state.update {
            it.copy(
                selectedChildId = child.userId, selectedChild = child,
                subscription = null, autopay = null, freeFromPrice = null,
                confirmDisable = false, showCardPicker = false,
            )
        }
        loadStatus()
        loadAutopay()
    }

    /** "Karta orqali oyiga … dan" — karta narxi; server bermasa Click narxi. */
    private fun loadFromPrice() {
        if (_state.value.freeFromPrice != null) return
        screenModelScope.launch {
            val plan = paymentRepository.subscriptionPlans().getOrNull()?.firstOrNull()?.toSubscriptionPlanUi() ?: return@launch
            _state.update { it.copy(freeFromPrice = plan.cardMonthly ?: plan.monthly.price) }
        }
    }

    /** Xato bo'lsa jim — eski ma'lumot qoladi. */
    private fun refreshStatusSilently() {
        screenModelScope.launch {
            paymentRepository.getSubscriptionStatus(_state.value.selectedChildId).getOrNull()?.let { s ->
                _state.update { it.copy(subscription = s) }
            }
        }
    }

    // ═══ Avto-to'lov (shu farzand uchun) ═══════════════════════

    private val childId: String? get() = _state.value.selectedChildId?.takeIf { it.isNotBlank() }

    private fun loadAutopay() {
        val child = childId ?: return
        screenModelScope.launch {
            autopay.state(child).getOrNull()?.let { info -> _state.update { it.copy(autopay = info) } }
        }
    }

    /** O'chiq avto-to'lovni yoqish — joriy tarif davri va karta narxi bilan tasdiqlash ekraniga. */
    private fun enableAutopay() {
        val s = _state.value
        if (s.autopayBusy) return
        _state.update { it.copy(autopayBusy = true) }
        screenModelScope.launch {
            val period = when (s.subscription?.planDuration) {
                PlanDuration.MONTHLY -> PlanPeriod.MONTHLY
                else -> s.autopay?.period ?: PlanPeriod.ANNUAL
            }
            val plan = paymentRepository.subscriptionPlans().getOrNull()?.firstOrNull()?.toSubscriptionPlanUi()
            _state.update { it.copy(autopayBusy = false) }
            if (plan == null) return@launch
            // Karta orqali avto-to'lov narxi; server bermasa (eski server) — umumiy narx
            val amount = if (period == PlanPeriod.ANNUAL) plan.cardAnnual ?: plan.annual.price
            else plan.cardMonthly ?: plan.monthly.price
            _effect.send(SubscriptionEffect.OpenAutopayConfirm(plan.planId, period, amount))
        }
    }

    private fun disable() {
        val child = childId ?: return
        _state.update { it.copy(confirmDisable = false, autopayBusy = true) }
        screenModelScope.launch {
            when (val r = autopay.disable(child)) {
                is Outcome.Success -> _state.update { it.copy(autopayBusy = false, autopay = r.data) }
                is Outcome.Failure -> _state.update { it.copy(autopayBusy = false, autopayError = r) }
            }
        }
    }

    private fun retry() {
        val child = childId ?: return
        if (_state.value.autopayBusy) return
        _state.update { it.copy(autopayBusy = true) }
        screenModelScope.launch {
            when (val r = autopay.retry(child)) {
                is Outcome.Success -> {
                    _state.update { it.copy(autopayBusy = false, autopay = r.data.autopay) }
                    when (r.data.status) {
                        ChargeStatus.PAID -> { runCatching { paymentRepository.syncSubscriptionLimits() }; refreshStatusSilently() }
                        ChargeStatus.FAILED -> _state.update { it.copy(retryDeclinedText = r.data.errorText.orEmpty()) }
                        else -> Unit
                    }
                }
                is Outcome.Failure -> _state.update { it.copy(autopayBusy = false, autopayError = r) }
            }
        }
    }

    private fun openCardPicker() {
        screenModelScope.launch {
            val cards = autopay.cards().getOrNull().orEmpty()
            _state.update { it.copy(cards = cards, showCardPicker = true) }
        }
    }

    private fun pickCard(cardId: String) {
        val child = childId ?: return
        _state.update { it.copy(showCardPicker = false, autopayBusy = true) }
        screenModelScope.launch {
            when (val r = autopay.changeCard(child, cardId)) {
                is Outcome.Success -> _state.update { it.copy(autopayBusy = false, autopay = r.data) }
                is Outcome.Failure -> _state.update { it.copy(autopayBusy = false, autopayError = r) }
            }
        }
    }
}
