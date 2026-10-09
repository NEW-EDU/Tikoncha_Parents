package uz.tikoncha_parent.presentation.profile.subscription.autopay.method

import uz.tikoncha_parent.domain.model.autopay.PlanPeriod
import kotlin.math.roundToInt

enum class PayMethod { CARD, CLICK }

data class PaymentMethodState(
    /** Farzand user id; null — ro'yxatdan o'tmagan bola (telefon raqami yo'li) → faqat Click. */
    val childId: String? = null,
    val planId: String = "",
    val period: PlanPeriod = PlanPeriod.ANNUAL,
    /** Karta orqali avto-to'lov narxi (arzonroq). */
    val cardAmount: Int = 0,
    /** Click — bir martalik narx. */
    val clickAmount: Int = 0,
    val loading: Boolean = true,
    /** Server karta to'lovini ochgan (sozlangan, sandbox emas) va farzand ma'lum. Yo'q bo'lsa faqat Click. */
    val cardAvailable: Boolean = false,
    val hasSavedCards: Boolean = false,
    val selected: PayMethod = PayMethod.CARD,
) {
    /** Click'ga nisbatan karta qancha arzon (foiz, butun). Arzon bo'lmasa — null. */
    val cardSavingPercent: Int?
        get() = if (clickAmount > 0 && cardAmount in 1 until clickAmount)
            ((clickAmount - cardAmount) * 100f / clickAmount).roundToInt() else null

    val selectedAmount: Int get() = if (selected == PayMethod.CARD && cardAvailable) cardAmount else clickAmount
}

sealed interface PaymentMethodEvent {
    data class Init(val childId: String?, val planId: String, val period: PlanPeriod, val cardAmount: Int, val clickAmount: Int) : PaymentMethodEvent
    data class Select(val method: PayMethod) : PaymentMethodEvent
    data object Continue : PaymentMethodEvent
}

sealed interface PaymentMethodEffect {
    data class OpenAddCard(val planId: String, val period: PlanPeriod, val amount: Int) : PaymentMethodEffect
    data class OpenConfirm(val planId: String, val period: PlanPeriod, val amount: Int) : PaymentMethodEffect
    data class OpenClick(val planId: String, val period: PlanPeriod, val amount: Int) : PaymentMethodEffect
}
