package uz.tikoncha_parent.data.remote.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `POST subscriptions/apple/transactions` — App Store xaridini serverga tasdiqlatish (PLUS farzandga). */
@Serializable
data class AppleTransactionRequest(
    @SerialName("signed_transaction") val signedTransaction: String,
    @SerialName("child_user_id") val childUserId: String? = null,
)

@Serializable
data class AppleSubscriptionData(
    val active: Boolean = false,
    @SerialName("plan_duration") val planDuration: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
)
