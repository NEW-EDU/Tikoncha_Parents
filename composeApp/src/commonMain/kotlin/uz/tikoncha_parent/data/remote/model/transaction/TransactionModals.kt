package uz.tikoncha_parent.data.remote.model.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionHistoryResponse(
    val success: Boolean = false,
    val data: TransactionHistoryData? = null,
    val error: String? = null,
    val code: Int? = null,
)

@Serializable
data class TransactionHistoryData(
    val items: List<TransactionDto> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
    @SerialName("has_next") val hasNext: Boolean = false,
    @SerialName("has_previous") val hasPrevious: Boolean = false,
)



@Serializable
data class TransactionDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("child_user_id") val childUserId: String? = null,
    @SerialName("user_first_name") val userFirstName: String? = null,
    @SerialName("user_last_name") val userLastName: String? = null,
    @SerialName("user_phone") val userPhone: String? = null,
    @SerialName("is_user_registered") val isUserRegistered: Boolean = false,
    @SerialName("merchant_trans_id") val merchantTransId: String,
    val amount: Long = 0L,
    @SerialName("original_amount") val originalAmount: Long? = null,
    val coins: Long = 0L,
    val status: String? = null,
    @SerialName("purchase_type") val purchaseType: String? = null,
    @SerialName("plan_name") val planName: String? = null,
    @SerialName("plan_duration") val planDuration: String? = null,
    @SerialName("expired_at") val expiredAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    /** CLICK (bir martalik) yoki PAYLOV (karta orqali avto-to'lov). */
    val provider: String = "CLICK",
    /** Avto-to'lov kartasi: "HUMO •• 4821". */
    @SerialName("card_mask") val cardMask: String? = null,
)