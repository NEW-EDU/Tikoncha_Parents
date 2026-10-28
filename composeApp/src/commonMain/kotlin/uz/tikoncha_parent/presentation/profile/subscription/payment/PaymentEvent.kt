package uz.tikoncha_parent.presentation.profile.subscription.payment

import uz.tikoncha_parent.domain.model.SubscriptionDuration

sealed interface PaymentEvent {
    data object Pay: PaymentEvent
    data class PayWithChildPhone(val phone: String): PaymentEvent
    data object DismissChildSelectionSheet: PaymentEvent
    data class SetAmount(val amount:Int): PaymentEvent
    data class SetPaymentType(val type: PaymentType): PaymentEvent
    data object ResetPaymentResponse: PaymentEvent
    data class SetSubscriptionDuration(val duration: SubscriptionDuration): PaymentEvent
    data class SetPlanId(val planId: String): PaymentEvent
    /** Foydalanuvçi App Store'ni özi tanladi (iPhone, O'zbekistonda ham). */
    data object ForceAppStore: PaymentEvent

    data class OnPromoCode(val promoCode: String): PaymentEvent
    data object ValidatePromoCode: PaymentEvent
    data object ClearPromoCodeResponse: PaymentEvent

}