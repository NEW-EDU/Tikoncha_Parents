package uz.tikoncha_parent.presentation.profile.coin_purchase

import uz.tikoncha_parent.domain.model.app_error.Outcome

sealed class CoinPurchaseEffect {
    data class ShowPromoCodeErrorToast(val failure: Outcome.Failure): CoinPurchaseEffect()
    data class PaymentFailed(val failure: Outcome.Failure): CoinPurchaseEffect()

    data object PaymentSuccess: CoinPurchaseEffect()
    /** Kartadan tölov tanlangan, saqlangan karta yöq — avval karta qöşiladi (SMS). Pul olinmaydi. */
    data object OpenAddCard: CoinPurchaseEffect()
    /** Kartadan tölov rad etildi — server matni. */
    data class CardPaymentFailed(val message: String?): CoinPurchaseEffect()
    data object ShowPromoCodeSuccessToast: CoinPurchaseEffect()

    data class OpenClickPayment(
        val serviceId: Int,
        val merchantId: Int,
        val amount: Int,
        val transactionId: String,
    ) : CoinPurchaseEffect()
}