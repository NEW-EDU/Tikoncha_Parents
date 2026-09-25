package uz.tikoncha_parent.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class AppsData(
    val items: List<AppDto>
)

@Serializable
data class AppDto(
    val `package`: String,
    val name: String? = null,
    val category: String? = null,
    val icon: String? = null,
    val order: Int = Int.MAX_VALUE,
)