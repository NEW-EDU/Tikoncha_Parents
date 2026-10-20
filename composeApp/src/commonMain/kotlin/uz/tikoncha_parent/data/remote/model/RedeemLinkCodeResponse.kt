package uz.tikoncha_parent.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class RedeemLinkCodeResponse(
    val success: Boolean,
    val data: RedeemLinkData? = null,
    val error: String? = null,
    val code: Int
)

@Serializable
data class RedeemLinkData(
    val message: String? = null,
    val parent_user_id: String? = null,
    val child_user_id: String? = null,
)
