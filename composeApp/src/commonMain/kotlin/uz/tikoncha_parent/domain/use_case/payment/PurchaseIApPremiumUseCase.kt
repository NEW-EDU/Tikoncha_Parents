package uz.tikoncha_parent.domain.use_case.payment

import uz.tikoncha_parent.domain.model.PurchaseResult
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.repository.PaymentRepository
import uz.tikoncha_parent.platform.Logger
import uz.tikoncha_parent.platform.PlatformPurchaseService

/**
 * App Store orqali PLUS (iPhone): xarid → Apple imzolagan tranzaksiya serverga → server farzandga PLUS yoqadi →
 * shundan keyingina tranzaksiya yopiladi. Server javob bermasa tranzaksiya ochiq qoladi va [retryPending]
 * keyingi ochilishda qayta yuboradi — pul yechilib PLUS yoqilmay qolmaydi.
 */
class PurchaseIApPremiumUseCase(
    private val platform: PlatformPurchaseService,
    private val payments: PaymentRepository,
) {
    suspend operator fun invoke(productId: String, childId: String?): PurchaseResult {
        val result = platform.purchase(productId, childId)
        Logger.e("PurchaseIApPremiumUseCase", "result=${result::class.simpleName}")
        if (result !is PurchaseResult.Signed) return result
        return when (val r = payments.reportApplePurchase(result.jws, childId)) {
            is Outcome.Success -> {
                platform.finish(result.transactionId)
                payments.syncSubscriptionLimits()
                PurchaseResult.Success
            }
            is Outcome.Failure -> PurchaseResult.Error(r.serverMessage ?: "activation_pending")
        }
    }

    /** Yopilmagan (server tasdiqlamagan) xaridlarni qayta yuboradi. Farzand xariddagi appAccountToken'dan. */
    suspend fun retryPending() {
        for (p in platform.unfinished()) {
            if (payments.reportApplePurchase(p.jws, null) is Outcome.Success) platform.finish(p.transactionId)
        }
    }

    suspend fun displayPrice(productId: String): String? = platform.displayPrice(productId)
}
