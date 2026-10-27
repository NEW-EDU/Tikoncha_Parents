package uz.tikoncha_parent.platform

import uz.tikoncha_parent.domain.model.PurchaseResult

actual class PlatformPurchaseService actual constructor() {
    actual suspend fun purchase(productId: String, appAccountToken: String?): PurchaseResult =
        PurchaseResult.Error("Android purchase is handled via Click/Paylov")

    actual fun finish(transactionId: String) = Unit

    actual suspend fun unfinished(): List<PurchaseResult.Signed> = emptyList()

    actual suspend fun displayPrice(productId: String): String? = null
}
