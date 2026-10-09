package uz.tikoncha_parent.data.remote.model.autopay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Karta orqali avto-to'lov (Paylov) kontrakti.
 * Backend: app/modules/autopay/schemas.py, router `/autopay`.
 * Envelope: {success, data, error, code}; HTTP status doim 200.
 *
 * Karta raqami faqat `CardStartRequest` da serverga ketadi va hech qayerda saqlanmaydi.
 * Parent farzand uchun to'laydi: `child_user_id` har so'rovda (GET/DELETE — query, qolgani — tana).
 */

// ── So'rovlar ─────────────────────────────────────────────

@Serializable
data class CardStartRequest(
    @SerialName("card_number") val cardNumber: String,
    val expire: String,
)

@Serializable
data class CardConfirmRequest(val otp: String)

@Serializable
data class AutopayEnableRequest(
    @SerialName("plan_id") val planId: String,
    @SerialName("plan_duration") val planDuration: String,
    @SerialName("card_id") val cardId: String,
    val consent: Boolean,
    val promocode: String? = null,
    @SerialName("child_user_id") val childUserId: String? = null,
)

@Serializable
data class AutopayCardRequest(
    @SerialName("card_id") val cardId: String,
    @SerialName("child_user_id") val childUserId: String? = null,
)

@Serializable
data class AutopayChildRequest(
    @SerialName("child_user_id") val childUserId: String? = null,
)

// ── Javob ma'lumotlari ────────────────────────────────────

@Serializable
data class CardDto(
    val id: String,
    @SerialName("masked_pan") val maskedPan: String = "",
    val vendor: String = "",
    val expire: String = "",
    @SerialName("in_use") val inUse: Boolean = false,
    /** "Asosiy karta": avto-to'lov shundan, yangi to'lovda oldindan tanlanadi. */
    @SerialName("is_primary") val isPrimary: Boolean = false,
)

@Serializable
data class CardStartDto(
    val id: String,
    @SerialName("masked_pan") val maskedPan: String = "",
    val vendor: String = "",
    @SerialName("phone_mask") val phoneMask: String = "",
)

@Serializable
data class AutopayDto(
    val available: Boolean = false,
    val enabled: Boolean = false,
    val mine: Boolean = false,
    /** Boshqasi to'laydi va bu — farzandning o'zi (Student ilovasidan). */
    @SerialName("payer_is_child") val payerIsChild: Boolean = false,
    @SerialName("child_user_id") val childUserId: String? = null,
    val status: String? = null,
    @SerialName("plan_id") val planId: String? = null,
    @SerialName("plan_duration") val planDuration: String? = null,
    val amount: Int? = null,
    val card: CardDto? = null,
    @SerialName("next_charge_at") val nextChargeAt: String? = null,
    @SerialName("last_error") val lastError: String? = null,
    @SerialName("last_error_text") val lastErrorText: String? = null,
    @SerialName("fail_count") val failCount: Int = 0,
    @SerialName("paid_until") val paidUntil: String? = null,
)

@Serializable
data class ChargeResultDto(
    val status: String,
    val error: String? = null,
    @SerialName("error_text") val errorText: String? = null,
    @SerialName("transaction_id") val transactionId: String? = null,
    val autopay: AutopayDto,
)

@Serializable
data class ChargeStatusDto(
    @SerialName("transaction_id") val transactionId: String,
    val status: String,
    @SerialName("fail_reason") val failReason: String? = null,
    val amount: Int = 0,
)

// ── Envelope'lar ──────────────────────────────────────────

@Serializable
data class CardListResponse(val success: Boolean, val data: List<CardDto>? = null, val error: String? = null, val code: Int? = null)

@Serializable
data class CardResponse(val success: Boolean, val data: CardDto? = null, val error: String? = null, val code: Int? = null)

@Serializable
data class CardStartResponse(val success: Boolean, val data: CardStartDto? = null, val error: String? = null, val code: Int? = null)

@Serializable
data class AutopayResponse(val success: Boolean, val data: AutopayDto? = null, val error: String? = null, val code: Int? = null)

@Serializable
data class ChargeResultResponse(val success: Boolean, val data: ChargeResultDto? = null, val error: String? = null, val code: Int? = null)

@Serializable
data class ChargeStatusResponse(val success: Boolean, val data: ChargeStatusDto? = null, val error: String? = null, val code: Int? = null)

@Serializable
data class OkResponse(val success: Boolean, val error: String? = null, val code: Int? = null)
