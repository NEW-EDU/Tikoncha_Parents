package uz.tikoncha_parent.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUsageDataDto(
    val items: List<AppUsageItemDto>
)

@Serializable
data class AppUsageItemDto(
    @SerialName("package") val packageName: String,
    val name: String,
    val icon: String? = null,
    val usage: Map<String, Map<String, Long>>
)
