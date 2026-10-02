package uz.tikoncha_parent.data.remote.model.protection

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProtectionStatusResponse(
    val success: Boolean = false,
    val data: ProtectionStatusData? = null,
    val error: String? = null,
    val code: Int? = null,
)

@Serializable
data class ProtectionStatusData(
    @SerialName("mode_status") val modeStatus: ModeStatusDto? = null,
    @SerialName("last_sync_at") val lastSyncAt: String? = null,
    /** Telefon jadvallar ro'yxatini oxirgi marta qo'llagan vaqt; null — eski ilova */
    @SerialName("policies_synced_at") val policiesSyncedAt: String? = null,
    /** true — oxirgi o'zgarishlar telefonda; false — hali yetib bormagan; null — noma'lum */
    @SerialName("policies_up_to_date") val policiesUpToDate: Boolean? = null,
    @SerialName("strict_disable_request") val strictDisableRequest: ChildRequestDto? = null,
    @SerialName("logout_request") val logoutRequest: AccountRequestDto? = null,
    @SerialName("delete_request") val deleteRequest: AccountRequestDto? = null,
)

/**
 * Bola data-exchange'da yuborgan holat — server o'zgartirmasdan uzatadi,
 * shuning uchun key'lar camelCase. unlock_data esa server qo'shadi (snake_case).
 */
@Serializable
data class ModeStatusDto(
    @SerialName("currentMode") val currentMode: String? = null,    // DEFAULT | GUJANAK | STRICT
    @SerialName("strictMethod") val strictMethod: String? = null,  // TIMER | SECRET_CODE | TEXT | PARENT_REQUEST
    @SerialName("unlock_data") val unlockData: String? = null,     // faqat SECRET_CODE bo'lganda
    val enabled: List<String> = emptyList(),
    val disabled: List<String> = emptyList(),
)

/** Qalqonni o'chirish so'rovi (strict tizimi) */
@Serializable
data class ChildRequestDto(
    val id: String? = null,
    val status: String? = null,    // pending | approved | rejected | cancelled | expired
    @SerialName("child_user_id") val childUserId: String? = null,
    @SerialName("child_name") val childName: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("decided_at") val decidedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
)

/** Hisobdan chiqish / ilovani o'chirish so'rovi (parent-requests tizimi) */
@Serializable
data class AccountRequestDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val action: String? = null,                 // "logout" | "delete"
    val status: String? = null,                 // "process" | "access" | "deny"
    val packages: List<String>? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("modified_at") val modifiedAt: String? = null,
)

@Serializable
data class ChildRequestsResponse(
    val success: Boolean = false,
    val data: ChildRequestsData? = null,
    val error: String? = null,
    val code: Int? = null,
)

@Serializable
data class ChildRequestsData(
    val items: List<ChildRequestDto> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
    @SerialName("has_next") val hasNext: Boolean = false,
    @SerialName("has_previous") val hasPrevious: Boolean = false,
)

@Serializable
data class RequestActionResponse(
    val success: Boolean = false,
    val data: ChildRequestDto? = null,
    val error: String? = null,
    val code: Int? = null,
)


@Serializable
data class AccountRequestActionRequest(
    val status: String,    // "access" | "deny"
)
@Serializable
data class AccountRequestActionResponse(
    val success: Boolean = false,
    val data: AccountRequestDto? = null,
    val error: String? = null,
    val code: Int? = null,
)