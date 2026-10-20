package uz.tikoncha_parent.data.remote.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentRegionResponse(
    val success: Boolean = false,
    val data: PaymentRegionDto? = null,
    val code: Int? = null,
)

@Serializable
data class PaymentRegionDto(
    val country: String? = null,
    @SerialName("local_payments") val localPayments: Boolean = false,
)
