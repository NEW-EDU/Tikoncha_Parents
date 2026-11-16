package uz.tikoncha_parent.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ChildrenLocationResponse(
    val success: Boolean,
    val data: ChildrenLocationData?,
    val error: String? = null,
    val code:Int
)
@Serializable
data class ChildrenLocationData(
    val items: List<ChildrenLocationItemDto>
)

@Serializable
data class ChildrenLocationItemDto(
    val child_user_id: String?,
    val first_name: String?,
    val last_name: String?,
    val avatar_url: String?,
    val lat: Double?,
    val lng: Double?,
    val updated_at: String?,
    /** Hali serverda yo'q (kelmasa null): nuqta soxta (mock) joylashuv provayderidan olingan. */
    val is_mock: Boolean? = null,
    /** Hali serverda yo'q (kelmasa null): nuqta aniqligi, metr — xaritadagi aniqlik doirasi uchun. */
    val accuracy_m: Double? = null,
)
