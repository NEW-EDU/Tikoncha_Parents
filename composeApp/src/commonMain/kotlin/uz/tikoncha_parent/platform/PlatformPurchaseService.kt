package uz.tikoncha_parent.platform

import uz.tikoncha_parent.domain.model.PurchaseResult

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
