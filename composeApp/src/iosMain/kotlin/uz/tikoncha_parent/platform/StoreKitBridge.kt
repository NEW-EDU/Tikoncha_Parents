package uz.tikoncha_parent.platform

/**
 * Swift'da (iosApp/StoreKitBridgeImpl.swift) — StoreKit 2. StoreKit 1 imzolangan tranzaksiya bermaydi, server esa
 * PLUS'ni faqat Apple imzolagan JWS bilan yoqadi (`POST subscriptions/apple/transactions`). Tranzaksiya server
 * tasdiqlagandan keyingina yopiladi (`finish`); yopilmagani keyingi ochilishda qayta yuboriladi (`unfinished`).
 */
interface StoreKitBridge {
    /** [appAccountToken] — farzand id'si (UUID): yangilanişlar ham shu bolaga boradi. */
    fun purchase(productId: String, appAccountToken: String?, callback: StoreKitPurchaseCallback)
    fun finish(transactionId: String)
    /** Har element: "<transactionId>\t<jws>". */
    fun unfinished(callback: StoreKitListCallback)
    /** Do'kon narxi foydalanuvchi valyutasida ("4,99 €"), topilmasa null. */
    fun displayPrice(productId: String, callback: StoreKitPriceCallback)
}

interface StoreKitPurchaseCallback {
    fun onSigned(jws: String, transactionId: String)
    fun onCancel()
    fun onPending()
    fun onError(message: String)
}

interface StoreKitListCallback {
    fun onResult(items: List<String>)
}

interface StoreKitPriceCallback {
    fun onResult(price: String?)
}

object StoreKitBridgeProvider {
    var bridge: StoreKitBridge? = null
}
