package uz.tikoncha_parent.data.remote.model

import kotlinx.serialization.Serializable

/** POST /auth/refresh tanasi — token URL'da emas (URL server log'iga yoziladi). */
@Serializable
data class RefreshTokenRequest(
    val refresh_token: String
)
