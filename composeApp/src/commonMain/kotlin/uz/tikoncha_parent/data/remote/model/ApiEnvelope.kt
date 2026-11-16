package uz.tikoncha_parent.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Server javobining umumiy o'rami. HTTP doim 200; haqiqiy status [code] da
 * (200/201/403/404/422...), xato matni [error] da (LANG-CODE bo'yicha tarjima qilingan).
 */
@Serializable
data class ApiEnvelope<T>(
    val success: Boolean = false,
    val data: T? = null,
    val error: String? = null,
    val code: Int? = null,
    /** Maşina öqiydigan xato kodi (masalan `policy_count_limit`). Eski serverda va kodsiz xatolarda yöq. */
    @SerialName("error_code") val errorCode: String? = null,
)
