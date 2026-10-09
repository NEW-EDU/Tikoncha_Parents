package uz.tikoncha_parent.presentation.profile.subscription.subscription_info

import uz.tikoncha_parent.domain.model.UserInfo

sealed interface SubscriptionEvent {
    data object Load : SubscriptionEvent
    data object ResetResponseState : SubscriptionEvent
    data object OnCardClick : SubscriptionEvent
    data object OnErrorDismissed : SubscriptionEvent
    data object OnBackClick : SubscriptionEvent
    data object Resumed : SubscriptionEvent
    data class SelectChild(val child: UserInfo) : SubscriptionEvent
    /** PLUS yo'q farzand — tarif ekrani shu farzand uchun. */
    data object OpenPaywall : SubscriptionEvent

    // ── avto-to'lov ──
    data class AutopaySwitch(val on: Boolean) : SubscriptionEvent
    data object ConfirmDisable : SubscriptionEvent
    data object DismissDisable : SubscriptionEvent
    data object RetryCharge : SubscriptionEvent
    data object OpenCardPicker : SubscriptionEvent
    data class PickCard(val cardId: String) : SubscriptionEvent
    data object DismissCardPicker : SubscriptionEvent
    data object AddCard : SubscriptionEvent
    data object OpenCards : SubscriptionEvent
    data object OpenHistory : SubscriptionEvent
    data object AutopayErrorDismissed : SubscriptionEvent
}