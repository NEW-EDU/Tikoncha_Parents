package uz.tikoncha_parent.platform

import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import uz.tikoncha_parent.domain.model.PurchaseResult

/** App Store xaridi StoreKit 2 orqali (Swift ko'prigi). 2026-10-27 gacha StoreKit 1 xaridni serverga yubormasdan
 *  yopardi — pul yechilardi, PLUS yoqilmasdi. */
actual class PlatformPurchaseService actual constructor() {

    actual suspend fun purchase(productId: String, appAccountToken: String?): PurchaseResult {
        // StoreKit'ning inglizcha matni ekranga chiqmaydi — kod qaytadi, UI tarjima qilingan matnni ko'rsatadi
        val bridge = StoreKitBridgeProvider.bridge ?: run {
            Logger.e("PlatformPurchaseService", "StoreKit bridge is not set")
            return PurchaseResult.Error(STORE_ERROR_UNAVAILABLE)
        }
        return suspendCancellableCoroutine { cont ->
            bridge.purchase(productId, appAccountToken, object : StoreKitPurchaseCallback {
                override fun onSigned(jws: String, transactionId: String) { if (cont.isActive) cont.resume(PurchaseResult.Signed(jws, transactionId)) }
                override fun onCancel() { if (cont.isActive) cont.resume(PurchaseResult.Cancelled) }
                override fun onPending() { if (cont.isActive) cont.resume(PurchaseResult.Pending) }
                override fun onError(message: String) {
                    Logger.e("PlatformPurchaseService", "StoreKit purchase error: $message")
                    if (cont.isActive) cont.resume(PurchaseResult.Error(STORE_ERROR_FAILED))
                }
            })
        }
    }

    actual fun finish(transactionId: String) {
        StoreKitBridgeProvider.bridge?.finish(transactionId)
    }

    actual suspend fun unfinished(): List<PurchaseResult.Signed> {
        val bridge = StoreKitBridgeProvider.bridge ?: return emptyList()
        return suspendCancellableCoroutine { cont ->
            bridge.unfinished(object : StoreKitListCallback {
                override fun onResult(items: List<String>) {
                    if (!cont.isActive) return
                    cont.resume(items.mapNotNull { line ->
                        val i = line.indexOf('\t')
                        if (i <= 0) null else PurchaseResult.Signed(jws = line.substring(i + 1), transactionId = line.substring(0, i))
                    })
                }
            })
        }
    }

    actual suspend fun displayPrice(productId: String): String? {
        val bridge = StoreKitBridgeProvider.bridge ?: return null
        return suspendCancellableCoroutine { cont ->
            bridge.displayPrice(productId, object : StoreKitPriceCallback {
                override fun onResult(price: String?) { if (cont.isActive) cont.resume(price) }
            })
        }
    }
}
