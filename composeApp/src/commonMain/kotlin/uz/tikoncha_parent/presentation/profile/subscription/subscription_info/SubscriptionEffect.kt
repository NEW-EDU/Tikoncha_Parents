package uz.tikoncha_parent.presentation.profile.subscription.subscription_info

import uz.tikoncha_parent.domain.model.autopay.PlanPeriod

sealed interface SubscriptionEffect {
    data object NavigateToPayment : SubscriptionEffect
    data object NavigateToInfoMode : SubscriptionEffect
    data object PopBack : SubscriptionEffect
    data class OpenAutopayConfirm(val planId: String, val period: PlanPeriod, val amount: Int) : SubscriptionEffect
    data object OpenCards : SubscriptionEffect
    data object OpenAddCard : SubscriptionEffect
    data object OpenHistory : SubscriptionEffect
    data object OpenPaywall : SubscriptionEffect
}