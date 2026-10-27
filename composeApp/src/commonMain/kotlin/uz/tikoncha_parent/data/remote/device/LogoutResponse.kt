package uz.tikoncha_parent.data.remote.device

import kotlinx.serialization.Serializable

@Serializable
data class LogoutResponse(
    val success: Boolean,
    val error: String?
)

/** `DELETE /users/`: status deleted | scheduled (ota-ona — 7 kundan keyin) | pending_parent. */
@Serializable
data class DeleteAccountResponse(
    val success: Boolean,
    val data: DeleteAccountData? = null,
    val error: String? = null,
)

@Serializable
data class DeleteAccountData(val status: String? = null, val scheduled_for: String? = null)
