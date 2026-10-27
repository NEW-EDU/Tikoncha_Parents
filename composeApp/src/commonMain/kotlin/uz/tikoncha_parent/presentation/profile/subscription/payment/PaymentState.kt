package uz.tikoncha_parent.presentation.profile.subscription.payment

import uz.tikoncha_parent.domain.model.PaymentStatus
import uz.tikoncha_parent.domain.model.SubscriptionDuration
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.presentation.ui_state.ResponseState

data class PaymentState(
    val selectedPaymentType: PaymentType? = PaymentType.Click,
    val paymentResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val applePaymentResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val merchantTransId: String = "",
    val serviceId: Int = 0,
    val merchantId: Int = 46788,
    val subscriptionDuration: SubscriptionDuration = SubscriptionDuration.MONTHLY,
    val paymentStatus: PaymentStatus = PaymentStatus.START,
    val paymentStatusError: Outcome.Failure? = null,
    /** iPhone va O'zbekistondan tashqarida: faqat App Store (mahalliy to'lovlar ko'rsatilmaydi). */
    val appStoreOnly: Boolean = false,
    /** App Store narxi foydalanuvchi valyutasida ("4,99 €"); null — hali yo'q yoki Android. */
    val storePrice: String? = null,
    val planId: String = "",

    val promoCodeResponseState: ResponseState<Nothing> = ResponseState.Idle,
    val promoActivated: Boolean = false,
    val originalAmount: Int = 0,
    val finalAmount: Int = 0,
    val promoCode: String = "",
    val discountAmount: Int = 0,
    val discountSaving: Int = 0,
    val discountPercentage: Int = 0,

    val showSubscribeChildSheet: Boolean = false
)