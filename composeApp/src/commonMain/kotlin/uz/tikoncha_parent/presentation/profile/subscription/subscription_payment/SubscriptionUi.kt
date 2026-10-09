package uz.tikoncha_parent.presentation.profile.subscription.subscription_payment

import uz.tikoncha_parent.domain.model.SubscriptionType

data class SubscriptionUi(
    val planId: String,
    val type: SubscriptionType,
    /** `price` — Click (bir martalik) narxi. */
    val monthly: PlanUi,
    val annual: PlanUi,
    /** Karta orqali avto-to'lov narxi; server bermasa null (karta yo'li yopiq yoki eski server). */
    val cardMonthly: Int? = null,
    val cardAnnual: Int? = null,
)

data class PlanUi(
    val price: Int,
    val coin: Int,
    val feature: List<String>,
    val bonus: List<String>
)