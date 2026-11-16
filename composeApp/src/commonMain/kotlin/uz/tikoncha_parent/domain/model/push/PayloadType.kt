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

    /** Farzand telefonida muhim ruxsat öçirildi (backend-contract §1). */
    PERMISSION_OFF,

    @SerialName("LIVE_RING")
    LIVE_RING,
}