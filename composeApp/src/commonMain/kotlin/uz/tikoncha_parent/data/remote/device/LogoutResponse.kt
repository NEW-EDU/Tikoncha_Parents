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

/** `GET/PUT /users/me/contact-phone`: SOS qönğirog'i uçun raqam (Google/Apple bilan kirganlarda login raqami yöq). */
@Serializable
data class ContactPhoneResponse(
    val success: Boolean,
    val data: ContactPhoneData? = null,
    val error: String? = null,
)

@Serializable
data class ContactPhoneData(val phone: String? = null, val needs_contact: Boolean = false)

@Serializable
data class ContactPhoneRequest(val phone: String)
