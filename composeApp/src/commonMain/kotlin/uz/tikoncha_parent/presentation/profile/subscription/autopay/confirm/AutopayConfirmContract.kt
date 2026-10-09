package uz.tikoncha_parent.presentation.profile.subscription.autopay.confirm

import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.autopay.PayCard
import uz.tikoncha_parent.domain.model.autopay.PlanPeriod
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

data class AutopayConfirmState(
    /** Farzand user id — ota-ona shu farzand uchun to'laydi. */
    val childId: String = "",
    val planId: String = "",
    val period: PlanPeriod = PlanPeriod.ANNUAL,
    val amount: Int = 0,
    val loading: Boolean = true,
    val cards: List<PayCard> = emptyList(),
    val selectedCardId: String? = null,
    /** Hozirgi PLUS tugashi — bo'lsa birinchi to'lov o'shandan bir kun oldin (hozir pul yechilmaydi). */
    val paidUntil: Instant? = null,
    /** Server tasdiqlagan promokod (bo'sh — yo'q). Chegirma faqat birinchi to'lovga. */
    val promocode: String = "",
    val promoPercent: Int = 0,
    /** Oynada "Qo'llash" bosildi — server tekshiryapti. */
    val promoChecking: Boolean = false,
    /** Server rad etdi — oyna ochiq qoladi, matn maydon tagida. */
    val promoError: Outcome.Failure? = null,
    val consent: Boolean = false,
    val submitting: Boolean = false,
    /** Natija hali noma'lum — server tekshiryapti. Qayta to'lash tugmasi yo'q. */
    val checking: Boolean = false,
    val showCardPicker: Boolean = false,
    val showPromoInput: Boolean = false,
    /** To'lov o'tmadi — server tilida tayyor matn. */
    val declinedText: String? = null,
    val error: Outcome.Failure? = null,
    /** Natija 2 daqiqada ham kelmadi. */
    val checkTimedOut: Boolean = false,
) {
    val selectedCard: PayCard? get() = cards.firstOrNull { it.id == selectedCardId }

    /** Faol PLUS uzoq — to'lov hozir emas, tugashidan bir kun oldin. */
    fun scheduledStart(now: Instant = Clock.System.now()): Instant? =
        paidUntil?.minus(1.days)?.takeIf { it > now + 5.minutes }

    /** Birinchi to'lov summasi — server bilan bir xil hisob (butun songa pastga). */
    val payAmount: Int get() = if (promoPercent > 0) amount * (100 - promoPercent) / 100 else amount

    val canPay: Boolean get() = consent && selectedCard != null && !submitting && !checking && !loading
}

sealed interface AutopayConfirmEvent {
    data class Init(val childId: String, val planId: String, val period: PlanPeriod, val amount: Int, val cardId: String?) : AutopayConfirmEvent
    data object Resumed : AutopayConfirmEvent
    data object OpenCardPicker : AutopayConfirmEvent
    data class PickCard(val cardId: String) : AutopayConfirmEvent
    data object AddNewCard : AutopayConfirmEvent
    data object DismissCardPicker : AutopayConfirmEvent
    data object OpenPromo : AutopayConfirmEvent
    data class PromoEntered(val code: String) : AutopayConfirmEvent
    data object DismissPromo : AutopayConfirmEvent
    data object RemovePromo : AutopayConfirmEvent
    data class ConsentChanged(val value: Boolean) : AutopayConfirmEvent
    data object Pay : AutopayConfirmEvent
    data object DismissDeclined : AutopayConfirmEvent
    data object DismissError : AutopayConfirmEvent
    data object DismissTimeout : AutopayConfirmEvent
}

sealed interface AutopayConfirmEffect {
    data class OpenAddCard(val planId: String, val period: PlanPeriod, val amount: Int) : AutopayConfirmEffect
    data class Done(
        val scheduled: Boolean,
        val amount: Int,
        val paidUntil: Instant?,
        val nextChargeAt: Instant?,
        val card: PayCard?,
    ) : AutopayConfirmEffect
    /** Natija noma'lum qoldi — foydalanuvchi "Obuna" bo'limida ko'radi. */
    data object CloseToSubscription : AutopayConfirmEffect
}
