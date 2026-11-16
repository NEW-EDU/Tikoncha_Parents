package uz.tikoncha_parent.data.remote.model

import kotlinx.serialization.Serializable


@Serializable
data class RefreshTokenResponse(
    val success: Boolean,
    val data: RefreshTokenResponseData?,
    val error: String? = null,
    val code: Int,
    /** Kelajak uçun (backend-contract §6): `session_ended` — sessiya tugatilgan. Hozir kelmaydi. */
    val error_code: String? = null,
)

@Serializable
data class RefreshTokenResponseData(
    val access_token: String = "",
    val access_token_expires_in: Long = 0,
    val token_type: String = ""
)