package uz.tikoncha_parent.domain.model.push

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PayloadType {
    GENERAL,
    NEWS,
    CHAT,
    PARENTAL_REQUEST,
    STRICT_DISABLE,

    @SerialName("LIVE_RING")
    LIVE_RING,
}