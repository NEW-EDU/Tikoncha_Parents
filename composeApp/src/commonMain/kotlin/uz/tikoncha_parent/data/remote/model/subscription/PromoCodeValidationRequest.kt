package uz.tikoncha_parent.data.remote.model.subscription

import kotlinx.serialization.Serializable

@Serializable
data class PromoCodeValidationRequest(
    val code: String,
    val amount: Int,
    val plan_duration: String?,
    /** Tarifga bog'langan promokodlar uchun (avto-to'lov tasdiqlash ekrani yuboradi). */
    val plan_id: String? = null,
)
