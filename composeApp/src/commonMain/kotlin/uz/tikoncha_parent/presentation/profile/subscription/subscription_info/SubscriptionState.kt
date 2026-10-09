package uz.tikoncha_parent.presentation.profile.subscription.subscription_info

import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.domain.model.autopay.PayCard
import uz.tikoncha_parent.domain.model.autopay.AutopayInfo
import uz.tikoncha_parent.domain.model.app_error.Outcome
import uz.tikoncha_parent.domain.model.subscription.SubscriptionStatus
import uz.tikoncha_parent.presentation.ui_state.ResponseState

data class SubscriptionState(
    val subscriptionStatusState: ResponseState<Unit> = ResponseState.Idle,
    val subscription: SubscriptionStatus? = null,
    val selectedChildId: String? = null,
    /** Sarlavhadagi tanlagich (Statistika'dagi bilan bir xil) uchun. */
    val children: List<UserInfo> = emptyList(),
    val selectedChild: UserInfo? = null,
    /** PLUS yo'q farzand: "karta orqali oyiga … dan" — tarifdan; yuklanmasa null (yozuv chiqmaydi). */
    val freeFromPrice: Int? = null,
    /** Karta orqali avto-to'lov holati (shu farzand uchun). `null` — yuklanmagan yoki server javob bermadi (bo'lim ko'rinmaydi). */
    val autopay: AutopayInfo? = null,
    val autopayBusy: Boolean = false,
    val confirmDisable: Boolean = false,
    val showCardPicker: Boolean = false,
    val cards: List<PayCard> = emptyList(),
    val autopayError: Outcome.Failure? = null,
    /** Qayta urinish o'tmadi — server tilida tayyor matn. */
    val retryDeclinedText: String? = null,
)