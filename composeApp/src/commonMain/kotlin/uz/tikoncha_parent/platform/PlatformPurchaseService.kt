package uz.tikoncha_parent.platform

import uz.tikoncha_parent.domain.model.PurchaseResult

/**
 * [PurchaseResult.Error.message] kodlari (do'kondan kelgan inglizcha matn ekranga chiqmaydi — UI tarjima qilingan
 * matn ko'rsatadi; asl sabab logga yoziladi).
 */
const val STORE_ERROR_UNAVAILABLE = "store_unavailable"
const val STORE_ERROR_FAILED = "store_failed"

/** Do'kon xaridi (iPhone: App Store). Android'da Click/Paylov — bu yerda emas. */
expect class PlatformPurchaseService() {
    /** [appAccountToken] — PLUS kimga (farzand id'si). Muvaffaqiyat — [PurchaseResult.Signed], hali yopilmagan. */
    suspend fun purchase(productId: String, appAccountToken: String? = null): PurchaseResult
    /** Server PLUS'ni yoqqandan keyin — tranzaksiyani yopadi. */
    fun finish(transactionId: String)
    /** Server hali tasdiqlamagan xaridlar (ilova yopilib qolgan, internet uzilgan) — qayta yuborish uchun. */
    suspend fun unfinished(): List<PurchaseResult.Signed>
    /** Do'kon narxi foydalanuvchi valyutasida. */
    suspend fun displayPrice(productId: String): String?
}
