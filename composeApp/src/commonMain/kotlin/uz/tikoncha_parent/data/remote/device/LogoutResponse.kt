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

/** `GET /users/me/deletion`: sayt, bot yoki ilovadan söralgan öçiriş 7 kun kutayotgan bölsa — sanasi. */
@Serializable
data class DeletionStatusResponse(
    val success: Boolean,
    val data: DeletionStatusData? = null,
    val error: String? = null,
)

@Serializable
data class DeletionStatusData(val scheduled_for: String? = null)
