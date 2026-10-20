package uz.tikoncha_parent.data.remote.model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `POST auth/apple-login`. */
@Serializable
data class AppleLoginRequest(
    @SerialName("identity_token") val identityToken: String,
    @SerialName("app_code") val appCode: Int,
    @SerialName("full_name") val fullName: String? = null,
)

/** `POST auth/google-login`. */
@Serializable
data class GoogleLoginRequest(
    @SerialName("id_token") val idToken: String,
    @SerialName("app_code") val appCode: Int,
)
