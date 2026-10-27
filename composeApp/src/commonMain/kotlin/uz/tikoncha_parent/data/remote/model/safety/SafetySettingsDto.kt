package uz.tikoncha_parent.data.remote.model.safety

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET/PUT safety/settings` — ota-ona farzand uçun MAJBURLAGAN himoyalar (`true` — farzand öçira olmaydi). */
@Serializable
data class SafetySettingsData(
    @SerialName("danger_zones") val dangerZones: Boolean = true,
    val conversation: Boolean = false,
    @SerialName("image_detection") val imageDetection: Boolean = false,
    @SerialName("block_incognito") val blockIncognito: Boolean = false,
)

/** Faqat özgargan maydon yuboriladi (null — tegilmaydi). */
@Serializable
data class SafetySettingsRequest(
    @SerialName("child_id") val childId: String,
    @SerialName("danger_zones") val dangerZones: Boolean? = null,
    val conversation: Boolean? = null,
    @SerialName("image_detection") val imageDetection: Boolean? = null,
    @SerialName("block_incognito") val blockIncognito: Boolean? = null,
)
