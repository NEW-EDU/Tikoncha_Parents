package uz.tikoncha_parent.presentation.profile.subscription.subscription_payment

import uz.tikoncha_parent.domain.model.SubscriptionType
import uz.tikoncha_parent.domain.model.UserInfo
import uz.tikoncha_parent.presentation.ui_state.ResponseState

data class SubscriptionPaymentState(
    val subscriptionUi: SubscriptionUi? = null,
    val currentPlan: SubscriptionType = SubscriptionType.FREE,
    val subscriptionPlanState: ResponseState<Nothing> = ResponseState.Idle,
    val selectedChild: UserInfo? = null,
    val children: List<UserInfo> = emptyList(),
    /** Server IP bo'yicha: O'zbekiston → mahalliy to'lovlar; null — hali noma'lum. */
    val localPayments: Boolean? = null,
)
