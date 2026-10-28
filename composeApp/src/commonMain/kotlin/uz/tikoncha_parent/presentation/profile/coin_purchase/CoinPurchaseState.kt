package uz.tikoncha_parent.presentation.profile.coin_purchase

import uz.tikoncha_parent.domain.model.PaymentStatus
import uz.tikoncha_parent.domain.model.app_error.Outcome

data class CoinPurchaseState(
    val coins: Int = 0,
    val totalPrice: Int = 100,
    val promoCodeDiscountPrice: Int = 0,
    val discountPrice: Int = 0,
    val paymentLoading: Boolean = false,
    val paymentActive: Boolean = false,
    val merchantTransId: String = "",
    val serviceId: Int = 0,
    val merchantId: Int = 46788,
    val paymentStatus: PaymentStatus = PaymentStatus.START,
    val paymentStatusError: Outcome.Failure? = null,
    val planId: String = "",
    val promoActivated: Boolean = false,
    val promoCode: String = "",
    val discountAmount: Int = 0,
    val discountSaving: Int = 0,
    val discountPercentage: Int = 0,
    val promoCodeLoading: Boolean = false,
    /** Saqlangan kartalar (birinçisi — asosiy) va kartadan tölov ochiqmi. */
    val cards: List<uz.tikoncha_parent.domain.model.autopay.PayCard> = emptyList(),
    val cardAvailable: Boolean = false,
    val method: CoinPayMethod = CoinPayMethod.CLICK,
){
    val card: uz.tikoncha_parent.domain.model.autopay.PayCard? get() = cards.firstOrNull()

    val finalPrice: Int
        get() = totalPrice - promoCodeDiscountPrice - discountPrice
}

enum class CoinPayMethod { CARD, CLICK }
