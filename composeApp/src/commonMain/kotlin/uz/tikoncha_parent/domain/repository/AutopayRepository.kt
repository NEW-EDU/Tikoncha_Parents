package uz.tikoncha_parent.domain.repository

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.autopay.AutopayInfo
import uz.tikoncha_parent.domain.model.autopay.CardStart
import uz.tikoncha_parent.domain.model.autopay.ChargeResult
import uz.tikoncha_parent.domain.model.autopay.ChargeState
import uz.tikoncha_parent.domain.model.autopay.PayCard
import uz.tikoncha_parent.domain.model.autopay.PlanPeriod

/**
 * Karta orqali avto-to'lov. Pul faqat [enable] va [retry] da yechiladi; karta qo'shish pul olmaydi.
 * `childId` — farzandning user id'si (ota-ona faqat o'ziga bog'langan farzand uchun to'laydi).
 */
interface AutopayRepository {
    suspend fun state(childId: String): Outcome<AutopayInfo>
    suspend fun cards(): Outcome<List<PayCard>>
    /** Karta raqami faqat shu chaqiruvda serverga ketadi; hech qayerda saqlanmaydi. */
    suspend fun startCard(cardNumber: String, expire: String): Outcome<CardStart>
    suspend fun confirmCard(cardId: String, otp: String): Outcome<PayCard>
    suspend fun removeCard(cardId: String): Outcome<Unit>
    /** Asosiy karta — ota-onaning barcha avto-to'lovlari shu kartaga o'tadi. Yangi ro'yxat. */
    suspend fun makePrimary(cardId: String): Outcome<List<PayCard>>
    suspend fun enable(childId: String, planId: String, period: PlanPeriod, cardId: String, consent: Boolean, promocode: String?): Outcome<ChargeResult>
    suspend fun changeCard(childId: String, cardId: String): Outcome<AutopayInfo>
    suspend fun retry(childId: String): Outcome<ChargeResult>
    suspend fun disable(childId: String): Outcome<AutopayInfo>
    suspend fun chargeState(transactionId: String): Outcome<ChargeState>
}
